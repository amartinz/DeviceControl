package org.namelessrom.devicecontrol.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.RemoteException;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

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
        final String base = "rm -rf /data/data/" + pkg;
        Utils.runRootCommand(base + "/app_*/*;" + base + "/cache/*;");
    }

    public static void clearData(final String pkg) {
        final String base = "rm -rf /data/data/" + pkg;
        Utils.runRootCommand("pkill -TERM " + pkg + ';' + base + "/app_*;" + base + "/cache/;"
                + base + "/databases/;" + base + "/files/;" + base + "/shared_prefs/;");
    }

    public static void killApp(final String pkg) {
        Utils.runRootCommand("pkill -TERM " + pkg);
    }

    public static boolean isAppRunning(final Context context, final String pkg) {
        final ActivityManager aM =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
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

    public static String convertSize(final long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(1024, digitGroups)) + ' ' + units[digitGroups];
    }

}
