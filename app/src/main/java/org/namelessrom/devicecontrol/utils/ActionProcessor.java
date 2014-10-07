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
package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.utils.constants.Constants;

import java.util.ArrayList;
import java.util.List;

public class ActionProcessor implements Constants {

    //----------------------------------------------------------------------------------------------
    // CPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_CPU_FREQUENCY_MAX = "cpu_frequency_max";
    public static final String ACTION_CPU_FREQUENCY_MIN = "cpu_frequency_min";
    public static final String ACTION_CPU_GOVERNOR      = "cpu_governor";
    //----------------------------------------------------------------------------------------------
    // GPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_GPU_FREQUENCY_MAX = "gpu_frequency_max";
    public static final String ACTION_GPU_GOVERNOR      = "gpu_governor";
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_3D_SCALING        = "3d_scaling";
    //----------------------------------------------------------------------------------------------
    // Extras
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_IO_SCHEDULER      = "io_scheduler";
    public static final String ACTION_READ_AHEAD        = "read_ahead";
    public static final String ACTION_KSM_ENABLED       = "ksm_enabled";
    public static final String ACTION_KSM_DEFERRED      = "ksm_deferred";
    public static final String ACTION_KSM_PAGES         = "ksm_pages";
    public static final String ACTION_KSM_SLEEP         = "ksm_sleep";

    public static final String[] CATEGORIES =
            {TaskerItem.CATEGORY_SCREEN_ON, TaskerItem.CATEGORY_SCREEN_OFF};

    public static List<String> getActions() {
        final List<String> actions = new ArrayList<String>();

        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        actions.add(ACTION_CPU_FREQUENCY_MAX);
        actions.add(ACTION_CPU_FREQUENCY_MIN);
        actions.add(ACTION_CPU_GOVERNOR);

        //------------------------------------------------------------------------------------------
        // General Actions
        //------------------------------------------------------------------------------------------
        actions.add(ACTION_IO_SCHEDULER);
        if (Utils.fileExists(KSM_PATH)) {
            if (Utils.fileExists(Application.get().getString(R.string.file_ksm_run))) {
                actions.add(ACTION_KSM_ENABLED);
            }
            if (Utils.fileExists(Application.get().getString(R.string.file_ksm_deferred))) {
                actions.add(ACTION_KSM_DEFERRED);
            }
            if (Utils.fileExists(KSM_PAGES_TO_SCAN)) actions.add(ACTION_KSM_PAGES);
            if (Utils.fileExists(KSM_SLEEP)) actions.add(ACTION_KSM_SLEEP);
        }

        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            if (Utils.fileExists(GPU_MAX_FREQ_FILE)) actions.add(ACTION_GPU_FREQUENCY_MAX);
            if (Utils.fileExists(GPU_GOV_PATH)) actions.add(ACTION_GPU_GOVERNOR);
            if (Utils.fileExists(FILE_3D_SCALING)) actions.add(ACTION_3D_SCALING);
        }

        return actions;
    }

    public static void processAction(final String cmd) {
        if (cmd == null || cmd.isEmpty()) {
            Logger.w(ActionProcessor.class, "Can not perform action, command is empty!");
            return;
        }

        Logger.i(ActionProcessor.class, String.format("Performing Action: %s", cmd));
        Utils.runRootCommand(cmd);
    }

    public static void processAction(final String action, final String value) {
        processAction(action, value, false);
    }

    public static void processAction(final String action, final String value, final boolean boot) {
        processAction(getProcessAction(action, value, boot));
    }

    public static String getProcessAction(final String action, final String value,
            final boolean boot) {
        if (action == null || action.isEmpty() || value == null || value.isEmpty()) return "";

        final StringBuilder sb = new StringBuilder();
        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        final int cpu = CpuUtils.get().getNumOfCpus();
        String path;

        if (ACTION_CPU_FREQUENCY_MAX.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.get().onlineCpu(i));
                path = CpuUtils.get().getMaxCpuFrequencyPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_max" + i, CpuUtils.get().getMaxCpuFrequencyPath(i), value));
                }
            }
        } else if (ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.get().onlineCpu(i));
                path = CpuUtils.get().getMinCpuFrequencyPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_min" + i, CpuUtils.get().getMinCpuFrequencyPath(i), value));
                }
            }
        } else if (ACTION_CPU_GOVERNOR.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.get().onlineCpu(i));
                path = GovernorUtils.get().getGovernorPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_gov" + i, GovernorUtils.get().getGovernorPath(i), value));
                }
            }
        }
        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        else if (ACTION_GPU_FREQUENCY_MAX.equals(action)) {
            sb.append(Utils.getWriteCommand(GPU_MAX_FREQ_FILE, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_GPU, "gpu_max",
                                GPU_MAX_FREQ_FILE, value)
                );
            }
        } else if (ACTION_GPU_GOVERNOR.equals(action)) {
            sb.append(Utils.getWriteCommand(GPU_GOV_PATH, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_GPU, "gpu_gov", GPU_GOV_PATH, value)
                );
            }
        } else if (ACTION_3D_SCALING.equals(action)) {
            sb.append(Utils.getWriteCommand(FILE_3D_SCALING, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_GPU, PREF_3D_SCALING, FILE_3D_SCALING,
                                value)
                );
            }
        }
        //------------------------------------------------------------------------------------------
        // Extras
        //------------------------------------------------------------------------------------------
        else if (ACTION_IO_SCHEDULER.equals(action)) {
            int c = 0;
            for (final String ioPath : IO_SCHEDULER_PATH) {
                sb.append(Utils.getWriteCommand(ioPath, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_EXTRAS,
                            "io" + (c++), ioPath, value));
                }
            }
        }
        // Read Ahead ------------------------------------------------------------------------------
        else if (ACTION_READ_AHEAD.equals(action)) {
            int c = 0;
            for (final String readAheadPath : READ_AHEAD_PATH) {
                sb.append(Utils.getWriteCommand(readAheadPath, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_EXTRAS,
                            "readahead" + (c++), readAheadPath, value));
                }
            }
        }
        // KSM -------------------------------------------------------------------------------------
        else if (ACTION_KSM_ENABLED.equals(action)) {
            path = Application.get().getString(R.string.file_ksm_run);
            sb.append(Utils.getWriteCommand(path, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_EXTRAS, "ksm_run", path, value));
            }
        } else if (ACTION_KSM_DEFERRED.equals(action)) {
            path = Application.get().getString(R.string.file_ksm_deferred);
            sb.append(Utils.getWriteCommand(path, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_EXTRAS, "ksm_deferred", path, value));
            }
        } else if (ACTION_KSM_PAGES.equals(action)) {
            sb.append(Utils.getWriteCommand(KSM_PAGES_TO_SCAN, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_EXTRAS, "ksm_pages_to_scan",
                                KSM_PAGES_TO_SCAN, value)
                );
            }
        } else if (ACTION_KSM_SLEEP.equals(action)) {
            sb.append(Utils.getWriteCommand(KSM_SLEEP, value));
            if (boot) {
                PreferenceHelper.setBootup(
                        new DataItem(DatabaseHandler.CATEGORY_EXTRAS, "ksm_sleep",
                                KSM_SLEEP, value)
                );
            }
        }

        return sb.toString();
    }

}
