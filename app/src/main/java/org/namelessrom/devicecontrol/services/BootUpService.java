/*
 *  Copyright (C) 2014 Alexander "Evisceration" Martinz
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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.fragments.device.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.device.FeaturesFragment;
import org.namelessrom.devicecontrol.fragments.performance.ExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.VoltageFragment;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.GpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import java.io.File;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class BootUpService extends IntentService
        implements DeviceConstants, FileConstants, PerformanceConstants {

    public BootUpService() { super("BootUpService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
        }
        new BootTask(this).execute();
    }

    private class BootTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        private BootTask(Context c) { mContext = c; }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            stopSelf();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //======================================================================================
            // No Root, No Friends, That's Life ...
            //======================================================================================
            if (!Application.HAS_ROOT) {
                logDebug("No Root, No Friends, That's Life ...");
                return null;
            }

            final DatabaseHandler db = DatabaseHandler.getInstance(mContext);
            PreferenceHelper.getInstance(mContext);

            if (!PreferenceHelper.getBoolean(DC_FIRST_START, true)) {

                //==================================================================================
                // Tasker
                //==================================================================================
                if (PreferenceHelper.getBoolean(FSTRIM, false)) {
                    logDebug("Scheduling Tasker - FSTRIM");
                    AlarmHelper.setAlarmFstrim(mContext,
                            PreferenceHelper.getInt(FSTRIM_INTERVAL, 480));
                }

                //==================================================================================
                // Fields For Reapplying
                //==================================================================================
                StringBuilder sbCmd = new StringBuilder();

                //==================================================================================
                // Custom Shell Command
                //==================================================================================
                /*sbCmd.append(PreferenceHelper.getString(CUSTOM_SHELL_COMMAND,
                        "echo \"Hello world!\""))
                        .append(";\n");
                */
                //==================================================================================
                // Device
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_DEVICE, false)) {
                    sbCmd.append(DeviceFragment.restore(db));
                    sbCmd.append(FeaturesFragment.restore(db));
                }

                //==================================================================================
                // Performance
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_CPU, false)) {
                    sbCmd.append(CpuUtils.restore(db));
                }

                if (PreferenceHelper.getBoolean(SOB_GPU, false)) {
                    sbCmd.append(GpuUtils.restore(db));
                }

                if (PreferenceHelper.getBoolean(SOB_EXTRAS, false)) {
                    sbCmd.append(ExtrasFragment.restore(db));
                }

                if (PreferenceHelper.getBoolean(SOB_VOLTAGE, false)) {
                    sbCmd.append(VoltageFragment.restore());
                }

                //==================================================================================
                // Tools
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_SYSCTL, false)) {
                    if (new File("/system/etc/sysctl.conf").exists()) {
                        sbCmd.append("busybox sysctl -p;\n");
                    }
                }
                if (PreferenceHelper.getBoolean(SOB_VM, false)) {
                    if (new File("/system/etc/vm.conf").exists()) {
                        sbCmd.append("busybox sysctl -p /system/etc/vm.conf;\n");
                    }
                }

                //==================================================================================
                // Execute
                //==================================================================================
                final String cmd = sbCmd.toString();
                logDebug("bootUp | executing: " + cmd);
                Utils.runRootCommand(cmd);

                logDebug("BootUp Done!");
            }

            return null;
        }
    }
}
