package org.namelessrom.devicecontrol.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import org.namelessrom.devicecontrol.receivers.ScreenReceiver;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class TaskerService extends Service {

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP  = "action_stop";

    private ScreenReceiver mScreenReceiver = null;

    public TaskerService() { }

    @Override
    public IBinder onBind(final Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = "";
        if (intent != null) {
            action = intent.getAction();
        }
        if (action == null || action.isEmpty() || action.equals(ACTION_STOP)) {
            logDebug("Stopping TaskerService");
            stopSelf();
            return START_NOT_STICKY;
        }

        logDebug("TaskerService: Action: " + action);

        if (action.equals(ACTION_START)) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            if (mScreenReceiver == null) {
                mScreenReceiver = new ScreenReceiver();
                registerReceiver(mScreenReceiver, filter);
                logDebug("TaskerService: Starting Service");
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
