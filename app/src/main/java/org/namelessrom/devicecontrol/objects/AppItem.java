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
package org.namelessrom.devicecontrol.objects;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.Toast;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;

public class AppItem {
    private final PackageInfo pkgInfo;
    private final ApplicationInfo appInfo;

    private final String label;
    private final Drawable icon;

    private boolean enabled = false;

    public interface UninstallListener {
        void OnUninstallComplete();
    }

    public AppItem(final PackageInfo info, final String label, final Drawable icon) {
        this.pkgInfo = info;
        this.appInfo = info.applicationInfo;

        this.label = label;
        this.icon = icon;

        this.enabled = (appInfo != null && appInfo.enabled);
    }

    public String getLabel() { return label; }

    public Drawable getIcon() { return icon; }

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

    public Toast launchActivity(Activity activity) {
        return AppItem.launchActivity(activity, this);
    }

    public static Toast launchActivity(Activity activity, AppItem appItem) {
        Intent i = activity.getPackageManager().getLaunchIntentForPackage(appItem.getPackageName());
        if (i != null) {
            try {
                activity.startActivity(i);
                return null;
            } catch (ActivityNotFoundException anfe) {
                Logger.e(appItem.getPackageName(), "Could not launch activity", anfe);
            }
        }

        return Toast.makeText(activity, R.string.could_not_launch_activity, Toast.LENGTH_SHORT);
    }

    public void uninstall(Activity activity, UninstallListener listener) {
        AppItem.uninstall(activity, this, listener);
    }

    public static void uninstall(final Activity activity, final AppItem appItem,
            final UninstallListener listener) {
        // try via native package manager api
        AppHelper.uninstallPackage(activity.getPackageManager(), appItem.getPackageName());

        // build our command
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("pm uninstall %s;", appItem.getPackageName()));

        if (appItem.isSystemApp()) {
            sb.append("busybox mount -o rw,remount /system;");
        }

        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().publicSourceDir));
        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().sourceDir));
        sb.append(String.format("rm -rf %s;", appItem.getApplicationInfo().dataDir));

        if (appItem.isSystemApp()) {
            sb.append("busybox mount -o ro,remount /system;");
        }

        final String cmd = sb.toString();
        Logger.v(appItem.getPackageName(), cmd);

        // create the dialog (will not be shown for a long amount of time though)
        final ProgressDialog dialog;
        dialog = new ProgressDialog(activity);
        dialog.setTitle(R.string.uninstalling);
        dialog.setMessage(activity.getString(R.string.applying_wait));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        new AsyncTask<Void, Void, Void>() {
            @Override protected void onPreExecute() {
                dialog.show();
            }

            @Override protected Void doInBackground(Void... voids) {
                Utils.runRootCommand(cmd, true);
                return null;
            }

            @Override protected void onPostExecute(Void aVoid) {
                dialog.dismiss();
                Toast.makeText(activity,
                        activity.getString(R.string.uninstall_success, appItem.getLabel()),
                        Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.OnUninstallComplete();
                }
            }
        }.execute();
    }

}
