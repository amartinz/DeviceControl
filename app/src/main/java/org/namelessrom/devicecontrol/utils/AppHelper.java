package org.namelessrom.devicecontrol.utils;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {

    public static void getSize(final PackageManager pm, final String pkg) throws Exception {
        final Method getPackageSizeInfo = pm.getClass().getMethod(
                "getPackageSizeInfo", String.class, IPackageStatsObserver.class);

        getPackageSizeInfo.invoke(pm, pkg, new IPackageStatsObserver.Stub() {
                    @Override
                    public void onGetStatsCompleted(final PackageStats pStats, boolean succeeded)
                            throws RemoteException {
                        logDebug("onGetStatsCompleted() " + (succeeded ? "succeeded" : "error"));
                        Application.HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                BusProvider.getBus().post(pStats);
                            }
                        });
                    }
                }
        );
    }

    public static void clearCache(final String pkg) {
        Utils.runRootCommand("rm -rf " + "/data/data/" + pkg + "/cache/*");
    }

    public static String convertSize(final long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#")
                .format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }

}
