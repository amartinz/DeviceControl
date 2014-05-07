package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class ActionProcessor implements PerformanceConstants {

    public static final String ACTION_CPU_FREQUENCY_MAX = "cpu_frequency_max";
    public static final String ACTION_CPU_FREQUENCY_MIN = "cpu_frequency_min";
    public static final String ACTION_CPU_GOVERNOR      = "cpu_governor";
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
                if (!value.isEmpty()) {
                    sb.append(Utils.getWriteCommand(path, value));
                    if (boot) {
                        PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                                "cpu_max" + i, CpuUtils.getMaxCpuFrequencyPath(i), value));
                    }
                }
            }
        } else if (ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.onlineCpu(i));
                path = CpuUtils.getMinCpuFrequencyPath(i);
                if (!value.isEmpty()) {
                    sb.append(Utils.getWriteCommand(path, value));
                    if (boot) {
                        PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                                "cpu_min" + i, CpuUtils.getMinCpuFrequencyPath(i), value));
                    }
                }
            }
        } else if (ACTION_CPU_GOVERNOR.equals(action)) {
            for (int i = 0; i < cpu; i++) {
                sb.append(CpuUtils.onlineCpu(i));
                path = CpuUtils.getGovernorPath(i);
                if (!value.isEmpty()) {
                    sb.append(Utils.getWriteCommand(path, value));
                    if (boot) {
                        PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                                "cpu_gov" + i, CpuUtils.getGovernorPath(i), value));
                    }
                }
            }
        } else if (ACTION_IO_SCHEDULER.equals(action)) {
            for (final String schedulerPath : IO_SCHEDULER_PATH) {
                sb.append(Utils.getWriteCommand(schedulerPath, value));
            }
        }

        final String cmd = sb.toString();
        logDebug("Performing Action: " + cmd);
        Utils.runRootCommand(cmd);
    }

}
