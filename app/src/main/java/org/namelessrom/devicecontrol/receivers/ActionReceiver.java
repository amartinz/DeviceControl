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
package org.namelessrom.devicecontrol.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.QuickActionActivity;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.AppHelper;

public class ActionReceiver extends BroadcastReceiver {
    public static final String ACTION = "org.namelessrom.devicecontrol.TRIGGER_ACTION";

    public static final String KEY_ACTION = "key_action";

    public static final int ACTION_DIALOG = 1133701;
    public static final int ACTION_MEDIA_SCAN = 1133702;

    @Override public void onReceive(final Context context, final Intent intent) {
        if (intent == null || !TextUtils.equals(ACTION, intent.getAction())) {
            return;
        }

        final Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        final int action = bundle.getInt(KEY_ACTION, -1);
        switch (action) {
            case ACTION_DIALOG: {
                final Intent i = new Intent(context.getApplicationContext(), QuickActionActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                break;
            }
            case ACTION_MEDIA_SCAN: {
                AppHelper.startMediaScan(null, context);
                break;
            }
        }
    }

    public static class Notification {
        private static int ID_NOTIFICATION = 1891235;

        public static void showNotification(Context applicationContext) {
            final Intent dialogIntent = new Intent(applicationContext, ActionReceiver.class);
            dialogIntent.setAction(ActionReceiver.ACTION);
            dialogIntent.putExtra(ActionReceiver.KEY_ACTION, ActionReceiver.ACTION_DIALOG);
            final PendingIntent dialog = PendingIntent.getBroadcast(applicationContext, 0, dialogIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(applicationContext);
            builder.setContentTitle(applicationContext.getString(R.string.quick_actions))
                    .setContentText(applicationContext.getString(R.string.quick_actions_content))
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setSmallIcon(R.drawable.ic_build_white_24dp)
                    .setColor(AppResources.get().getAccentColor())
                    .setContentIntent(dialog)
                    .setAutoCancel(false);

            final android.app.Notification notification = builder.build();
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            notificationManager.cancel(ID_NOTIFICATION);
            notificationManager.notify(ID_NOTIFICATION, notification);
        }

        public static void cancelNotification(Context applicationContext) {
            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(applicationContext);
            notificationManager.cancel(ID_NOTIFICATION);
        }
    }
}
