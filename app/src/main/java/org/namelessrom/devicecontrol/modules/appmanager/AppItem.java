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
package org.namelessrom.devicecontrol.modules.appmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

import java.lang.ref.WeakReference;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.Command;
import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class AppItem {
    private final PackageInfo pkgInfo;
    private final ApplicationInfo appInfo;
    private final String label;

    private boolean enabled = false;

    public interface DisableEnableListener {
        void OnDisabledOrEnabled();
    }

    public interface UninstallListener {
        void OnUninstallComplete();
    }

    public AppItem(final PackageInfo info, final String label) {
        this.pkgInfo = info;
        this.appInfo = info.applicationInfo;

        this.label = label;

        this.enabled = (appInfo != null && appInfo.enabled);
    }

    public String getLabel() { return label; }

    public PackageInfo getPackageInfo() { return pkgInfo; }

    public ApplicationInfo getApplicationInfo() { return appInfo; }

    public String getPackageName() { return pkgInfo.packageName; }

    public String getVersion() {
        return String.format("%s (%s)", pkgInfo.versionName, pkgInfo.versionCode);
    }

    public boolean isSystemApp() {
        return isSystemApp(appInfo);
    }

    public static boolean isSystemApp(ApplicationInfo applicationInfo) {
        final int mask = (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
        return ((applicationInfo.flags & mask) != 0);
    }

    public boolean isEnabled() { return enabled; }

    public static boolean isEnabled(ApplicationInfo applicationInfo) {
        return (applicationInfo != null && applicationInfo.enabled);
    }

    public void setEnabled(final boolean enabled) { this.enabled = enabled; }

    public boolean launchActivity(Activity activity) {
        return AppItem.launchActivity(activity, this);
    }

    public static boolean launchActivity(Activity activity, AppItem appItem) {
        Intent i = activity.getPackageManager().getLaunchIntentForPackage(appItem.getPackageName());
        if (i != null) {
            try {
                activity.startActivity(i);
                return true;
            } catch (ActivityNotFoundException ignored) { }
        }

        return false;
    }

    public void disable(final DisableEnableListener listener) {
        disableOrEnable(String.format("pm disable %s 2> /dev/null;", pkgInfo.packageName), listener);
    }

    public void enable(final DisableEnableListener listener) {
        disableOrEnable(String.format("pm enable %s 2> /dev/null;", pkgInfo.packageName), listener);
    }

    public void disableOrEnable(final DisableEnableListener listener) {
        if (enabled) {
            disable(listener);
            return;
        }
        enable(listener);
    }

    private void disableOrEnable(String cmd, final DisableEnableListener listener) {
        final Command command = new DisableEnableCommand(cmd, listener);
        RootShell.fireAndForget(command);
    }

    public boolean isRunning(Context context) {
        return AppItem.isRunning(context, this);
    }

    public static boolean isRunning(Context context, AppItem appItem) {
        return AppHelper.isAppRunning(context, appItem.getPackageName());
    }

    public void forceStop(Context context) {
        AppItem.forceStop(context, this);
    }

    public static void forceStop(Context context, AppItem appItem) {
        AppHelper.killProcess(context, appItem.getPackageName());
    }

    public void uninstall(Activity activity, UninstallListener listener) {
        AppItem.uninstall(activity, this, listener);
    }

    public void uninstall(Activity activity, UninstallListener listener, boolean withFeedback) {
        AppItem.uninstall(activity, this, listener, withFeedback);
    }

    public void clearCache(Context context) {
        AppItem.clearCache(context, this);
    }

    public static void clearCache(Context context, AppItem appItem) {
        final PackageManager packageManager = context.getPackageManager();
        AppHelper.clearCache(packageManager, appItem.getPackageName());
    }

    public void clearData(Context context) {
        AppItem.clearData(context, this);
    }

    public static void clearData(Context context, AppItem appItem) {
        final PackageManager packageManager = context.getPackageManager();
        AppHelper.clearData(packageManager, appItem.getPackageName());
    }

    public static void uninstall(final Activity activity, final AppItem appItem, final UninstallListener listener) {
        uninstall(activity, appItem, listener, true);
    }

    public static void uninstall(final Activity activity, final AppItem appItem,
            final UninstallListener listener, final boolean withFeedback) {
        // try via native package manager api
        AppHelper.uninstallPackage(activity.getPackageManager(), appItem.getPackageName());

        // build our command
        final StringBuilder sb = new StringBuilder();

        if (appItem.isSystemApp()) {
            sb.append(BusyBox.callBusyBoxApplet("mount", "-o rw,remount /system;"));
        }

        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().publicSourceDir));
        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().sourceDir));
        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().dataDir));
        sb.append("sync;");

        if (appItem.isSystemApp()) {
            sb.append(BusyBox.callBusyBoxApplet("mount", "-o ro,remount /system;"));
        }
        sb.append("sync;echo \"Done!\";");

        final String cmd = sb.toString();
        Timber.v("Created command: %s", cmd);
        // create the dialog (will not be shown for a long amount of time though)
        new UninstallAsyncTask(withFeedback, activity, appItem, cmd, listener).execute();
    }

    private static class DisableEnableCommand extends Command {
        private final DisableEnableListener listener;

        DisableEnableCommand(String cmd, DisableEnableListener listener) {
            super(cmd);
            this.listener = listener;
        }

        @Override public void onCommandTerminated(int id, String reason) {
            super.onCommandTerminated(id, reason);
            if (listener != null) {
                listener.OnDisabledOrEnabled();
            }
        }

        @Override public void onCommandCompleted(int id, int exitCode) {
            super.onCommandCompleted(id, exitCode);
            if (listener != null) {
                listener.OnDisabledOrEnabled();
            }
        }
    }

    private static class UninstallAsyncTask extends AsyncTask<Void, Void, Void> {
        private final boolean withFeedback;
        private final WeakReference<Activity> activityReference;
        private final AppItem appItem;
        private final String cmd;
        private final UninstallListener listener;
        private final ProgressDialog dialog;

        UninstallAsyncTask(boolean withFeedback, Activity activity, AppItem appItem, String cmd, UninstallListener listener) {
            this.withFeedback = withFeedback;
            this.activityReference = new WeakReference<>(activity);
            this.appItem = appItem;
            this.cmd = cmd;
            this.listener = listener;

            if (this.withFeedback) {
                this.dialog = new ProgressDialog(activity);
            } else {
                this.dialog = null;
            }
        }

        @Override protected void onPreExecute() {
            if (dialog != null) {
                final Activity activity = activityReference.get();
                if (activity != null) {
                    dialog.setTitle(R.string.uninstalling);
                    dialog.setMessage(activity.getString(R.string.applying_wait));
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        }

        @Override protected Void doInBackground(Void... voids) {
            final String uninstallCmd = String.format("pm uninstall %s;sync;", appItem.getPackageName());
            RootShell.fireAndForget(uninstallCmd);
            RootShell.fireAndForget(cmd);

            // fake delay to make all more smooth
            try {
                Thread.sleep(750);
            } catch (Exception ignored) { }

            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            if (withFeedback) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                final Activity activity = activityReference.get();
                if (activity != null) {
                    Toast.makeText(activity, activity.getString(R.string.uninstall_success, appItem.getLabel()),
                            Toast.LENGTH_SHORT).show();
                }
            }
            if (listener != null) {
                listener.OnUninstallComplete();
            }
        }
    }
}
