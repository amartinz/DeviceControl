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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.fragments.main.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceExtrasFragment;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.helpers.AlarmHelper;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.GpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.threads.FireAndForget;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class BootUpService extends Service
        implements DeviceConstants, FileConstants, PerformanceConstants {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
        }
        new BootTask(this).execute();
        return START_NOT_STICKY;
    }

    private class BootTask extends AsyncTask<Void, Void, Void> {
        private final Context mContext;

        private BootTask(Context c) {
            mContext = c;
        }

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
                boolean tmp;
                StringBuilder sbCmd = new StringBuilder();
                List<String> fileList = new ArrayList<String>();
                List<String> valueList = new ArrayList<String>();

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
                    if (DeviceFragment.sKnockOn) {
                        logDebug("Reapplying: sKnockOn");
                        fileList.add(DeviceFragment.sKnockOnFile);
                        valueList.add(PreferenceHelper.getBoolean(KEY_KNOCK_ON, false) ? "1" : "0");
                    }

                    if (VibratorTuningPreference.isSupported()) {
                        logDebug("Reapplying: Vibration");
                        final int percent = VibratorTuningPreference.strengthToPercent(
                                PreferenceHelper.getInt(KEY_VIBRATOR_TUNING,
                                        VIBRATOR_INTENSITY_DEFAULT_VALUE)
                        );
                        fileList.add(VibratorTuningPreference.FILE_VIBRATOR);
                        valueList.add("" + percent);
                    }

                    if (HighTouchSensitivity.isSupported()) {
                        logDebug("Reapplying: Glove Mode");
                        fileList.add(HighTouchSensitivity.COMMAND_PATH);
                        valueList.add(PreferenceHelper.getBoolean(KEY_GLOVE_MODE, false)
                                ? HighTouchSensitivity.GLOVE_MODE_ENABLE
                                : HighTouchSensitivity.GLOVE_MODE_DISABLE);
                    }

                    if (DeviceFragment.sHasPanel) {
                        logDebug("Reapplying: Panel Color Temp");
                        fileList.add(DeviceFragment.sHasPanelFile);
                        valueList.add(PreferenceHelper.getString(KEY_PANEL_COLOR_TEMP, "2"));
                    }

                    if (DeviceFragment.sHasTouchkeyToggle) {
                        logDebug("Reapplying: Touchkey Light");
                        tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_LIGHT, true);
                        fileList.add(FILE_TOUCHKEY_TOGGLE);
                        valueList.add(tmp ? "255" : "0");
                        fileList.add(FILE_TOUCHKEY_BRIGHTNESS);
                        valueList.add(tmp ? "255" : "0");
                    }

                    if (DeviceFragment.sHasTouchkeyBLN) {
                        logDebug("Reapplying: Touchkey BLN");
                        tmp = PreferenceHelper.getBoolean(KEY_TOUCHKEY_BLN, true);
                        fileList.add(FILE_BLN_TOGGLE);
                        valueList.add(tmp ? "1" : "0");
                    }

                    if (DeviceFragment.sHasKeyboardToggle) {
                        logDebug("Reapplying: KeyBoard Light");
                        tmp = PreferenceHelper.getBoolean(KEY_KEYBOARD_LIGHT, true);
                        fileList.add(FILE_KEYBOARD_TOGGLE);
                        valueList.add(tmp ? "255" : "0");
                    }
                }

                //==================================================================================
                // Performance
                //==================================================================================
                if (PreferenceHelper.getBoolean(SOB_CPU, false)) {
                    CpuUtils.restore();
                }

                if (PreferenceHelper.getBoolean(SOB_GPU, false)) {
                    GpuUtils.restore();
                }

                if (PreferenceHelper.getBoolean(SOB_EXTRAS, false)) {
                    if (PerformanceExtrasFragment.sLcdPowerReduce) {
                        logDebug("Reapplying: LcdPowerReduce");
                        fileList.add(PerformanceExtrasFragment.sLcdPowerReduceFile);
                        valueList.add(PreferenceHelper.getBoolean(KEY_LCD_POWER_REDUCE, false)
                                ? "1" : "0");
                    }
                    if (CpuUtils.hasIntelliPlug()) {
                        logDebug("Reapplying: IntelliPlug");
                        fileList.add(CpuUtils.INTELLI_PLUG_PATH);
                        valueList.add(PreferenceHelper.getBoolean(KEY_INTELLI_PLUG, false)
                                ? "1" : "0");
                    }
                    if (CpuUtils.hasIntelliPlugEcoMode()) {
                        logDebug("Reapplying: IntelliPlugEco");
                        fileList.add(CpuUtils.INTELLI_PLUG_ECO_MODE_PATH);
                        valueList.add(PreferenceHelper.getBoolean(KEY_INTELLI_PLUG_ECO, false)
                                ? "1" : "0");
                    }
                    if (PerformanceExtrasFragment.sMcPowerScheduler) {
                        logDebug("Reapplying: McPowerScheduler");
                        fileList.add(PerformanceExtrasFragment.sMcPowerSchedulerFile);
                        valueList.add(PreferenceHelper.getInt(KEY_MC_POWER_SCHEDULER, 2) + "");
                    }
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
                // Turn file and value list into commands
                //==================================================================================
                for (int i = 0; i < fileList.size(); i++) {
                    sbCmd.append("busybox echo ").append(valueList.get(i))
                            .append(" > ").append(fileList.get(i))
                            .append(";\n");
                }

                //==================================================================================
                // Execute
                //==================================================================================
                final String cmd = sbCmd.toString();
                logDebug("bootUp | executing: " + cmd);
                new FireAndForget(cmd).run();

                logDebug("BootUp Done!");
            }

            return null;
        }
    }
}
