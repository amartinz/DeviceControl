/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.CustomTabsHelper;

import java.io.File;

import alexander.martinz.libs.execution.ShellLogger;
import io.paperdb.Paper;
import uk.co.senab.bitmapcache.BitmapLruCache;

// XXX: DO NOT USE ROOT HERE! NEVER!
public class Application extends android.app.Application {
    public static final Handler HANDLER = new Handler();

    private static Application sInstance;

    private BitmapLruCache mCache;

    private CustomTabsHelper mCustomTabsHelper;

    public static Application get() {
        return Application.sInstance;
    }

    public static Application get(Context context) {
        return (Application) context.getApplicationContext();
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (mCache != null) {
            mCache.trimMemory();
        }
    }

    @Override public void onCreate() {
        super.onCreate();

        if (Application.sInstance == null) {
            // force enable logger until we hit the user preference
            Logger.setEnabled(true);
            ShellLogger.DEBUG = true;

            Application.sInstance = this;
            Paper.init(this);

            AsyncTask.execute(new Runnable() {
                @Override public void run() {
                    setupEverythingAsync();
                }
            });
        }
    }

    private void setupDirectories() {
        final String basePath = getFilesDirectory();
        final String[] dirList = new String[]{ basePath + DeviceConstants.DC_LOG_DIR };
        for (final String s : dirList) {
            final File dir = new File(s);
            if (!dir.exists()) {
                Logger.v(this, String.format("setupDirectories: creating %s -> %s", s, dir.mkdirs()));
            }
        }
    }

    @WorkerThread private void setupEverythingAsync() {
        setupDirectories();

        final DeviceConfig deviceConfig = DeviceConfig.get();
        if (deviceConfig.debugStrictMode) {
            Logger.setStrictModeEnabled(true);
        }

        Logger.setEnabled(deviceConfig.extensiveLogging);
        ShellLogger.DEBUG = deviceConfig.extensiveLogging;

        dumpInformation();
    }

    private BitmapLruCache buildCache() {
        final File cacheLocation;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(getExternalCacheDir(), "bitmapCache");
        } else {
            cacheLocation = new File(getFilesDir(), "bitmapCache");
        }
        //noinspection ResultOfMethodCallIgnored
        Logger.d(this, "Setting up cache: %s\nNeed to create dirs: %s", cacheLocation.getAbsolutePath(), cacheLocation.mkdirs());

        final BitmapLruCache.Builder builder = new BitmapLruCache.Builder(this);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(0.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        mCache = builder.build();
        return mCache;
    }

    private void dumpInformation() {
        if (!Logger.getEnabled()) {
            return;
        }

        final Resources res = getResources();
        final int gmsVersion = res.getInteger(R.integer.google_play_services_version);
        Logger.i(this, "Google Play Services -> %s", gmsVersion);
    }

    public BitmapLruCache getBitmapCache() {
        if (mCache == null) {
            mCache = buildCache();
        }
        return mCache;
    }

    public CustomTabsHelper getCustomTabsHelper() {
        if (mCustomTabsHelper == null) {
            mCustomTabsHelper = new CustomTabsHelper(sInstance);
        }
        return mCustomTabsHelper;
    }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + Application.get().getPackageName();
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

}
