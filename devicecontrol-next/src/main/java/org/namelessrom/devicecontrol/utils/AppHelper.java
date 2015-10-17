/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */
package org.namelessrom.devicecontrol.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.modules.tools.appmanager.PackageStatsObserver;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import alexander.martinz.libs.execution.ShellManager;
import alexander.martinz.libs.hardware.utils.ProcessManager;
import alexander.martinz.libs.logger.Logger;
import hugo.weaving.DebugLog;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {
    private static final String TAG = AppHelper.class.getSimpleName();

    public static void mayLaunchUrlViaTabs(@NonNull final Activity activity, @NonNull final String url) {
        final Application application = ((Application) activity.getApplicationContext());
        application.getCustomTabsHelper().mayLaunchUrl(url);
    }

    public static void launchUrlViaTabs(@NonNull final Activity activity, @NonNull final String url) {
        final Application application = ((Application) activity.getApplicationContext());
        application.getCustomTabsHelper().launchUrl(activity, url);
    }

    public static void viewInBrowser(Context context, String url) {
        final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(i);
        } catch (Exception e) {
            Logger.e(AppHelper.class, "viewInBrowser", e);
        }
    }

    /**
     * Check if a specific package is installed.
     *
     * @param packageName The package name
     * @return true if package is installed, false otherwise
     */
    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Google Play Store is installed
     *
     * @return true if installed
     */
    public static boolean isPlayStoreInstalled(Context context) {
        return isPackageInstalled(context, Constants.PKG_PLAY_STORE);
    }

    /**
     * Shows the app in Google's Play Store if Play Store is installed
     */
    public static boolean showInPlayStore(Context context, String uri) {
        try {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            return true;
        } catch (Exception exc) {
            Logger.e(AppHelper.class, exc.getMessage());
        }
        return false;
    }

    /**
     * Gets the package stats of the given application. <p>
     * Results are returned to the given listener.
     *
     * @param pkg The package name of the application
     */
    public static void getSize(PackageManager pm, String pkg, PackageStatsObserver.OnPackageStatsListener listener) {
        try {
            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, pkg, new PackageStatsObserver(listener));
        } catch (Exception e) {
            Logger.e(AppHelper.class, "AppHelper.getSize()", e);
        }
    }

    /**
     * Converts a size, given as long, to a human readable representation
     *
     * @param size The size to convert
     * @return A human readable data size
     */
    public static String convertSize(final long size) {
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{ "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }

    /**
     * Checks if the given application is running
     *
     * @param pkg The package name of the application
     * @return Whether the app is running
     */
    @DebugLog public static boolean isAppRunning(Context context, @NonNull String pkg) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();
        if (processList != null) {
            for (final ActivityManager.RunningAppProcessInfo procInfo : processList) {
                if (pkg.equals(procInfo.processName)) {
                    return true;
                }
            }

            if (processList.size() <= 1) {
                Logger.i(TAG, "Using fallback to get process list");
                final List<ProcessManager.Process> processes = ProcessManager.getRunningApps(context);
                for (final ProcessManager.Process process : processes) {
                    if (pkg.equals(process.name)) {
                        return true;
                    }
                }
            }
        } else {
            Logger.e(TAG, "Could not get list of running processes!");
        }
        return false;
    }

    /**
     * KILL IT!
     *
     * @param process The name of the application / process to kill
     */
    public static void killProcess(Context context, @NonNull String process) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(process);

        ShellManager.get().runRootCommand(String.format("pkill -TERM %s", process));
    }

    /**
     * Clears cache via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearCache(Context context, String pkg) {
        deleteCacheOrData(context, pkg, true);
    }

    /**
     * Clears data via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearData(Context context, String pkg) {
        deleteCacheOrData(context, pkg, false);
    }

    private static void deleteCacheOrData(Context context, String pkg, boolean clearCache) {
        // internal (/data)
        final String internal;
        // external (/sdcard/Android)
        final String external;
        final String method;
        final String cmdPrefix;

        final String internalBase = String.format("rm -rf /data/data/%s", pkg);
        final String externalBase = String.format("rm -rf %s/Android/data/%s",
                Environment.getExternalStorageDirectory().getAbsolutePath(), pkg);

        if (clearCache) {
            // 3 x base
            final String dirs = "%s/app_*/*;%s/cache/*;%s/code_cache/*;";
            method = "deleteApplicationCacheFiles";
            cmdPrefix = "";

            internal = String.format(dirs, internalBase, internalBase, internalBase);
            external = String.format(dirs, externalBase, externalBase, externalBase);
        } else {
            // 5 x base
            final String dirs = "%s/app_*;%s/cache;%s/databases;%s/files;%s/shared_prefs;";
            method = "clearApplicationUserData";
            cmdPrefix = String.format("pkill -TERM %s;", pkg);

            internal = String.format(dirs, internalBase, internalBase, internalBase, internalBase, internalBase);
            external = String.format(dirs, externalBase, externalBase, externalBase, externalBase, externalBase);
        }

        final PackageManager pm = context.getPackageManager();
        try {
            final Method m = pm.getClass().getDeclaredMethod(method, String.class, IPackageDataObserver.class);
            m.invoke(pm, pkg, null);
        } catch (Exception e) {
            Logger.e(TAG, "could not call " + method + " via reflection", e);
        }

        Logger.d(TAG, "internal -> %s", internal);
        Logger.d(TAG, "external -> %s", external);

        ShellManager.get().runRootCommand(cmdPrefix + internal + external);
    }

    public static void uninstallPackage(PackageManager pm, String pkg) {
        try {
            Method m = pm.getClass().getDeclaredMethod("deletePackage", String.class, IPackageDeleteObserver.class, int.class);
            m.invoke(pm, pkg, null, /* DELETE_ALL_USERS */ 2);
        } catch (Exception e) {
            Logger.e(TAG, "could not call deletePackage via reflection", e);
        }
    }

}
