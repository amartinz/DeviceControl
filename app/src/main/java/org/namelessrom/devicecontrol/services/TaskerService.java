/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
import android.os.IBinder;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;

import java.io.FileOutputStream;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class TaskerService extends Service implements DeviceConstants, FileConstants {

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {

        PreferenceHelper.getInstance(this);

        final String action = intent.getAction();

        if (action != null) {
            if (action.equals(ACTION_TASKER_FSTRIM)) {
                mFstrimThread.run();
            }
        }

        // Done, staaaahp it
        stopSelf();

        return START_NOT_STICKY;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    //

    //================
    // Runnable
    //================
    private final Thread mFstrimThread = new Thread(new Runnable() {
        public void run() {
            logDebug("FSTRIM RUNNING");
            try {
                final FileOutputStream fos = new FileOutputStream(DC_LOG_FILE_FSSTRIM);
                final String sb = "date;\n"
                        + "busybox fstrim -v /system;\n"
                        + "busybox fstrim -v /data;\n"
                        + "busybox fstrim -v /cache;\n";

                final CommandCapture comm = new CommandCapture(0, false, sb) {

                    @Override
                    public void commandOutput(int id, String line) {
                        logDebug("Result: " + line);
                        try {
                            fos.write((line + "\n").getBytes());
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

                final Shell shell = RootTools.getShell(true);
                shell.add(comm);
            } catch (Exception exc) {
                logDebug("Fstrim error: " + exc.getLocalizedMessage());
            }
            logDebug("FSTRIM RAN");
        }
    });

}
