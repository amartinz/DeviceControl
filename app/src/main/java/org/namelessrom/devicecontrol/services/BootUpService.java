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
import org.namelessrom.devicecontrol.fragments.main.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceExtrasFragment;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.helpers.AlarmHelper;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.GpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;

import java.io.File;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class BootUpService extends IntentService
        implements DeviceConstants, FileConstants, PerformanceConstants {

    public static void start(final Context context) {
        final Intent service = new Intent(context, BootUpService.class);
        context.startService(service);
    }

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
                    sbCmd.append(DeviceFragment.restore());
                }

                //==================================================================================
                // Performance
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_CPU, false)) {
                    sbCmd.append(CpuUtils.restore());
                }

                if (PreferenceHelper.getBoolean(SOB_GPU, false)) {
                    sbCmd.append(GpuUtils.restore());
                }

                if (PreferenceHelper.getBoolean(SOB_EXTRAS, false)) {
                    sbCmd.append(PerformanceExtrasFragment.restore());
                }

                //==================================================================================
                // Tools
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_SYSCTL, false)) {
                    if (new File("/system/etc/sysctl.conf").exists()) {
                        logDebug("Reapplying: Sysctl");
                        sbCmd.append("busybox sysctl -p;\n");
                    }
                }
                if (PreferenceHelper.getBoolean(SOB_VM, false)) {
                    if (new File("/system/etc/vm.conf").exists()) {
                        logDebug("Reapplying: Vm");
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
