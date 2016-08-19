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
package org.namelessrom.devicecontrol.modules.cpu;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.modules.cpu.monitors.CpuStateMonitor;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils {

    //----------------------------------------------------------------------------------------------
    public static final String CPU_BASE = "/sys/devices/system/cpu/";
    public static final String CORE_ONLINE = CPU_BASE + "cpu%s/online";

    public static final String FREQ_TIME_IN_STATE = CPU_BASE + "cpu0/cpufreq/stats/time_in_state";

    public static class State {
        public final List<CpuStateMonitor.CpuState> states;
        public final long totalTime;

        public State(final List<CpuStateMonitor.CpuState> stateList, final long totalStateTime) {
            states = stateList;
            totalTime = totalStateTime;
        }
    }

    public interface StateListener {
        void onStates(@NonNull final State states);
    }

    private static CpuUtils sInstance;

    private CpuUtils() { }

    public static CpuUtils get() {
        if (sInstance == null) {
            sInstance = new CpuUtils();
        }
        return sInstance;
    }

    @NonNull public String getOnlinePath(final int cpu) {
        return String.format(CORE_ONLINE, cpu);
    }

    // TODO: newer qcom support, tsens has scaling factors
    public int getCpuTemperature() {
        String tmp = Utils.readOneLine(App.get().getString(R.string.file_thermal_cpu));
        int temp = -1;
        if (!TextUtils.isEmpty(tmp) && !tmp.trim().isEmpty()) {
            temp = Utils.parseInt(tmp, -1);
            if (temp > 100 || temp < 0) {
                temp = -1;
            }
        }
        return temp;
    }

    public String restore(BootupConfig config) {
        final ArrayList<BootupItem> items =
                config.getItemsByCategory(BootupConfig.CATEGORY_CPU);

        if (items.size() == 0) {
            return "";
        }
        final StringBuilder sbCmd = new StringBuilder();

        String tmpString;
        int tmpInt;
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            tmpInt = -1;
            tmpString = item.name;
            if (tmpString != null && !tmpString.contains("io")) {
                try {
                    tmpInt = Utils.parseInt(
                            String.valueOf(tmpString.charAt(tmpString.length() - 1)));
                } catch (Exception exc) {
                    tmpInt = -1;
                }
            }
            if (tmpInt != -1) {
                final String path = getOnlinePath(tmpInt);
                if (!TextUtils.isEmpty(path)) {
                    sbCmd.append(Utils.getWriteCommand(path, "0"));
                    sbCmd.append(Utils.getWriteCommand(path, "1"));
                }
            }
            sbCmd.append(Utils.getWriteCommand(item.filename, item.value));
        }

        return sbCmd.toString();
    }

    @NonNull public String onlineCpu(final int cpu) {
        // protect against onlining core 0
        if (cpu == 0) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();
        final String pathOnline = getOnlinePath(cpu);
        if (!pathOnline.isEmpty()) {
            sb.append(Utils.getWriteCommand(pathOnline, "1"));
        }
        return sb.toString();
    }

}
