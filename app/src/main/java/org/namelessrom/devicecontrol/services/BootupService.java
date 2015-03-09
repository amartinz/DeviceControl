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
import android.text.TextUtils;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Device;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.configuration.TaskerConfiguration;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.device.DeviceFeatureFragment;
import org.namelessrom.devicecontrol.device.DeviceFeatureKernelFragment;
import org.namelessrom.devicecontrol.editor.SysctlFragment;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.ui.fragments.performance.sub.EntropyFragment;
import org.namelessrom.devicecontrol.ui.fragments.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

public class BootupService extends IntentService {
    private static final Object lockObject = new Object();

    public static final String SOB_SYSCTL = "sob_sysctl";
    public static final String SOB_CPU = "sob_cpu";
    public static final String SOB_GPU = "sob_gpu";
    public static final String SOB_EXTRAS = "sob_extras";
    public static final String SOB_DEVICE = "sob_device";
    public static final String SOB_VOLTAGE = "sob_voltage";

    public BootupService() { super("BootUpService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
        }
        new BootTask(this).execute();
    }

    @Override
    public void onDestroy() {
        DatabaseHandler.tearDown();
        synchronized (lockObject) {
            Logger.i(this, "closing shells");
            try {
                RootTools.closeAllShells();
            } catch (Exception e) {
                Logger.e(this, String.format("onDestroy(): %s", e));
            }
        }
        super.onDestroy();
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
            final DeviceConfiguration configuration = DeviceConfiguration.get(mContext);
            if (configuration.dcFirstStart) {
                Logger.i(this, "First start not completed, exiting");
                return null;
            }

            // Update information about the device, to see whether we fulfill all requirements
            Device.get().update();

            //==================================================================================
            // No Root, No Friends, That's Life ...
            //==================================================================================
            if (!Device.get().hasRoot || !Device.get().hasBusyBox) {
                Logger.e(this, "No Root, No Friends, That's Life ...");
                return null;
            }

            //==================================================================================
            // Tasker
            //==================================================================================
            if (TaskerConfiguration.get(mContext).fstrimEnabled) {
                Logger.v(this, "Scheduling Tasker - FSTRIM");
                AlarmHelper.setAlarmFstrim(mContext,
                        TaskerConfiguration.get(mContext).fstrimInterval);
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
            Logger.i(this, "----- DEVICE START -----");
            if (configuration.sobDevice) {
                cmd = DeviceFeatureFragment.restore();
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- DEVICE END -----");

            //==================================================================================
            // Performance
            //==================================================================================
            Logger.i(this, "----- CPU START -----");
            if (configuration.sobCpu) {
                cmd = CpuUtils.get().restore();
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- CPU END -----");
            Logger.i(this, "----- GPU START -----");
            if (configuration.sobGpu) {
                cmd = GpuUtils.get().restore();
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- GPU END -----");
            Logger.i(this, "----- EXTRAS START -----");
            if (configuration.sobExtras) {
                cmd = DeviceFeatureKernelFragment.restore();
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- EXTRAS END -----");
            Logger.i(this, "----- VOLTAGE START -----");
            if (configuration.sobVoltage) {
                cmd = VoltageFragment.restore(mContext);
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- VOLTAGE END -----");

            //==================================================================================
            // Tools
            //==================================================================================
            Logger.i(this, "----- TOOLS START -----");
            if (configuration.sobSysctl) {
                if (new File("/system/etc/sysctl.conf").exists()) {
                    cmd = SysctlFragment.restore();
                    Logger.v(this, cmd);
                    sbCmd.append(cmd);
                    sbCmd.append("busybox sysctl -p;\n");
                }
            }

            cmd = EntropyFragment.restore(mContext);
            if (!TextUtils.isEmpty(cmd)) {
                Logger.v(this, cmd);
                sbCmd.append(cmd);
            }
            Logger.i(this, "----- TOOLS END -----");

            //==================================================================================
            // Execute
            //==================================================================================
            cmd = sbCmd.toString();
            if (!cmd.isEmpty()) {
                Utils.runRootCommand(cmd);
            }
            Logger.i(this, "Bootup Done!");

            return null;
        }
    }
}
