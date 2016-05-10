/*
 * Copyright 2014 ParanoidAndroid Project
 * Modifications Copyright (C) 2014 - 2015 Alexander "Evisceration" Martinz
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.modules.flasher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.FlasherConfig;
import org.namelessrom.devicecontrol.modules.flasher.recovery.RecoveryInfo;
import org.namelessrom.devicecontrol.utils.IOUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class RebootHelper {
    private RecoveryHelper mRecoveryHelper;

    public RebootHelper(RecoveryHelper recoveryHelper) {
        mRecoveryHelper = recoveryHelper;
    }

    private void showBackupDialog(final Context context, final String[] items, final boolean wipeData, final boolean wipeCaches) {
        final double spaceLeft = IOUtils.get().getSpaceLeft();
        if (spaceLeft < 1.0) {
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.alert_backup_space_title);
            alert.setMessage(context.getResources().getString(
                    R.string.alert_backup_space_message, 1.0));

            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();

                    reallyShowBackupDialog(context, items, wipeData, wipeCaches);
                }
            });

            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        } else {
            reallyShowBackupDialog(context, items, wipeData, wipeCaches);
        }
    }

    private void reallyShowBackupDialog(final Context context, final String[] items, final boolean wipeData,
            final boolean wipeCaches) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_backup_title);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_backup,
                (ViewGroup) ((Activity) context).findViewById(R.id.backup_dialog_layout));
        alert.setView(view);

        final CheckBox cbSystem = (CheckBox) view.findViewById(R.id.system);
        final CheckBox cbData = (CheckBox) view.findViewById(R.id.data);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cache);
        final CheckBox cbRecovery = (CheckBox) view.findViewById(R.id.recovery);
        final CheckBox cbBoot = (CheckBox) view.findViewById(R.id.boot);
        final CheckBox cbSecure = (CheckBox) view.findViewById(R.id.androidsecure);
        final CheckBox cbSdext = (CheckBox) view.findViewById(R.id.sdext);
        final EditText input = (EditText) view.findViewById(R.id.backupname);

        input.setText(Utils.getDateAndTime());
        input.selectAll();

        if (!IOUtils.get().hasAndroidSecure()) {
            cbSecure.setVisibility(View.GONE);
        }
        if (!IOUtils.get().hasSdExt()) {
            cbSdext.setVisibility(View.GONE);
        }

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                String text = input.getText().toString();
                text = text.replaceAll("[^a-zA-Z0-9.-]", "");

                String backupOptions = "";
                if (cbSystem.isChecked()) {
                    backupOptions += "S";
                }
                if (cbData.isChecked()) {
                    backupOptions += "D";
                }
                if (cbCache.isChecked()) {
                    backupOptions += "C";
                }
                if (cbRecovery.isChecked()) {
                    backupOptions += "R";
                }
                if (cbBoot.isChecked()) {
                    backupOptions += "B";
                }
                if (cbSecure.isChecked()) {
                    backupOptions += "A";
                }
                if (cbSdext.isChecked()) {
                    backupOptions += "E";
                }

                reboot(context, items, wipeData, wipeCaches, text, backupOptions);
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public void showRebootDialog(final Context context, final String[] items, final boolean backup,
            final boolean wipeData, final boolean wipeCaches) {

        if (items == null || items.length == 0) {
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.alert_reboot_install_title);

        if (items.length == 1) {
            alert.setMessage(context.getResources().getString(R.string.alert_reboot_one_message));
        } else {
            alert.setMessage(context.getResources().getString(R.string.alert_reboot_more_message));
        }

        alert.setPositiveButton(R.string.install_now, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (backup) {
                    showBackupDialog(context, items, wipeData, wipeCaches);
                } else {
                    reboot(context, items, wipeData, wipeCaches, null, null);
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void reboot(Context context, String[] items, boolean wipeData, boolean wipeCaches, String folder, String options) {
        final ProgressDialog rebootDialog = showRebootProgressDialog(context);
        rebootDialog.show();

        final int[] recoveries;
        final int flashType = FlasherConfig.get().recoveryType;
        if (FlasherConfig.RECOVERY_TYPE_CWM == flashType) {
            recoveries = new int[]{ RecoveryInfo.CWM_BASED };
        } else if (FlasherConfig.RECOVERY_TYPE_OPEN == flashType) {
            recoveries = new int[]{ RecoveryInfo.TWRP_BASED };
        } else {
            recoveries = new int[]{ RecoveryInfo.CWM_BASED, RecoveryInfo.TWRP_BASED };
        }

        StringBuilder sb;
        for (final int recovery : recoveries) {
            sb = new StringBuilder();
            String file = "/cache/recovery/" + mRecoveryHelper.getCommandsFile(recovery);

            String[] files = new String[items.length];
            for (int i = 0; i < files.length; i++) {
                files[i] = mRecoveryHelper.getRecoveryFilePath(recovery, items[i]);
            }

            final String[] commands = mRecoveryHelper.getCommands(recovery, files, wipeData, wipeCaches, folder, options);
            if (commands != null) {
                for (final String s : commands) {
                    sb.append(s).append("\n");
                }
            }

            final String cmd = String.format("mkdir -p /cache/recovery/;\necho '%s' > '%s';\nsync;\n", sb.toString(), file);
            RootShell.fireAndBlock(cmd);
            Utils.setPermissions(file, "0644", android.os.Process.myUid(), 2001);
        }

        try {
            App.get().getPowerManager().reboot("recovery");
        } catch (Exception exc) {
            Timber.e(exc, "can not reboot using power manager");
            RootShell.fireAndBlock("sync;reboot recovery;\n");
        }
    }

    public static ProgressDialog showRebootProgressDialog(Context context) {
        final ProgressDialog rebootDialog = new ProgressDialog(context);
        rebootDialog.setTitle(R.string.rebooting);
        rebootDialog.setMessage(context.getString(R.string.please_wait));
        rebootDialog.setCancelable(false);
        rebootDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return rebootDialog;
    }
}
