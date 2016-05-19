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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.sense360.android.quinoa.lib.Sense360;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.services.BootupService;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.thirdparty.Sense360Impl;
import org.namelessrom.devicecontrol.utils.Utils;

import io.paperdb.Paper;
import timber.log.Timber;

public class BootUpReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1000;

    @Override public void onReceive(final Context ctx, final Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            AsyncTask.execute(new Runnable() {
                @Override public void run() {
                    startBootupStuffsAsync(ctx);
                }
            });
        }
    }

    private void startBootupStuffsAsync(Context ctx) {
        Paper.init(ctx);
        Utils.startTaskerService(ctx);

        Sense360Impl.init(ctx.getApplicationContext());

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.getBoolean(ctx.getString(R.string.key_quick_actions), false)) {
            ActionReceiver.Notification.showNotification(ctx.getApplicationContext());
        }

        BootupConfig bootupConfig = BootupConfig.get();
        boolean isBootup = bootupConfig.isEnabled;
        if (!isBootup) {
            Timber.v("User does not want to restore settings on bootup");
            return;
        }

        int size = bootupConfig.items.size();
        if (size == 0) {
            Timber.v("No bootup items, not starting bootup restoration");
            return;
        }

        final Intent bootupRestorationIntent = new Intent(ctx, BootupService.class);
        if (bootupConfig.isAutomatedRestoration) {
            ctx.startService(bootupRestorationIntent);
            Timber.v("Starting automated bootup restoration");
            return;
        }

        final PendingIntent pi = PendingIntent.getService(ctx, 0, bootupRestorationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        final String title = ctx.getString(R.string.app_name);
        final String content = ctx.getString(R.string.bootup_restoration_content);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx);
        final NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(content);

        builder.setContentTitle(title)
                .setContentText(content)
                .setStyle(style)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_settings_backup_restore_black_24dp)
                .setColor(AppResources.get().getAccentColor())
                .setContentIntent(pi)
                .setAutoCancel(true);
        final Notification notification = builder.build();

        final NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
