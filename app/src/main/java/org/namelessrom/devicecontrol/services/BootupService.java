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
import android.os.Handler;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureFragment;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureKernelFragment;
import org.namelessrom.devicecontrol.modules.editor.SysctlFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.EntropyFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.RootCheck;
import at.amartinz.execution.RootShell;
import at.amartinz.execution.ShellManager;
import hugo.weaving.DebugLog;
import io.paperdb.Paper;
import timber.log.Timber;

public class BootupService extends IntentService {
    public BootupService() {
        super("BootupService");
    }

    @Override protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
            return;
        }

        startBootupRestoration();
    }

    @Override public void onDestroy() {
        Timber.d("closing shells");
        ShellManager.get().cleanupShells();

        super.onDestroy();
    }

    private void startBootupRestoration() {
        Paper.init(this);

        final DeviceConfig configuration = DeviceConfig.get();
        if (configuration.dcFirstStart) {
            Timber.i("First start not completed, exiting");
            return;
        }

        //========================================================================================================================
        // No Root, No Friends, That's Life ...
        //========================================================================================================================
        if (!RootCheck.isRooted()) {
            Timber.e("No Root, No Friends, That's Life ...");
            return;
        }

        // patch sepolicy
        Utils.patchSEPolicy(this);

        final BootupConfig bootupConfig = BootupConfig.get();
        final int delay = bootupConfig.automatedRestorationDelay;
        if (delay > 0) {
            Timber.v("Delaying bootup restoration by %s seconds", delay);
        }
        new Handler().postDelayed(new BootupRunnable(getApplicationContext(), bootupConfig), delay);
    }

    private static final class BootupRunnable implements Runnable {
        private BootupConfig bootupConfig;
        private Context context;

        public BootupRunnable(Context context, BootupConfig bootupConfig) {
            this.context = context;
            this.bootupConfig = bootupConfig;
        }

        @DebugLog @Override public void run() {
            //========================================================================================================================
            // Tasker
            //========================================================================================================================
            final TaskerConfig taskerConfig = TaskerConfig.get();
            if (taskerConfig.fstrimEnabled) {
                Timber.v("Scheduling Tasker - FSTRIM");
                AlarmHelper.setAlarmFstrim(context, taskerConfig.fstrimInterval);
            }

            //========================================================================================================================
            // Fields For Reapplying
            //========================================================================================================================
            final StringBuilder sbCmd = new StringBuilder();
            String cmd;

            //========================================================================================================================
            // Custom Shell Command
            //========================================================================================================================
            //sbCmd.append(PreferenceHelper.getString(CUSTOM_SHELL_COMMAND,"echo \"Hello world!\"")).append(";\n");

            //========================================================================================================================
            // Device
            //========================================================================================================================
            Timber.v("----- DEVICE START -----");
            cmd = DeviceFeatureFragment.restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- DEVICE END -----");

            //========================================================================================================================
            // Performance
            //========================================================================================================================
            Timber.v("----- CPU START -----");
            cmd = CpuUtils.get().restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- CPU END -----");

            Timber.v("----- GPU START -----");
            cmd = GpuUtils.get().restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- GPU END -----");

            Timber.v("----- EXTRAS START -----");
            cmd = DeviceFeatureKernelFragment.restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- EXTRAS END -----");

            Timber.v("----- VOLTAGE START -----");
            // TODO: FULLY convert to bootup
            cmd = VoltageFragment.restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- VOLTAGE END -----");

            //========================================================================================================================
            // Tools
            //========================================================================================================================
            Timber.v("----- TOOLS START -----");
            cmd = SysctlFragment.restore(bootupConfig);
            Timber.v(cmd);
            sbCmd.append(cmd);
            if (new File("/system/etc/sysctl.conf").exists()) {
                sbCmd.append(BusyBox.callBusyBoxApplet("sysctl", "-p;"));
            }

            cmd = EntropyFragment.restore();
            if (!TextUtils.isEmpty(cmd)) {
                Timber.v(cmd);
                sbCmd.append(cmd);
            }
            Timber.v("----- TOOLS END -----");

            Timber.v("----- SPECIAL START -----");
            cmd = restoreCategory(bootupConfig, BootupConfig.CATEGORY_INTELLI_HOTPLUG);
            Timber.v(cmd);
            sbCmd.append(cmd);

            cmd = restoreCategory(bootupConfig, BootupConfig.CATEGORY_MAKO_HOTPLUG);
            Timber.v(cmd);
            sbCmd.append(cmd);
            Timber.v("----- SPECIAL END -----");

            //========================================================================================================================
            // Execute
            //========================================================================================================================
            cmd = sbCmd.toString();
            Timber.v("Starting bootup with cmd:\n%s", cmd);
            if (!cmd.isEmpty()) {
                RootShell.fireAndBlock(cmd);
            }

            Timber.v("Bootup Done!");
        }

        private String restoreCategory(BootupConfig config, String category) {
            final ArrayList<BootupItem> items = config.getItemsByCategory(category);
            if (items.size() == 0) {
                return "";
            }

            final StringBuilder sbCmd = new StringBuilder();
            for (final BootupItem item : items) {
                if (!item.enabled) {
                    continue;
                }
                sbCmd.append(Utils.getWriteCommand(item.filename, item.value));
            }

            return sbCmd.toString();
        }
    }
}
