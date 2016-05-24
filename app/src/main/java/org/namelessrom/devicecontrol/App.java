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
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.CustomTabsHelper;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import at.amartinz.execution.ShellManager;
import at.amartinz.universaldebug.UniversalDebug;
import at.amartinz.universaldebug.fabric.FabricConfig;
import at.amartinz.universaldebug.fabric.trees.CrashlyticsComponent;
import at.amartinz.universaldebug.trees.BaseTree;
import at.amartinz.universaldebug.trees.LogComponent;
import at.amartinz.universaldebug.trees.VibrationComponent;
import at.amartinz.universaldebug.trees.WriterComponent;
import io.paperdb.Paper;
import timber.log.Timber;

// XXX: DO NOT USE ROOT HERE! NEVER!
public class App extends android.app.Application {
    public static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static final int APP_VERSION = 1;

    private static App sInstance;
    private static boolean enableDebug = BuildConfig.DEBUG;

    private CustomTabsHelper customTabsHelper;

    private PowerManager powerManager;
    private Vibrator vibrator;

    public static App get() {
        return App.sInstance;
    }

    public static App get(Context context) {
        return ((App) context.getApplicationContext());
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

    @Override public void onCreate() {
        super.onCreate();
        if (App.sInstance != null) {
            return;
        }
        App.sInstance = this;

        final UniversalDebug universalDebug = new UniversalDebug(this)
                .withDebug(BuildConfig.DEBUG)
                .withTimber(true)
                .withDebugTree(buildDebugTree())
                .withProductionTree(buildProductionTree());

        if (!enableDebug) {
            final FabricConfig fabricConfig = new FabricConfig(universalDebug)
                    .withAnswers()
                    .withCrashlytics();
            universalDebug.withExtension(fabricConfig);
        }

        universalDebug.install();
        ShellManager.enableDebug(App.enableDebug);

        Paper.init(this);
        setupThemeMode();

        AsyncTask.execute(new Runnable() {
            @Override public void run() {
                setupEverythingAsync();
            }
        });
    }

    public static void setupThemeMode() {
        final DeviceConfig deviceConfig = DeviceConfig.get();
        switch (deviceConfig.themeMode) {
            case DeviceConfig.THEME_AUTO: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
                break;
            }
            case DeviceConfig.THEME_DAY: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            }
            default:
            case DeviceConfig.THEME_NIGHT: {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            }
        }
    }

    @WorkerThread private void setupEverythingAsync() {
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

        if (App.enableDebug) {
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
            if (cacheFiles != null) {
                for (final File cacheFile : cacheFiles) {
                    cacheFile.delete();
                }
            }
            directory.delete();
        }
    }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + getPackageName();
        }
    }

    public String[] getStringArray(final int resId) {
        return getResources().getStringArray(resId);
    }

    private BaseTree buildDebugTree() {
        return buildTree(true);
    }

    private BaseTree buildProductionTree() {
        return buildTree(false);
    }

    private BaseTree buildTree(boolean isDebug) {
        final HashSet<Integer> priorityFilter = new HashSet<>();
        // if we are in release mode, remove all log calls except ERROR and WARN
        if (!isDebug) {
            priorityFilter.addAll(Arrays.asList(Log.ASSERT, Log.DEBUG, Log.INFO, Log.VERBOSE));
        }
        final BaseTree baseTree = new BaseTree(this, priorityFilter);

        final LogComponent logComponent = new LogComponent(baseTree);
        baseTree.addComponent(logComponent);

        if (isDebug) {
            final VibrationComponent vibrationComponent = new VibrationComponent(baseTree);
            // only vibrate on error logs
            final HashSet<Integer> vibrationFilter = new HashSet<>(
                    Arrays.asList(Log.ASSERT, Log.DEBUG, Log.INFO, Log.VERBOSE, Log.WARN));
            vibrationComponent.setPriorityFilterSet(vibrationFilter);
            baseTree.addComponent(vibrationComponent);

            final WriterComponent writerComponent = new WriterComponent(baseTree, getExternalCacheDir());
            // only vibrate on error and warning logs
            final HashSet<Integer> writerFilter = new HashSet<>(
                    Arrays.asList(Log.ASSERT, Log.DEBUG, Log.INFO, Log.VERBOSE, Log.WARN));
            writerComponent.setPriorityFilterSet(writerFilter);
            baseTree.addComponent(writerComponent);
        }

        final CrashlyticsComponent crashlyticsComponent = new CrashlyticsComponent(baseTree);
        baseTree.addComponent(crashlyticsComponent);

        return baseTree;
    }
}
