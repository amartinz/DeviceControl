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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.listeners.OnShellOutputListener;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

import static org.namelessrom.devicecontrol.utils.constants.DeviceConstants.ID_PGREP;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {

    public static boolean preventOnResume = false;

    private static final String DESCRIPTOR = "android.content.pm.IPackageStatsObserver";

    /**
     * Gets the package stats of the given application.
     * The package stats are getting sent via OTTO
     *
     * @param pkg The package name of the application
     * @throws Exception
     */
    public static void getSize(final String pkg) throws Exception {
        final Method getPackageSizeInfo = Application.getPm().getClass().getMethod(
                "getPackageSizeInfo", String.class, IPackageStatsObserver.class);

        getPackageSizeInfo.invoke(Application.getPm(), pkg, mPkgObs);
    }

    /**
     * Clears cache via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearCache(final String pkg) {
        final String base = "rm -rf /data/data/" + pkg;
        Utils.runRootCommand(base + "/app_*/*;" + base + "/cache/*;");
    }

    /**
     * Clears data via shell of the given package name
     *
     * @param pkg The package name of the application
     */
    public static void clearData(final String pkg) {
        final String base = "rm -rf /data/data/" + pkg;
        Utils.runRootCommand("pkill -TERM " + pkg + ';' + base + "/app_*;" + base + "/cache/;"
                + base + "/databases/;" + base + "/files/;" + base + "/shared_prefs/;");
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
        final ActivityManager aM = (ActivityManager) Application.applicationContext
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
                ((ActivityManager) Application.applicationContext
                        .getSystemService(Context.ACTIVITY_SERVICE))
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
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
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
            Application.getPm().getPackageInfo(packageName, 0);
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
            Application.applicationContext.startActivity(i);
            return true;
        } catch (Exception exc) {
            Logger.e(AppHelper.class, exc.getMessage());
        }
        return false;
    }

    /**
     * Shows the app in Google's Play Store if Play Store is installed
     */
    public static Intent showInPlaystore(final Activity activity, final String uri, final int req) {
        try {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivityForResult(i, req);
            return i;
        } catch (Exception exc) {
            Logger.e(AppHelper.class, exc.getMessage());
            return null;
        }
    }

    /**
     * Our Stub for the package stats observer.
     * Usually we just have to override onGetStatsCompleted but my android studio instance is
     * going crazy and produces apps, which crash at onTransact...
     */
    private static final IPackageStatsObserver.Stub mPkgObs = new IPackageStatsObserver.Stub() {
        @Override
        public IBinder asBinder() { return super.asBinder(); }

        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case FIRST_CALL_TRANSACTION: {
                    data.enforceInterface(DESCRIPTOR);
                    final PackageStats _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = PackageStats.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    final boolean _arg1 = (0 != data.readInt());
                    this.onGetStatsCompleted(_arg0, _arg1);
                    return true;
                }
            }
            return true;
        }

        @Override
        public void onGetStatsCompleted(final PackageStats pStats, final boolean succeeded)
                throws RemoteException {
            Logger.v(this, String.format("onGetStatsCompleted(): %s", succeeded));
            Application.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    BusProvider.getBus().post(pStats);
                }
            });
        }
    };

    public static boolean isExternalAllowed() {
        // if we are not a release, we allow to donate externally
        return !BuildConfig.IS_RELEASE;
    }
}
