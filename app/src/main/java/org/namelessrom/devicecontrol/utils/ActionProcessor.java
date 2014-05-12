package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.objects.Action;
import org.namelessrom.devicecontrol.objects.Category;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import java.util.ArrayList;
import java.util.List;

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
    public static final String ACTION_3D_SCALING        = "3d_scaling";
    //----------------------------------------------------------------------------------------------
    // Extras
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_IO_SCHEDULER      = "io_scheduler";

    public static List<Category> getCategories() {
        final List<Category> categories = new ArrayList<Category>();

        categories.add(new Category(TaskerItem.CATEGORY_SCREEN_ON,
                Utils.getString(R.string.screen_on)));
        categories.add(new Category(TaskerItem.CATEGORY_SCREEN_OFF,
                Utils.getString(R.string.screen_off)));

        return categories;
    }

    public static List<Action> getActions() {
        final List<Action> actions = new ArrayList<Action>();

        //------------------------------------------------------------------------------------------
        // General Actions
        //------------------------------------------------------------------------------------------
        actions.add(new Action(ACTION_CPU_FREQUENCY_MAX, Utils.getString(R.string.cpu_freq_max)));
        actions.add(new Action(ACTION_CPU_FREQUENCY_MIN, Utils.getString(R.string.cpu_freq_min)));
        actions.add(new Action(ACTION_CPU_GOVERNOR, Utils.getString(R.string.cpu_governor)));
        actions.add(new Action(ACTION_IO_SCHEDULER, Utils.getString(R.string.io)));

        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            if (Utils.fileExists(GPU_MAX_FREQ_FILE)) {
                actions.add(new Action(ACTION_GPU_FREQUENCY_MAX,
                        Utils.getString(R.string.gpu_freq_max)));
            }
            if (Utils.fileExists(GPU_GOV_PATH)) {
                actions.add(new Action(ACTION_GPU_GOVERNOR,
                        Utils.getString(R.string.gpu_governor)));
            }
            if (Utils.fileExists(FILE_3D_SCALING)) {
                actions.add(new Action(ACTION_3D_SCALING,
                        Utils.getString(R.string.gpu_3d_scaling)));
            }
        }

        return actions;
    }

    public static void processAction(final String cmd) {
        if (cmd == null || cmd.isEmpty()) {
            logDebug("Can not perform action, command is empty!");
            return;
        }

        logDebug("Performing Action: " + cmd);
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
                    PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                            "io" + (c++), ioPath, value));
                }
            }
        }

        return sb.toString();
    }

}
