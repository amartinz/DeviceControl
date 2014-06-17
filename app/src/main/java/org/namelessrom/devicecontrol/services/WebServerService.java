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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.koushikdutta.async.AsyncServerSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;

import org.namelessrom.devicecontrol.net.ServerWrapper;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class WebServerService extends Service {

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";

    private ServerWrapper mServerWrapper;

    @Override public IBinder onBind(final Intent intent) { return new WebServerBinder(); }

    @Override public void onDestroy() {
        super.onDestroy();
        stopServer();
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
            mServerWrapper = new ServerWrapper();
            mServerWrapper.createServer();
        } else {
            logDebug("stopping service!");
            stopServer();
        }
        return START_NOT_STICKY;
    }

    private void stopServer() {
        if (mServerWrapper != null) mServerWrapper.stopServer();
        stopSelf();
    }

    public String getWebServerLog() {
        if (mServerWrapper == null || mServerWrapper.mStringBuilder == null) return "";
        final String log = mServerWrapper.mStringBuilder.toString();              // save
        if (!log.isEmpty()) mServerWrapper.mStringBuilder = new StringBuilder();  // clear
        return log;                                                               // return
    }

    public AsyncHttpServer getServer() { return mServerWrapper.getServer(); }

    public AsyncServerSocket getServerSocket() { return mServerWrapper.getServerSocket(); }

    public class WebServerBinder extends Binder {
        public WebServerService getService() { return WebServerService.this; }
    }

}
