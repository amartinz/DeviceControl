package org.namelessrom.devicecontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.utils.ActionProcessor;

import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class ScreenReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        final String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            logDebug("ScreenReceiver: Action: " + action);
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                new Worker().execute(TaskerItem.CATEGORY_SCREEN_ON);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                new Worker().execute(TaskerItem.CATEGORY_SCREEN_OFF);
            }
        }
    }

    class Worker extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            final String category = params[0];
            logDebug("ScreenReceiver: " + category);

            final DatabaseHandler db = DatabaseHandler.getInstance(mContext);
            final List<TaskerItem> itemList = db.getAllTaskerItems(category);

            String name, value;
            boolean enabled;
            for (final TaskerItem item : itemList) {
                name = item.getName();
                value = item.getValue();
                enabled = item.getEnabled();
                logDebug("Processing: " + name + " | " + value + " | " + (enabled ? "1" : "0"));
                if (enabled) {
                    ActionProcessor.processAction(name, value);
                }
            }

            return null;
        }
    }

}
