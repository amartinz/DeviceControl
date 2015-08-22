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
import android.text.TextUtils;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Device;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureFragment;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureKernelFragment;
import org.namelessrom.devicecontrol.modules.editor.SysctlFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.EntropyFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import io.paperdb.Paper;

public class BootupService extends IntentService {
    private static final Object lockObject = new Object();

    public BootupService() { super("BootupService"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            stopSelf();
            return;
        }

        startBootupRestoration();
    }

    @Override
    public void onDestroy() {
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

    private void startBootupRestoration() {
        Paper.init(this);

        final DeviceConfig configuration = DeviceConfig.get();
        if (configuration.dcFirstStart) {
            Logger.i(this, "First start not completed, exiting");
            return;
        }

        // Update information about the device, to see whether we fulfill all requirements
        final Device device = Device.get(this).update();

        //==================================================================================
        // No Root, No Friends, That's Life ...
        //==================================================================================
        if (!device.hasRoot || !device.hasBusyBox) {
            Logger.e(this, "No Root, No Friends, That's Life ...");
            return;
        }

        // patch sepolicy
        Utils.patchSEPolicy(this);

        final BootupConfig bootupConfig = BootupConfig.get();

        int delay = bootupConfig.automatedRestorationDelay;
        if (delay != 0) {
            Logger.v(this, "Delaying bootup restoration by %s seconds", delay);
            try {
                Thread.sleep(delay * 1000);
            } catch (Exception ignored) { }
            Logger.v(this, "Done sleeping, starting the actual work");
        }

        //==================================================================================
        // Tasker
        //==================================================================================
        TaskerConfig taskerConfig = TaskerConfig.get();
        if (taskerConfig.fstrimEnabled) {
            Logger.v(this, "Scheduling Tasker - FSTRIM");
            AlarmHelper.setAlarmFstrim(this, taskerConfig.fstrimInterval);
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
        cmd = DeviceFeatureFragment.restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- DEVICE END -----");

        //==================================================================================
        // Performance
        //==================================================================================
        Logger.i(this, "----- CPU START -----");
        cmd = CpuUtils.get().restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- CPU END -----");

        Logger.i(this, "----- GPU START -----");
        cmd = GpuUtils.get().restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- GPU END -----");

        Logger.i(this, "----- EXTRAS START -----");
        cmd = DeviceFeatureKernelFragment.restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- EXTRAS END -----");

        Logger.i(this, "----- VOLTAGE START -----");
        // TODO: FULLY convert to bootup
        cmd = VoltageFragment.restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- VOLTAGE END -----");

        //==================================================================================
        // Tools
        //==================================================================================
        Logger.i(this, "----- TOOLS START -----");
        cmd = SysctlFragment.restore(bootupConfig);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        if (new File("/system/etc/sysctl.conf").exists()) {
            sbCmd.append("busybox sysctl -p;\n");
        }

        cmd = EntropyFragment.restore();
        if (!TextUtils.isEmpty(cmd)) {
            Logger.v(this, cmd);
            sbCmd.append(cmd);
        }
        Logger.i(this, "----- TOOLS END -----");

        Logger.i(this, "----- SPECIAL START -----");
        cmd = restoreCategory(bootupConfig, BootupConfig.CATEGORY_INTELLI_HOTPLUG);
        Logger.v(this, cmd);
        sbCmd.append(cmd);

        cmd = restoreCategory(bootupConfig, BootupConfig.CATEGORY_MAKO_HOTPLUG);
        Logger.v(this, cmd);
        sbCmd.append(cmd);
        Logger.i(this, "----- SPECIAL END -----");

        //==================================================================================
        // Execute
        //==================================================================================
        cmd = sbCmd.toString();
        if (!cmd.isEmpty()) {
            Utils.runRootCommand(cmd);
        }
        Logger.i(this, "Bootup Done!");
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
