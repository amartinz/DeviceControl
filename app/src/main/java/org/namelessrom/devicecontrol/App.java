/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.CustomTabsHelper;

import java.io.File;

import alexander.martinz.libs.execution.ShellManager;
import io.fabric.sdk.android.Fabric;
import io.paperdb.Paper;
import timber.log.Timber;
import uk.co.senab.bitmapcache.BitmapLruCache;

// XXX: DO NOT USE ROOT HERE! NEVER!
public class App extends android.app.Application {
    public static final Handler HANDLER = new Handler();

    private static final int APP_VERSION = 1;

    private static final Timber.Tree DEBUG_TREE = new Timber.DebugTree();

    private static App sInstance;
    private static boolean enableDebug = BuildConfig.DEBUG;

    private BitmapLruCache bitmapLruCache;
    private CustomTabsHelper customTabsHelper;

    private PowerManager powerManager;
    private Vibrator vibrator;

    public static App get() {
        return App.sInstance;
    }

    public static App get(Context context) {
        return ((App) context.getApplicationContext());
    }

    public BitmapLruCache getBitmapLruCache() {
        if (bitmapLruCache == null) {
            bitmapLruCache = setupBitmapLruCache();
        }
        return bitmapLruCache;
    }

    public CustomTabsHelper getCustomTabsHelper() {
        if (customTabsHelper == null) {
            customTabsHelper = new CustomTabsHelper(this);
        }
        return customTabsHelper;
    }

    public PowerManager getPowerManager() {
        if (powerManager == null) {
            powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        }
        return powerManager;
    }

    public Vibrator getVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        return vibrator;
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (bitmapLruCache != null) {
            bitmapLruCache.trimMemory();
        }
    }

    @Override public void onCreate() {
        super.onCreate();

        if (App.sInstance == null) {
            App.sInstance = this;

            Fabric.with(App.sInstance, new Answers(), new Crashlytics());

            // force enable logger until we hit the user preference
            if (BuildConfig.DEBUG) {
                Timber.plant(DEBUG_TREE);
            } else {
                Timber.plant(new AwesomeTree());
            }
            ShellManager.enableDebug(true);

            Paper.init(this);

            AsyncTask.execute(new Runnable() {
                @Override public void run() {
                    setupEverythingAsync();
                }
            });
        }
    }

    @WorkerThread private void setupEverythingAsync() {
        ShellManager.enableDebug(App.enableDebug);

        Timber.d("Enable debug: %s", App.enableDebug);

        final String basePath = getFilesDirectory();
        final String[] dirList = new String[]{ basePath + DeviceConstants.DC_LOG_DIR };
        for (final String s : dirList) {
            final File dir = new File(s);
            if (!dir.exists()) {
                Timber.v("setupDirectories: creating %s -> %s", s, dir.mkdirs());
            }
        }

        handleAppUpgrades();

        if (BuildConfig.DEBUG || App.enableDebug) {
            final int gmsVersion = getResources().getInteger(R.integer.google_play_services_version);
            Timber.v("Google Play Services -> %s", gmsVersion);
        }
    }

    private void handleAppUpgrades() {
        boolean needsUpgrade = false;

        final DeviceConfig deviceConfig = DeviceConfig.get();
        if (deviceConfig.appVersion < 1) {
            needsUpgrade = true;

            final File externalCache = new File(getExternalCacheDir(), "bitmapCache");
            clearDirectory(externalCache);

            final File internalCacheOld = new File(getFilesDir(), "bitmapCache");
            clearDirectory(internalCacheOld);

            final File internalCache = new File(getCacheDir(), "bitmapCache");
            clearDirectory(internalCache);
        }

        if (needsUpgrade) {
            deviceConfig.appVersion = APP_VERSION;
            deviceConfig.save();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private void clearDirectory(File directory) {
        if (directory.exists()) {
            final File[] cacheFiles = directory.listFiles();
            for (final File cacheFile : cacheFiles) {
                cacheFile.delete();
            }
            directory.delete();
        }
    }

    private BitmapLruCache setupBitmapLruCache() {
        final File cacheLocation = getCacheDirectory();
        try {
            Timber.d("Setting up cache: %s\nNeed to create dirs: %s", cacheLocation.getAbsolutePath(), cacheLocation.mkdirs());
        } catch (SecurityException sex) {
            Timber.wtf(sex, "can not create directory");
        }

        final BitmapLruCache.Builder builder = new BitmapLruCache.Builder(this);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(0.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        return builder.build();
    }

    private File getCacheDirectory() {
        // TODO: configurable
        if (true) {
            return new File(getCacheDir(), "bitmapCache");
        }

        File cacheLocation = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(getExternalCacheDir(), "bitmapCache");

            // if we can not read or write, use the internal storage for caches
            try {
                if (!cacheLocation.canRead() || !cacheLocation.canWrite()) {
                    cacheLocation = null;
                }
            } catch (SecurityException sex) {
                cacheLocation = null;
                Timber.e(sex, "can not read or write directory");
            }
        }

        if (cacheLocation == null) {
            cacheLocation = new File(getCacheDir(), "bitmapCache");
        }
        return cacheLocation;
    }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + getPackageName();
        }
    }

    public int getColorApplication(@ColorRes final int resId) {
        return ContextCompat.getColor(this, resId);
    }

    public Drawable getDrawableApplication(@DrawableRes final int resId) {
        return ContextCompat.getDrawable(this, resId);
    }

    public String[] getStringArray(final int resId) {
        return getResources().getStringArray(resId);
    }

    private static class AwesomeTree extends Timber.DebugTree {
        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (!App.enableDebug && (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO)) {
                return;
            }

            super.log(priority, tag, message, t);
        }
    }
}
