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
import android.content.IntentFilter;
import android.os.IBinder;

import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.receivers.ScreenReceiver;

import timber.log.Timber;

public class TaskerService extends Service {

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";

    private ScreenReceiver mScreenReceiver = null;

    public TaskerService() { }

    @Override
    public IBinder onBind(final Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!TaskerConfig.get().enabled) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String action = "";
        if (intent != null) {
            action = intent.getAction();
        }
        if (action == null || action.isEmpty() || action.equals(ACTION_STOP)) {
            Timber.v("Stopping TaskerService");
            stopSelf();
            return START_NOT_STICKY;
        }

        if (action.equals(ACTION_START)) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            if (mScreenReceiver == null) {
                mScreenReceiver = new ScreenReceiver();
                registerReceiver(mScreenReceiver, filter);
                Timber.v("Starting TaskerService");
            }
            return START_STICKY;
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mScreenReceiver != null) { unregisterReceiver(mScreenReceiver); }
        } catch (Exception ignored) { }
        mScreenReceiver = null;
    }
}
