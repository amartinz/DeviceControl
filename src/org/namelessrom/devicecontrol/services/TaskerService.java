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

import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Created by alex on 11.11.13.
 */
public class TaskerService extends Service implements DeviceConstants, FileConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private boolean mDebug = false;

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
        mDebug = PreferenceHelper.getBoolean(JF_EXTENSIVE_LOGGING);

        String action = intent.getAction();

        if (action != null) {
            if (action.equals(ACTION_TASKER_FSTRIM)) {
                mFstrimRunnable.run();
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
    private final Runnable mFstrimRunnable = new Runnable() {
        public void run() {
            List<String> mResults;
            logDebug("FSTRIM RUNNING");
            FileOutputStream fos = null;
            try {

                List<String> mCommands = new ArrayList<String>();
                mCommands.add("date\n");
                mCommands.add("busybox fstrim -v /system\n");
                mCommands.add("busybox fstrim -v /data\n");
                mCommands.add("busybox fstrim -v /cache\n");

                mResults = Shell.SU.run(mCommands);

                if (mResults != null) {
                    fos = new FileOutputStream(DC_LOG_FILE_FSSTRIM);
                    for (String s : mResults) {
                        logDebug("Result: " + s);
                        fos.write((s + "\n").getBytes());
                    }
                    fos.write("\n\n".getBytes());
                }
            } catch (Exception exc) {
                logDebug("Fstrim error: " + exc.getLocalizedMessage());
            } finally {
                try {
                    if (fos != null) {
                        fos.flush();
                        fos.close();
                    }
                } catch (Exception exc) {
                    logDebug("Fstrim error: " + exc.getLocalizedMessage());
                }
            }
            logDebug("FSTRIM RAN");
        }
    };

}
