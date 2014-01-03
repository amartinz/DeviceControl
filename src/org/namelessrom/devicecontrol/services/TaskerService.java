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

import org.namelessrom.devicecontrol.utils.DeviceConstants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

/**
 * Created by alex on 11.11.13.
 */
public class TaskerService extends Service implements DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private boolean mDebug = false;
    private boolean mShouldRun = false;
    private boolean mFstrim = false;

    //==============================================================================================
    // Scheduler
    //==============================================================================================
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private ScheduledFuture fstrimHandle;

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

        update();

        if (mFstrim) {
            scheduleFstrim();
        }

        if (!mShouldRun) {
            stopSelf();
        }

        return START_STICKY;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void update() {
        mFstrim = PreferenceHelper.getBoolean(TASKER_TOOLS_FSTRIM);
        logDebug("mFstrim: " + (mFstrim ? "true" : "false"));

        // Set Flag to kill service if no tasker action is used
        mShouldRun = mFstrim;
        logDebug("mShouldRun: " + (mShouldRun ? "true" : "false"));
    }

    private void scheduleFstrim() {
        if (fstrimHandle != null) {
            fstrimHandle.cancel(true);
        }
        long period = Integer.parseInt(PreferenceHelper.getString(
                TASKER_TOOLS_FSTRIM_INTERVAL, "30"));
        fstrimHandle = scheduler.scheduleAtFixedRate(mFstrimRunnable, 1, period, TimeUnit.MINUTES);
        logDebug("Fstrim scheduled every " + period + " Minutes.");
    }

    private void logDebug(String msg) {
        Utils.logDebug(msg, mDebug);
    }

    //================
    // Runnable
    //================
    private final Runnable mFstrimRunnable = new Runnable() {
        public void run() {
            List<String> mResults = null;
            logDebug("FSTRIM RUNNING");
            FileOutputStream fos = null;
            try {

                List<String> mCommands = new ArrayList<String>();
                mCommands.add("date\n");
                mCommands.add("busybox fstrim -v /system\n");
                mCommands.add("busybox fstrim -v /data\n");
                mCommands.add("busybox fstrim -v /cache\n");

                if (Application.IS_SYSTEM_APP) {
                    mResults = Shell.SH.run(mCommands);
                } else {
                    if (Application.HAS_ROOT) {
                        mResults = Shell.SU.run(mCommands);
                    }
                }

                if ((mResults == null) || (Application.IS_SYSTEM_APP && mResults.size() <= 1)) {
                    mResults = Shell.SU.run(mCommands);
                }

                if (mResults != null) {
                    fos = new FileOutputStream(JF_LOG_FILE_FSSTRIM);
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
