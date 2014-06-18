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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.server.ServerStoppedEvent;
import org.namelessrom.devicecontrol.events.server.ServerStoppingEvent;
import org.namelessrom.devicecontrol.net.NetworkInfo;
import org.namelessrom.devicecontrol.net.ServerWrapper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import static org.namelessrom.devicecontrol.Application.getStr;
import static org.namelessrom.devicecontrol.Application.logDebug;

public class WebServerService extends Service {

    public static final int NOTIFICATION_ONGOING = 7861;

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";

    private ServerWrapper mServerWrapper;

    @Override public IBinder onBind(final Intent intent) { return new WebServerBinder(); }

    @Override public void onDestroy() {
        BusProvider.getBus().post(new ServerStoppedEvent());
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
                    : PreferenceHelper.getString("wfn_port", "8080"));
            text = getString(R.string.stop_wfm,
                    "http://" + NetworkInfo.getAnyIpAddress() + ":" + port);
        } else {
            text = getStr(R.string.starting_wfm);
        }

        final Intent intent = new Intent(this, WebServerService.class);
        intent.setAction(ACTION_STOP);
        final PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Notification.Builder builder = new Notification.Builder(this);
            final Notification.InboxStyle inboxStyle = new Notification.InboxStyle(builder);
            inboxStyle.setBigContentTitle(title);
            final String[] split = text.split("\n");
            for (final String s : split) inboxStyle.addLine(s);
            return builder
                    .setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_general_wifi)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        } else {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            final NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle(builder);
            inboxStyle.setBigContentTitle(title);
            final String[] split = text.split("\n");
            for (final String s : split) inboxStyle.addLine(s);
            return builder
                    .setContentTitle(title)
                    .setContentText(text)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_general_wifi)
                    .setStyle(inboxStyle)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
        }
    }

    public void setNotification(Notification notification) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notification == null) notification = getNotification();
        notificationManager.notify(NOTIFICATION_ONGOING, notification);
    }

    public void cancelNotification(final int notificationId) {
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @Override public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (intent == null || intent.getAction() == null || intent.getAction().isEmpty()) {
            logDebug("intent or action is null or empty!");
            stopServer();
            return START_NOT_STICKY;
        }
        final String action = intent.getAction();
        logDebug("action: " + action);

        if (ACTION_START.equals(action)) {
            logDebug("creating server!");
            mServerWrapper = new ServerWrapper(this);
            mServerWrapper.createServer();
        } else {
            logDebug("stopping service!");
            BusProvider.getBus().post(new ServerStoppingEvent());
            stopServer();
        }
        return START_NOT_STICKY;
    }

    private void stopServer() {
        if (mServerWrapper != null) {
            mServerWrapper.stopServer();
            mServerWrapper = null;
        }
        stopForeground(true);
        stopSelf();
    }

    public String getWebServerLog() {
        if (mServerWrapper == null || mServerWrapper.mStringBuilder == null) return "";
        final String log = mServerWrapper.mStringBuilder.toString();              // save
        if (!log.isEmpty()) mServerWrapper.mStringBuilder = new StringBuilder();  // clear
        return log;                                                               // return
    }

    public AsyncHttpServer getServer() {
        if (mServerWrapper != null) {
            return mServerWrapper.getServer();
        } else {
            return null;
        }
    }

    public AsyncServerSocket getServerSocket() {
        if (mServerWrapper != null) {
            return mServerWrapper.getServerSocket();
        } else {
            return null;
        }
    }

    public class WebServerBinder extends Binder {
        public WebServerService getService() { return WebServerService.this; }
    }

}
