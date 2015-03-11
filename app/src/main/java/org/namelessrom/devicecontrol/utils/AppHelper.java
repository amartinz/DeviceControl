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
package org.namelessrom.devicecontrol.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.objects.PackageStatsObserver;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import static org.namelessrom.devicecontrol.DeviceConstants.ID_PGREP;
import static org.namelessrom.devicecontrol.objects.ShellOutput.OnShellOutputListener;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {
    private static final String TAG = "AppHelper";
    private static final String DONATE_URL =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZSN2SW53JJQJY";

    public static boolean preventOnResume = false;

    /**
     * Gets the package stats of the given application.
     * The package stats are getting sent via OTTO
     *
     * @param pkg The package name of the application
     */
    public static void getSize(final PackageManager pm,
            final PackageStatsObserver.OnPackageStatsListener l, final String pkg) {
        try {
            final Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo",
                    String.class, IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, pkg, new PackageStatsObserver(l));
        } catch (Exception e) {
            Logger.e(AppHelper.class, "AppHelper.getSize()", e);
        }
    }

    /**
     * Clears cache via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearCache(final PackageManager pm, final String pkg) {
        deleteCacheOrData(pm, pkg, true);
    }

    /**
     * Clears data via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearData(final PackageManager pm, final String pkg) {
        deleteCacheOrData(pm, pkg, false);
    }

    private static void deleteCacheOrData(PackageManager pm, String pkg, boolean clearCache) {
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

            internal = String.format(dirs, internalBase, internalBase, internalBase, internalBase,
                    internalBase);
            external = String.format(dirs, externalBase, externalBase, externalBase, externalBase,
                    externalBase);
        }

        Logger.d(TAG, "internal -> %s", internal);
        Logger.d(TAG, "external -> %s", external);

        try {
            final Method m = pm.getClass().getDeclaredMethod(method,
                    String.class, IPackageDataObserver.class);
            m.invoke(pm, pkg, null);
        } catch (Exception e) {
            Logger.e(TAG, "could not call " + method + " via reflection", e);
        }

        Utils.runRootCommand(cmdPrefix + internal + external);
    }

    public static void uninstallPackage(PackageManager pm, String pkg) {
        try {
            final Method m = pm.getClass().getDeclaredMethod("deletePackage",
                    String.class, IPackageDeleteObserver.class, int.class);
            m.invoke(pm, pkg, null, /* DELETE_ALL_USERS */ 2);
        } catch (Exception e) {
            Logger.e(TAG, "could not call deletePackage via reflection", e);
        }
    }

    /**
     * KILL IT!
     *
     * @param process The name of the application / process to kill
     */
    public static void killProcess(final String process) {
        Utils.runRootCommand("pkill -TERM " + process);
    }

    /**
     * Search for a progress and return it via Otto's event bus.
     *
     * @param process The process name to search for
     */
    public static void getProcess(final OnShellOutputListener listener, final String process) {
        Utils.getCommandResult(listener, ID_PGREP, String.format("pgrep %s", process));
    }

    /**
     * Checks if the given application is running
     *
     * @param pkg The package name of the application
     * @return Whether the app is running
     */
    public static boolean isAppRunning(final String pkg) {
        final ActivityManager aM = (ActivityManager) Application.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = aM.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo procInfo : procInfos) {
                if (procInfo.processName.equals(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a specific service is running.
     *
     * @param serviceName The name of the service
     * @return Whether the service is running or not
     */
    public static boolean isServiceRunning(final String serviceName) {
        final List<ActivityManager.RunningServiceInfo> services =
                ((ActivityManager) Application.get().getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningServices(Integer.MAX_VALUE);

        if (services != null) {
            for (final ActivityManager.RunningServiceInfo info : services) {
                if (info.service != null) {
                    if (info.service.getClassName() != null && info.service.getClassName()
                            .equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Converts a size, given as long, to a human readable representation
     *
     * @param size The size to convert
     * @return A human readable data size
     */
    public static String convertSize(final long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{ "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }


    /**
     * Check if a specific package is installed.
     *
     * @param packageName The package name
     * @return true if package is installed, false otherwise
     */
    public static boolean isPackageInstalled(final String packageName) {
        try {
            Application.get().getPackageManager().getPackageInfo(packageName, 0);
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
    public static boolean isPlayStoreInstalled() {
        return isPackageInstalled("com.android.vending");
    }

    /**
     * Shows the app in Google's Play Store if Play Store is installed
     */
    public static boolean showInPlaystore(final String uri) {
        try {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Application.get().startActivity(i);
            return true;
        } catch (Exception exc) {
            Logger.e(AppHelper.class, exc.getMessage());
        }
        return false;
    }

    public static boolean startExternalDonation(final Context context) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(DONATE_URL));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception exc) {
            return false;
        }
    }

    public static void viewInBrowser(final String url) {
        final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            Application.get().startActivity(i);
        } catch (Exception e) {
            Logger.e(AppHelper.class, "viewInBrowser", e);
        }
    }

}
