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
package org.namelessrom.devicecontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.namelessrom.devicecontrol.services.TaskerService;
import org.namelessrom.devicecontrol.utils.DeviceConstants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;

import java.io.File;
import java.util.List;

import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

import static org.namelessrom.devicecontrol.utils.Utils.logDebug;

/**
 * Restores and Applies values on boot, as well as starts services.
 */
public class BootupReceiver extends BroadcastReceiver implements DeviceConstants {

    @Override
    public void onReceive(Context context, Intent intent) {

        PreferenceHelper.getInstance(context);

        List<String> tmpList;

        /* Reapply values */
        boolean tmp;
        StringBuilder sb = new StringBuilder();

        // Custom Shell Command
        sb.append(PreferenceHelper.getString(CUSTOM_SHELL_COMMAND, "echo \"Hello world!\""))
                .append(";");

        // Device

        // Input
        Utils.writeValue(FILE_VIBRATOR, PreferenceHelper.getString(KEY_VIBRATOR_TUNING, "50"));

        Utils.writeValue(FILE_PANEL_COLOR_TEMP,
                PreferenceHelper.getString(KEY_PANEL_COLOR_TEMP, "2"));

        if (HighTouchSensitivity.isSupported()) {
            HighTouchSensitivity.setEnabled(PreferenceHelper.getBoolean(KEY_GLOVE_MODE, false));
        }

        // Lights
        tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_LIGHT, true);
        Utils.writeValue(FILE_TOUCHKEY_TOGGLE, (tmp ? "255" : "0"));
        Utils.writeValue(FILE_TOUCHKEY_BRIGHTNESS, (tmp ? "255" : "0"));

        tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_BLN, true);
        Utils.writeValue(FILE_BLN_TOGGLE, (tmp ? "1" : "0"));

        tmp = PreferenceHelper.getBoolean(KEY_KEYBOARD_LIGHT, true);
        Utils.writeValue(FILE_KEYBOARD_TOGGLE, (tmp ? "255" : "0"));

        // Performance

        // Tools
        if (new File("/system/etc/sysctl.conf").exists()) {
            if (PreferenceHelper.getBoolean(SYSCTL_SOB, false)) {
                sb.append("busybox sysctl -p;");
            }
        }
        if (new File("/system/etc/vm.conf").exists()) {
            if (PreferenceHelper.getBoolean(VM_SOB, false)) {
                sb.append("busybox sysctl -p /system/etc/vm.conf;");
            }
        }

        // Without root, these features are not available, sorry!
        if (!Application.HAS_ROOT) {
            logDebug("bootUp | SU", Application.IS_LOG_DEBUG);
            tmpList = Shell.SU.run(sb.toString());
            if (tmpList.get(0) != null) {
                for (String s : tmpList) {
                    logDebug("bootUp | SU result: " + s, Application.IS_LOG_DEBUG);
                }
            }
        }

        // Start Tasker Service
        Intent service = new Intent(context, TaskerService.class);
        context.startService(service);
    }

}
