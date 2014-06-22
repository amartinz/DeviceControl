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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.fragments.device.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.device.FeaturesFragment;
import org.namelessrom.devicecontrol.fragments.performance.ExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.SysctlFragment;
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

            final DatabaseHandler db = DatabaseHandler.getInstance(mContext);
            PreferenceHelper.getInstance(mContext);

            if (!PreferenceHelper.getBoolean(DC_FIRST_START, true)) {

                //==================================================================================
                // No Root, No Friends, That's Life ...
                //==================================================================================
                if (!RootTools.isRootAvailable() || !RootTools.isAccessGiven()) {
                    logDebug("No Root, No Friends, That's Life ...");
                    return null;
                }

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
                final StringBuilder sbCmd = new StringBuilder();
                String cmd;

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
                logDebug("----- DEVICE START -----");
                if (PreferenceHelper.getBoolean(SOB_DEVICE, false)) {
                    cmd = DeviceFragment.restore(db);
                    logDebug(cmd);
                    sbCmd.append(cmd);
                    cmd = FeaturesFragment.restore(db);
                    logDebug(cmd);
                    sbCmd.append(cmd);
                }
                logDebug("----- DEVICE END -----");

                //==================================================================================
                // Performance
                //==================================================================================
                logDebug("----- CPU START -----");
                if (PreferenceHelper.getBoolean(SOB_CPU, false)) {
                    cmd = CpuUtils.restore(db);
                    logDebug(cmd);
                    sbCmd.append(cmd);
                }
                logDebug("----- CPU END -----");
                logDebug("----- GPU START -----");
                if (PreferenceHelper.getBoolean(SOB_GPU, false)) {
                    cmd = GpuUtils.restore(db);
                    logDebug(cmd);
                    sbCmd.append(cmd);
                }
                logDebug("----- GPU END -----");
                logDebug("----- EXTRAS START -----");
                if (PreferenceHelper.getBoolean(SOB_EXTRAS, false)) {
                    cmd = ExtrasFragment.restore(db);
                    logDebug(cmd);
                    sbCmd.append(cmd);
                }
                logDebug("----- EXTRAS END -----");
                logDebug("----- VOLTAGE START -----");
                if (PreferenceHelper.getBoolean(SOB_VOLTAGE, false)) {
                    cmd = VoltageFragment.restore();
                    logDebug(cmd);
                    sbCmd.append(cmd);
                }
                logDebug("----- VOLTAGE END -----");

                //==================================================================================
                // Tools
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_SYSCTL, false)) {
                    if (new File("/system/etc/sysctl.conf").exists()) {
                        cmd = SysctlFragment.restore(db);
                        logDebug(cmd);
                        sbCmd.append(cmd);
                        sbCmd.append("busybox sysctl -p;\n");
                    }
                }

                //==================================================================================
                // Execute
                //==================================================================================
                cmd = sbCmd.toString();
                if (!cmd.isEmpty()) {
                    Utils.runRootCommand(cmd);
                }
                logDebug("BootUp Done!");
            }

            return null;
        }
    }
}
