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

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.DeviceConstants;

import java.io.FileOutputStream;

public class FstrimService extends IntentService {
    //==============================================================================================
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    public FstrimService() { super("FstrimService"); }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (action != null) {
                if (action.equals(ACTION_TASKER_FSTRIM)) {
                    new FstrimTask().execute();
                }
            }
        } else {
            stopSelf();
        }
    }

    //================
    // Runnable
    //================
    private class FstrimTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Logger.i(this, "FSTRIM RUNNING");
            try {
                String path = getFilesDir().getAbsolutePath() + DeviceConstants.DC_LOG_FILE_FSTRIM;

                final FileOutputStream fos = new FileOutputStream(path);
                final String sb = "date;\n"
                        + "busybox fstrim -v /system;\n"
                        + "busybox fstrim -v /data;\n"
                        + "busybox fstrim -v /cache;\n";

                final CommandCapture comm = new CommandCapture(0, sb) {

                    @Override
                    public void commandOutput(int id, String line) {
                        Logger.v(this, "Result: " + line);
                        try {
                            fos.write((line + '\n').getBytes());
                        } catch (Exception ignored) { }
                    }

                    @Override
                    public void commandCompleted(int id, int exitcode) {
                        try {
                            fos.write("\n\n".getBytes());
                            fos.flush();
                            fos.close();
                        } catch (Exception ignored) { }
                    }
                };

                RootTools.getShell(true).add(comm);
            } catch (Exception exc) {
                Logger.e(this, "Fstrim error: " + exc.getLocalizedMessage());
            }
            Logger.i(this, "FSTRIM RAN");

            return null;
        }
    }

}
