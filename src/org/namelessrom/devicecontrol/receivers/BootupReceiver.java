/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.fragments.device.DeviceGraphicsFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceInputFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceLightsFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceGeneralFragment;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.threads.FireAndForget;
import org.namelessrom.devicecontrol.threads.WriteAndForget;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Restores and Applies values on boot, as well as starts services.
 */
public class BootupReceiver extends BroadcastReceiver
        implements DeviceConstants, FileConstants {

    @Override
    public void onReceive(Context context, Intent intent) {

        //==========================================================================================
        // No Root, No Friends, That's Life ...
        //==========================================================================================
        if (!Application.HAS_ROOT) {
            logDebug("No Root, No Friends, That's Life ...");
            return;
        }

        PreferenceHelper.getInstance(context);

        if (!PreferenceHelper.getBoolean(DC_FIRST_START, true)) {

            //======================================================================================
            // Tasker
            //======================================================================================
            if (PreferenceHelper.getBoolean(TASKER_TOOLS_FSTRIM, false) && Application.HAS_ROOT) {
                AlarmHelper.setAlarmFstrim(context, Integer.parseInt(
                        PreferenceHelper.getString(TASKER_TOOLS_FSTRIM_INTERVAL, "30")));
            }

            //======================================================================================
            // Fields For Reapplying
            //======================================================================================
            boolean tmp;
            StringBuilder sbCmd = new StringBuilder();
            List<String> fileList = new ArrayList<String>();
            List<String> valueList = new ArrayList<String>();

            //======================================================================================
            // Custom Shell Command
            //======================================================================================
            sbCmd.append(PreferenceHelper.getString(CUSTOM_SHELL_COMMAND, "echo \"Hello world!\""))
                    .append(";");

            //======================================================================================
            // Device Input
            //======================================================================================
            if (DeviceInputFragment.sKnockOn) {
                fileList.add(DeviceInputFragment.sKnockOnFile);
                valueList.add(PreferenceHelper.getBoolean(KEY_KNOCK_ON, false) ? "1" : "0");
            }

            if (VibratorTuningPreference.isSupported()) {
                final int percent = PreferenceHelper.getInt(KEY_VIBRATOR_TUNING
                        , VibratorTuningPreference
                        .strengthToPercent(VIBRATOR_INTENSITY_DEFAULT_VALUE));
                fileList.add(VibratorTuningPreference.FILE_VIBRATOR);
                valueList.add("" + percent);
            }

            if (HighTouchSensitivity.isSupported()) {
                fileList.add(HighTouchSensitivity.COMMAND_PATH);
                valueList.add(PreferenceHelper.getBoolean(KEY_GLOVE_MODE, false)
                        ? HighTouchSensitivity.GLOVE_MODE_ENABLE
                        : HighTouchSensitivity.GLOVE_MODE_DISABLE);
            }

            //======================================================================================
            // Device Graphics
            //======================================================================================

            if (DeviceGraphicsFragment.sHasPanel) {
                fileList.add(FILE_PANEL_COLOR_TEMP);
                valueList.add(PreferenceHelper.getString(KEY_PANEL_COLOR_TEMP, "2"));
            }

            //======================================================================================
            // Device Lights
            //======================================================================================
            if (DeviceLightsFragment.sHasTouchkeyToggle) {
                tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_LIGHT, true);
                fileList.add(FILE_TOUCHKEY_TOGGLE);
                valueList.add(tmp ? "255" : "0");
                fileList.add(FILE_TOUCHKEY_BRIGHTNESS);
                valueList.add(tmp ? "255" : "0");
            }

            if (DeviceLightsFragment.sHasTouchkeyBLN) {
                tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_BLN, true);
                fileList.add(FILE_BLN_TOGGLE);
                valueList.add(tmp ? "1" : "0");
            }

            if (DeviceLightsFragment.sHasKeyboardToggle) {
                tmp = PreferenceHelper.getBoolean(KEY_KEYBOARD_LIGHT, true);
                fileList.add(FILE_KEYBOARD_TOGGLE);
                valueList.add(tmp ? "255" : "0");
            }

            //======================================================================================
            // Performance
            //======================================================================================
            if (PerformanceGeneralFragment.sLcdPowerReduce) {
                fileList.add(PerformanceGeneralFragment.sLcdPowerReduceFile);
                valueList.add(PreferenceHelper.getBoolean(KEY_LCD_POWER_REDUCE, false)
                        ? "1" : "0");
            }
            if (PerformanceGeneralFragment.sIntelliPlugEco) {
                fileList.add(PerformanceGeneralFragment.sIntelliPlugEcoFile);
                valueList.add(PreferenceHelper.getBoolean(KEY_INTELLI_PLUG_ECO, false)
                        ? "1" : "0");
            }
            if (PerformanceGeneralFragment.sMcPowerScheduler) {
                fileList.add(PerformanceGeneralFragment.sMcPowerSchedulerFile);
                valueList.add(PreferenceHelper.getInt(KEY_MC_POWER_SCHEDULER, 2) + "");
            }

            //======================================================================================
            // Tools
            //======================================================================================
            if (new File("/system/etc/sysctl.conf").exists()) {
                if (PreferenceHelper.getBoolean(SYSCTL_SOB, false)) {
                    sbCmd.append("busybox sysctl -p;");
                }
            }
            if (new File("/system/etc/vm.conf").exists()) {
                if (PreferenceHelper.getBoolean(VM_SOB, false)) {
                    sbCmd.append("busybox sysctl -p /system/etc/vm.conf;");
                }
            }

            //======================================================================================
            // Execute
            //======================================================================================
            final String cmd = sbCmd.toString();
            logDebug("bootUp | executing: " + cmd);
            new FireAndForget(cmd).run();
            new WriteAndForget(fileList, valueList).run();
        }
    }
}
