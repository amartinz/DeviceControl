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
package org.namelessrom.devicecontrol.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.koushikdutta.async.AsyncServerSocket;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.WebServerConfig;
import org.namelessrom.devicecontrol.net.NetworkInfo;
import org.namelessrom.devicecontrol.net.ServerWrapper;
import org.namelessrom.devicecontrol.theme.AppResources;

import timber.log.Timber;

public class WebServerService extends Service {
    public static final int NOTIFICATION_ONGOING = 7861;

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";

    private ServerWrapper mServerWrapper;

    @Override public IBinder onBind(final Intent intent) { return null; }

    @Override public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    @Override public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ONGOING, getNotification());
    }

    private Notification getNotification() {
        final String title = getString(R.string.wireless_file_manager);
        final String text;
        if (mServerWrapper != null) {
            final String port = ((getServerSocket() != null)
                    ? String.valueOf(getServerSocket().getLocalPort())
                    : String.valueOf(WebServerConfig.get().port));
            text = getString(R.string.web_server_running,
                    "http://" + NetworkInfo.getAnyIpAddress() + ":" + port);
        } else {
            text = getString(R.string.web_server_not_running);
        }

        final Intent intent = new Intent(this, WebServerService.class);
        intent.setAction(ACTION_STOP);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_wifi_tethering_white_24dp)
                .setColor(AppResources.get().getAccentColor())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        addNotificationStopButton(builder);
        return builder.build();
    }

    private void addNotificationStopButton(final NotificationCompat.Builder builder) {
        final Intent stop = new Intent(this, WebServerService.class);
        stop.setAction(WebServerService.ACTION_STOP);
        final PendingIntent stopIntent = PendingIntent.getService(this, 0, stop, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_close_black_24dp, getString(R.string.stop), stopIntent);
    }

    public void setNotification(Notification notification) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notification == null) {
            notification = getNotification();
        }
        notificationManager.notify(NOTIFICATION_ONGOING, notification);
    }

    public void cancelNotification() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ONGOING);
    }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null || intent.getAction() == null || intent.getAction().isEmpty()) {
            stopServer();
            return START_NOT_STICKY;
        }
        final String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            Timber.v("creating server!");
            mServerWrapper = new ServerWrapper(this);
            mServerWrapper.createServer();
        } else {
            Timber.v("stopping service!");
            stopServer();
        }
        return START_NOT_STICKY;
    }

    private void stopServer() {
        if (mServerWrapper != null) {
            mServerWrapper.stopServer();
            mServerWrapper = null;
        }
        cancelNotification();
        stopForeground(true);
        stopSelf();
    }

    public AsyncServerSocket getServerSocket() {
        if (mServerWrapper != null) {
            return mServerWrapper.getServerSocket();
        } else {
            return null;
        }
    }

}
