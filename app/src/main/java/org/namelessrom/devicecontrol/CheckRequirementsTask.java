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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.RootCheck;
import at.amartinz.hardware.device.Device;

public class CheckRequirementsTask extends AsyncTask<Void, Void, Void> {
    private static final String XPOSED_INSTALLER_PACAKGE = "de.robv.android.xposed.installer";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final MainActivity mainActivity;
    private final boolean skipChecks;

    private final ArrayList<Dialog> dialogs = new ArrayList<>();
    private final ProgressDialog progressDialog;
    private AlertDialog permissionDialog;

    public boolean hasRoot;
    public boolean hasRootGranted;
    private boolean hasBusyBox;
    private String suVersion;

    private Runnable mPostExecuteHook;

    private static final String[] WHITELIST_SU = { "SUPERSU", "CM-SU" };

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
            hasRootGranted = RootCheck.isRootGranted();
            suVersion = RootCheck.getSuVersion(true);
        }
        hasBusyBox = BusyBox.isAvailable(true);

        return null;
    }

    @Override protected void onPostExecute(Void result) {
        // actually skip that stuff
        if (skipChecks) {
            letsGetItStarted();
            return;
        }

        mHandler.removeCallbacks(showDialogRunnable);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        final DeviceConfig deviceConfig = DeviceConfig.get();

        // if we have root and got root granted ...
        if (hasRoot && hasRootGranted) {
            // ... check if we have busybox and throw a warning, if there is no busybox
            if (!hasBusyBox && !deviceConfig.ignoreDialogWarningBusyBox) {
                dialogs.add(buildBusyBoxDialog());
            }

            boolean showSuWarning = true;
            if (!TextUtils.isEmpty(suVersion) && !"-".equals(suVersion)) {
                final String suVersionCompare = suVersion.toUpperCase();
                for (final String whitelist : WHITELIST_SU) {
                    if (suVersionCompare.contains(whitelist)) {
                        showSuWarning = false;
                    }
                }
            }

            if (showSuWarning) {
                if (!deviceConfig.ignoreDialogWarningSuVersion) {
                    dialogs.add(buildSuVersionWarning(suVersion));
                }
            }
        } else {
            // ... else show the root warning dialog
            if (!deviceConfig.ignoreDialogWarningRoot) {
                dialogs.add(buildRootDialog());
            }
        }

        letsGetItStarted();
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

    private void letsGetItStarted() {
        Utils.startTaskerService(mainActivity);

        DeviceConfig deviceConfig = DeviceConfig.get();
        if (deviceConfig.dcFirstStart) {
            deviceConfig.dcFirstStart = false;
            deviceConfig.save();

            final boolean isXposedInstalled = AppHelper.isPackageInstalled(XPOSED_INSTALLER_PACAKGE);
            final CustomEvent customEvent = new CustomEvent("first_start");
            customEvent.putCustomAttribute("xposed_installed", isXposedInstalled ? "true" : "false");
            Answers.getInstance().logCustom(customEvent);
        }

        // patch sepolicy
        if (hasRootGranted) {
            Utils.patchSEPolicy(mainActivity);
        }

        for (final Dialog dialog : dialogs) {
            if (dialog != null) {
                dialog.show();
            }
        }

        permissionDialog = showPermissionDialog(mainActivity);
        if (permissionDialog == null && mPostExecuteHook != null) {
            mainActivity.runOnUiThread(mPostExecuteHook);
        }
    }

    private AlertDialog buildBusyBoxDialog() {
        final String statusText = getString(R.string.warning_busybox) + "\n\n" + getString(R.string.warning_busybox_note);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.missing_requirements);
        builder.setCancelable(false);
        builder.setMessage(statusText);
        builder.setNegativeButton(R.string.dialog_action_never_show_again, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final DeviceConfig deviceConfig = DeviceConfig.get();
                deviceConfig.ignoreDialogWarningBusyBox = true;
                deviceConfig.save();

                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.ignore, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.get_busybox, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                BusyBox.offerBusyBox(mainActivity);
            }
        });
        return builder.create();
    }

    private AlertDialog buildRootDialog() {
        final String statusText = getString(R.string.warning_root) + "\n\n" + getString(R.string.warning_root_note);

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.missing_requirements);
        builder.setCancelable(false);
        builder.setMessage(statusText);
        builder.setNegativeButton(R.string.dialog_action_never_show_again, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final DeviceConfig deviceConfig = DeviceConfig.get();
                deviceConfig.ignoreDialogWarningRoot = true;
                deviceConfig.save();

                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.more_information, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final String url = String.format("https://www.google.com/#q=how+to+root+%s", Device.get(mainActivity).model);
                ((App) mainActivity.getApplicationContext()).getCustomTabsHelper().launchUrl(mainActivity, url);
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private AlertDialog buildSuVersionWarning(String suVersion) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle(R.string.dialog_warning);
        builder.setMessage(getString(R.string.dialog_warning_su_version, suVersion));
        builder.setNegativeButton(R.string.dialog_action_never_show_again, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                final DeviceConfig deviceConfig = DeviceConfig.get();
                deviceConfig.ignoreDialogWarningSuVersion = true;
                deviceConfig.save();

                dialog.dismiss();
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    private AlertDialog showPermissionDialog(final Context context) {
        // TODO: new wizard, more user friendly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final boolean storage = mainActivity.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    && mainActivity.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            final boolean telephony = mainActivity.isGranted(Manifest.permission.READ_PHONE_STATE);
            final boolean location = mainActivity.isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
                                     && mainActivity.isGranted(Manifest.permission.ACCESS_FINE_LOCATION);

            boolean needsPermissionGrant = !storage || !telephony || !location;
            if (needsPermissionGrant) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.dialog_permission_title);
                builder.setMessage(R.string.dialog_permission_summary);
                builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        final ArrayList<String> toRequest = new ArrayList<>();
                        if (!storage) {
                            // we only launch this code on M anyways, but please shut up android studio
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                toRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                            toRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        }

                        if (!telephony) {
                            toRequest.add(Manifest.permission.READ_PHONE_STATE);
                        }

                        if (!location) {
                            toRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                            toRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
                        }

                        mainActivity.requestPermissions(toRequest);

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
        if (mainActivity != null) {
            mainActivity.runOnUiThread(mDestroyRunnable);
        } else {
            mDestroyRunnable.run();
        }
    }

    private final Runnable mDestroyRunnable = new Runnable() {
        @Override public void run() {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if (permissionDialog != null) {
                permissionDialog.dismiss();
            }

            final Iterator<Dialog> iterator = dialogs.iterator();
            while (iterator.hasNext()) {
                final Dialog dialog = iterator.next();
                if (dialog != null) {
                    dialog.dismiss();
                }
                iterator.remove();
            }
        }
    };
}
