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

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.tbruyelle.rxpermissions.RxPermissions;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.execution.RootCheck;
import alexander.martinz.libs.execution.binaries.BusyBox;

public class CheckRequirementsTask extends AsyncTask<Void, Void, Void> {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final MainActivity mainActivity;
    private final boolean skipChecks;

    private final ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AlertDialog permissionDialog;

    private boolean hasRoot;
    private boolean hasBusyBox;
    private String suVersion;

    private Runnable mPostExecuteHook;

    public CheckRequirementsTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        skipChecks = DeviceConfig.get().skipChecks;

        if (!skipChecks) {
            progressDialog = new ProgressDialog(mainActivity);
            progressDialog.setTitle(R.string.checking_requirements);
            progressDialog.setMessage(getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog = null;
        }
    }

    public CheckRequirementsTask setPostExecuteHook(Runnable postExecuteHook) {
        mPostExecuteHook = postExecuteHook;
        return this;
    }

    @Override protected void onPreExecute() {
        if (!skipChecks) {
            // if check takes longer than 200ms, show progress dialog
            mHandler.postDelayed(showDialogRunnable, 200);
        }
    }

    @Override protected Void doInBackground(Void... params) {
        if (skipChecks) {
            return null;
        }

        hasRoot = RootCheck.isRooted(true);
        if (hasRoot) {
            suVersion = RootCheck.getSuVersion(true);
        }
        hasBusyBox = BusyBox.isAvailable(true);

        return null;
    }

    @Override protected void onPostExecute(Void result) {
        // actually skip that stuff
        if (skipChecks) {
            letsGetItStarted(mainActivity);
            return;
        }

        mHandler.removeCallbacks(showDialogRunnable);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (hasRoot && hasBusyBox) {
            boolean showSuWarning = false;
            if (!TextUtils.isEmpty(suVersion) && !"-".equals(suVersion)) {
                if (!suVersion.toUpperCase().contains("SUPERSU")) {
                    final DeviceConfig deviceConfig = DeviceConfig.get();
                    if (!deviceConfig.ignoreDialogWarningSuVersion) {
                        showSuWarning = true;
                    }
                }
            }

            if (showSuWarning) {
                alertDialog = showSuVersionWarning(mainActivity, suVersion);
                alertDialog.show();
                return;
            }

            letsGetItStarted(mainActivity);
            return;
        }

        alertDialog = buildRequirementsDialog(hasRoot);
        alertDialog.show();
    }

    private String getString(@StringRes final int resId) {
        return mainActivity.getString(resId);
    }

    private String getString(@StringRes final int resId, Object... objects) {
        return mainActivity.getString(resId, objects);
    }

    private final Runnable showDialogRunnable = new Runnable() {
        @Override public void run() {
            if (progressDialog != null) {
                progressDialog.show();
            }
        }
    };

    private void letsGetItStarted(MainActivity mainActivity) {
        Utils.startTaskerService(mainActivity);

        DeviceConfig deviceConfig = DeviceConfig.get();
        if (deviceConfig.dcFirstStart) {
            deviceConfig.dcFirstStart = false;
            deviceConfig.save();
        }

        // patch sepolicy
        Utils.patchSEPolicy(mainActivity);

        permissionDialog = showPermissionDialog(mainActivity);
        if (permissionDialog == null && mPostExecuteHook != null) {
            mainActivity.runOnUiThread(mPostExecuteHook);
        }
    }

    private AlertDialog buildRequirementsDialog(final boolean hasRoot) {
        final String statusText;
        final String actionText;
        if (!hasRoot) {
            statusText = getString(R.string.app_warning_root, getString(R.string.app_name));
            actionText = getString(R.string.more_information);
        } else {
            statusText = getString(R.string.app_warning_busybox, getString(R.string.app_name)) + "\n\n"
                         + getString(R.string.app_warning_busybox_note);
            actionText = getString(R.string.get_busybox);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.missing_requirements);
        builder.setCancelable(false);
        builder.setMessage(statusText);
        builder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                mainActivity.finish();
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(actionText, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (!hasRoot) {
                    String url = String.format("https://www.google.com/#q=how+to+root+%s", Device.get(mainActivity).model);
                    ((Application) mainActivity.getApplicationContext()).getCustomTabsHelper().launchUrl(mainActivity, url);
                } else {
                    BusyBox.offerBusyBox(mainActivity);
                }
            }
        });
        return builder.create();
    }

    private AlertDialog showSuVersionWarning(final MainActivity mainActivity, String suVersion) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.dialog_warning);
        builder.setMessage(getString(R.string.dialog_warning_su_version, suVersion));
        builder.setNegativeButton(R.string.dialog_action_never_show_again, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final DeviceConfig deviceConfig = DeviceConfig.get();
                deviceConfig.ignoreDialogWarningSuVersion = true;
                deviceConfig.save();

                dialog.dismiss();
                letsGetItStarted(mainActivity);
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                letsGetItStarted(mainActivity);
            }
        });
        return builder.create();
    }

    private AlertDialog showPermissionDialog(final Context context) {
        // TODO: new wizard, more user friendly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final RxPermissions rxPermissions = RxPermissions.getInstance(context);

            final boolean storage = rxPermissions.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    && rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            final boolean telephony = rxPermissions.isGranted(Manifest.permission.READ_PHONE_STATE);
            final boolean location = rxPermissions.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                                     && rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION);

            boolean needsPermissionGrant = !storage || !telephony || !location;
            if (needsPermissionGrant) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.dialog_permission_title);
                builder.setMessage(R.string.dialog_permission_summary);
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        if (!storage) {
                            rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe();
                        }

                        if (!telephony) {
                            rxPermissions.request(Manifest.permission.READ_PHONE_STATE).subscribe();
                        }

                        if (!location) {
                            rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION).subscribe();
                        }

                        if (mPostExecuteHook != null) {
                            mainActivity.runOnUiThread(mPostExecuteHook);
                        }
                    }
                });
                return builder.show();
            }
        }
        return null;
    }

    public void destroy() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        if (permissionDialog != null) {
            permissionDialog.dismiss();
        }
    }
}
