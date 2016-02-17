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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;

import java.util.List;

import timber.log.Timber;

public class ScreenReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && !action.isEmpty()) {
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                new Worker().execute(ActionProcessor.TRIGGER_SCREEN_ON);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                new Worker().execute(ActionProcessor.TRIGGER_SCREEN_OFF);
            }
        }
    }

    private static class Worker extends AsyncTask<String, Void, Void> {
        @Override protected Void doInBackground(String... params) {
            final String trigger = params[0];
            Timber.v("Trigger: %s", trigger);

            final List<TaskerItem> itemList = TaskerConfig.get().getItemsByTrigger(trigger);

            Timber.v("Items: %s", itemList.size());

            for (final TaskerItem item : itemList) {
                Timber.v("Processing: %s | %s | %s", item.name, item.value, item.enabled);
                if (item.enabled) {
                    ActionProcessor.getProcessAction(item.name, item.value, false);
                }
            }

            return null;
        }
    }

}
