package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class ActionProcessor implements PerformanceConstants {

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
    // Extras
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_IO_SCHEDULER      = "io_scheduler";

    public static final String[] CATEGORIES =
            {TaskerItem.CATEGORY_SCREEN_ON, TaskerItem.CATEGORY_SCREEN_OFF};
    public static final String[] ACTIONS    =
            {
                    ACTION_CPU_FREQUENCY_MAX, ACTION_CPU_FREQUENCY_MIN, ACTION_CPU_GOVERNOR,
                    ACTION_IO_SCHEDULER
            };

    public static void processAction(final String action, final String value) {
        processAction(action, value, false);
    }

    public static void processAction(final String action, final String value, final boolean boot) {
        if (action == null || action.isEmpty() || value == null || value.isEmpty()) return;

        final StringBuilder sb = new StringBuilder();
        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        final int cpu = CpuUtils.getNumOfCpus();
        String path;

        if (ACTION_CPU_FREQUENCY_MAX.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.onlineCpu(i));
                path = CpuUtils.getMaxCpuFrequencyPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_max" + i, CpuUtils.getMaxCpuFrequencyPath(i), value));
                }
            }
        } else if (ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.onlineCpu(i));
                path = CpuUtils.getMinCpuFrequencyPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_min" + i, CpuUtils.getMinCpuFrequencyPath(i), value));
                }
            }
        } else if (ACTION_CPU_GOVERNOR.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.onlineCpu(i));
                path = CpuUtils.getGovernorPath(i);
                sb.append(Utils.getWriteCommand(path, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "cpu_gov" + i, CpuUtils.getGovernorPath(i), value));
                }
            }
        } else if (ACTION_GPU_FREQUENCY_MAX.equals(action)) {
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
        } else if (ACTION_IO_SCHEDULER.equals(action)) {
            int c = 0;
            for (final String ioPath : IO_SCHEDULER_PATH) {
                sb.append(Utils.getWriteCommand(ioPath, value));
                if (boot) {
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "io" + (c++), ioPath, value));
                }
            }
        }

        final String cmd = sb.toString();
        logDebug("Performing Action: " + cmd);
        Utils.runRootCommand(cmd);
    }

}
