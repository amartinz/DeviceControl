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
package org.namelessrom.devicecontrol.ui.widgets;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.RemoteViews;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;

public class RebootWidget extends AppWidgetProvider {

    private static final String SHOW_POPUP_DIALOG_REBOOT_ACTION =
            "org.namelessrom.devicecontrol.ui.widgets.showrebootdialog";

    @Override public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] ids) {
        final ComponentName widget = new ComponentName(context, RebootWidget.class);

        final int[] allWidgetInstancesIds = appWidgetManager.getAppWidgetIds(widget);
        for (int widgetId : allWidgetInstancesIds) {
            final RemoteViews remoteViews = new RemoteViews(
                    context.getPackageName(), R.layout.widget_reboot);

            final Intent intent = new Intent(context, RebootWidget.class);
            intent.setAction(SHOW_POPUP_DIALOG_REBOOT_ACTION);

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.widget_reboot_image, pendingIntent);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, ids);
    }

    @Override public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        if (TextUtils.isEmpty(intent.getAction())) return;

        if (intent.getAction().equals(SHOW_POPUP_DIALOG_REBOOT_ACTION)) {
            final Intent popUpIntent = new Intent(context, RebootDialogActivity.class);
            popUpIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(popUpIntent);
        }

        super.onReceive(context, intent);
    }

    public static class RebootDialogActivity extends Activity
            implements DialogInterface.OnClickListener {

        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            final String[] rebootOptions = new String[]{
                    Application.get().getString(R.string.shutdown),
                    Application.get().getString(R.string.reboot),
                    Application.get().getString(R.string.hot_reboot),
                    Application.get().getString(R.string.recovery),
                    Application.get().getString(R.string.bootloader),
            };

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setItems(rebootOptions, this);
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });

            final AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override public void onClick(DialogInterface dialogInterface, int item) {
            switch (item) {
                case 0: // shutdown
                    Utils.runRootCommand("reboot -p");
                    break;
                case 1: // normal
                    Utils.runRootCommand("reboot");
                    break;
                case 2: // "hot" reboot
                    Utils.runRootCommand("pkill -TERM zygote");
                    break;
                case 3: // recovery
                    Utils.runRootCommand("reboot recovery");
                    break;
                case 4: // bootloader
                    Utils.runRootCommand("reboot bootloader");
                    break;
            }

            // close dialog and finish
            dialogInterface.dismiss();
            finish();
        }
    }

}
