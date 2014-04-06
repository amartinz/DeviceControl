package org.namelessrom.devicecontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by alex on 06.04.14.
 */
public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            Log.wtf("DeviceControl", "ScreenReceiver: Action: " + action);
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.wtf("DeviceControl", "ScreenReceiver: Screen ON");
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.wtf("DeviceControl", "ScreenReceiver: Screen OFF");
            }
        }
    }

}
