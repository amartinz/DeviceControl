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
package org.namelessrom.devicecontrol.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.flasher.RebootHelper;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

import at.amartinz.execution.RootShell;

public class RebootWidget extends AppWidgetProvider {

    private static final String SHOW_POPUP_DIALOG_REBOOT_ACTION =
            "org.namelessrom.devicecontrol.ui.widgets.showrebootdialog";

    @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ids) {
        final ComponentName widget = new ComponentName(context, RebootWidget.class);

        final int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(widget);
        for (int widgetId : allWidgetInstancesIds) {
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_reboot);

            final Intent intent = new Intent(context, RebootWidget.class);
            intent.setAction(SHOW_POPUP_DIALOG_REBOOT_ACTION);

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_reboot_image, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, ids);
    }

    @Override public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) { return; }

        if (intent.getAction().equals(SHOW_POPUP_DIALOG_REBOOT_ACTION)) {
            final Intent popUpIntent = new Intent(context, RebootDialogActivity.class);
            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popUpIntent);
        }

        super.onReceive(context, intent);
    }

    public static class RebootDialogActivity extends Activity implements DialogInterface.OnClickListener {
        private AlertDialog chooserDialog;
        private ProgressDialog rebootDialog;

        @Override protected void onDestroy() {
            if (chooserDialog != null) {
                chooserDialog.dismiss();
            }
            if (rebootDialog != null) {
                rebootDialog.dismiss();
            }
            super.onDestroy();
        }

        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final String[] rebootOptions = new String[]{
                    App.get().getString(R.string.shutdown),
                    App.get().getString(R.string.reboot),
                    App.get().getString(R.string.hot_reboot),
                    App.get().getString(R.string.recovery),
                    App.get().getString(R.string.bootloader),
            };

            final Drawable powerDrawable = ContextCompat.getDrawable(this, R.drawable.ic_power_settings_new_white_24dp).mutate();
            final int powerColor = ContextCompat.getColor(this, R.color.accent_light);
            DrawableHelper.applyColorFilter(powerDrawable, powerColor);

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setIcon(powerDrawable);
            builder.setTitle(R.string.widget_power);
            builder.setItems(rebootOptions, this);
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            });

            chooserDialog = builder.create();
            chooserDialog.show();
        }

        @Override public void onClick(DialogInterface dialogInterface, int item) {
            final String cmd;
            switch (item) {
                case 0: // shutdown
                    cmd = "reboot -p";
                    break;
                case 1: // normal
                    cmd = "reboot";
                    break;
                case 2: // "hot" reboot
                    cmd = "pkill -TERM zygote";
                    break;
                case 3: // recovery
                    cmd = "reboot recovery";
                    break;
                case 4: // bootloader
                    cmd = "reboot bootloader";
                    break;
                default: // not handled
                    cmd = "";
                    break;
            }
            dialogInterface.dismiss();

            if (!TextUtils.isEmpty(cmd)) {
                final String rebootCmd = String.format("sync;%s;", cmd);
                showRebootDialog(rebootCmd);
                return;
            }

            // close dialog and finish
            finish();
        }

        private void showRebootDialog(final String rebootCmd) {
            rebootDialog = RebootHelper.showRebootProgressDialog(this);

            new AsyncTask<Void, Void, Void>() {
                @Override protected void onPreExecute() {
                    rebootDialog.show();
                }

                @Override protected Void doInBackground(Void... params) {
                    RootShell.fireAndBlock(rebootCmd);
                    return null;
                }

                @Override protected void onPostExecute(Void aVoid) {
                    rebootDialog.dismiss();
                    finish();
                }
            }.execute();
        }
    }

}
