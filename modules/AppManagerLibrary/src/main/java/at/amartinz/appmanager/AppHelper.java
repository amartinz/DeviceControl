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
package at.amartinz.appmanager;

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
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.RootShell;
import at.amartinz.hardware.ProcessManager;
import timber.log.Timber;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {
    /**
     * Gets the package stats of the given application.
     * The package stats are getting sent via OTTO
     *
     * @param pkg The package name of the application
     */
    public static void getSize(final PackageManager pm, final PackageStatsObserver.OnPackageStatsListener l, final String pkg) {
        try {
            Method m = pm.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
            m.invoke(pm, pkg, new PackageStatsObserver(l));
        } catch (Exception e) {
            Timber.e(e, "AppHelper.getSize()");
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
            cmdPrefix = String.format("pkill -TERM %s;pm clear %s;sync;", pkg, pkg);

            internal = String.format(dirs, internalBase, internalBase, internalBase, internalBase, internalBase);
            external = String.format(dirs, externalBase, externalBase, externalBase, externalBase, externalBase);
        }

        Timber.d("internal -> %s", internal);
        Timber.d("external -> %s", external);

        try {
            final Method m = pm.getClass().getDeclaredMethod(method, String.class, IPackageDataObserver.class);
            m.invoke(pm, pkg, null);
        } catch (Exception e) {
            Timber.e(e, "could not call %s via reflection", method);
        }

        RootShell.fireAndForget(String.format("%s%s%s;sync;", cmdPrefix, internal, external));
    }

    public static void uninstallPackage(PackageManager pm, String pkg) {
        try {
            Method m = pm.getClass().getDeclaredMethod("deletePackage", String.class, IPackageDeleteObserver.class, int.class);
            m.invoke(pm, pkg, null, /* DELETE_ALL_USERS */ 2);
        } catch (Exception e) {
            Timber.e(e, "could not call deletePackage via reflection");
        }
    }

    /**
     * KILL IT!
     *
     * @param process The name of the application / process to kill
     */
    public static void killProcess(Context context, @NonNull String process) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        am.killBackgroundProcesses(process);

        RootShell.fireAndForget(BusyBox.callBusyBoxApplet("pkill", process));
        RootShell.fireAndForget(BusyBox.callBusyBoxApplet("pkill", String.format("-l TERM %s", process)));
    }

    /**
     * @param processName The process name / path to search for
     */
    @NonNull @WorkerThread public static List<Integer> getProcessIds(final String processName) {
        final ArrayList<Integer> processIdList = new ArrayList<>();
        final List<String> result = RootShell.fireAndBlockList(BusyBox.callBusyBoxApplet("pgrep", processName));
        if (result != null && !result.isEmpty()) {
            for (final String entry : result) {
                final int processId;
                try {
                    processId = Integer.valueOf(entry);
                } catch (NumberFormatException nfe) {
                    continue;
                }
                processIdList.add(processId);
            }
        }
        return processIdList;
    }

    /**
     * Checks if the given application is running
     *
     * @param pkg The package name of the application
     * @return Whether the app is running
     */
    public static boolean isAppRunning(Context context, @NonNull String pkg) {
        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> processList = am.getRunningAppProcesses();
        if (processList != null) {
            for (final ActivityManager.RunningAppProcessInfo procInfo : processList) {
                if (pkg.equals(procInfo.processName)) {
                    return true;
                }
            }

            if (processList.size() <= 1) {
                Timber.v("Using fallback to get process list");
                final List<ProcessManager.Process> processes = ProcessManager.getRunningApps();
                for (final ProcessManager.Process process : processes) {
                    if (pkg.equals(process.name)) {
                        return true;
                    }
                }
            }
        } else {
            Timber.e("Could not get list of running processes!");
        }
        return false;
    }

    /**
     * Checks if a specific service is running.
     *
     * @param serviceName The name of the service
     * @return Whether the service is running or not
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (services == null || services.isEmpty()) {
            return false;
        }

        for (final ActivityManager.RunningServiceInfo info : services) {
            if (info != null && info.service != null) {
                final String className = info.service.getClassName();
                if (TextUtils.isEmpty(className)) {
                    continue;
                }
                if (className.equalsIgnoreCase(serviceName)) {
                    return true;
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
        if (size <= 0) {
            return "0 B";
        }
        final String[] units = new String[]{ "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }


    /**
     * Check if a specific package is installed.
     *
     * @param packageName The package name
     * @return true if package is installed, false otherwise
     */
    public static boolean isPackageInstalled(Context context, @NonNull final String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception ignored) { }
        return false;
    }

    /**
     * Check if Google Play Store is installed
     *
     * @return true if installed
     */
    public static boolean isPlayStoreInstalled(Context context) {
        return isPackageInstalled(context, "com.android.vending");
    }

    /**
     * Shows the app in Google's Play Store if Play Store is installed
     */
    public static boolean showInPlayStore(Context context, String packageName) {
        final String uriString = String.format("market://details?id=%s", packageName);
        final Uri uri = Uri.parse(uriString);

        final Intent i = new Intent(Intent.ACTION_VIEW, uri);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(i);
            return true;
        } catch (Exception exc) {
            Timber.e(exc, exc.getMessage());
        }

        return false;
    }

}
