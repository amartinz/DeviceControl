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
package org.namelessrom.devicecontrol.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.BootupConfiguration;
import org.namelessrom.devicecontrol.services.BootupService;
import org.namelessrom.devicecontrol.utils.Utils;

public class BootUpReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1000;

    @Override
    public void onReceive(final Context ctx, final Intent intent) {
        if (intent == null) {
            return;
        }

        final String action = intent.getAction();
        if (TextUtils.isEmpty(action)
                || !Intent.ACTION_BOOT_COMPLETED.equals(action)
                || !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }

        Utils.startTaskerService(ctx);

        int size = BootupConfiguration.get(ctx).items.size();
        if (size == 0) {
            Logger.v(this, "No bootup items, not showing notification");
            return;
        }

        Intent i = new Intent(ctx, BootupService.class);
        PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        builder.setContentTitle(ctx.getString(R.string.app_name))
                .setContentText(ctx.getString(R.string.bootup_restoration_content))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_bootup_restore)
                .setColor(ctx.getResources().getColor(R.color.accent_light))
                .setContentIntent(pi)
                .setAutoCancel(true);
        Notification notification = builder.build();

        NotificationManager notificationManager =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

}
