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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.cpu.monitors.CpuStateMonitor;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.objects.CpuCore;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils {

    //----------------------------------------------------------------------------------------------
    public static final String CPU_BASE = "/sys/devices/system/cpu/";
    public static final String CORE_ONLINE = CPU_BASE + "cpu%s/online";
    public static final String CORE_PRESENT = CPU_BASE + "present";
    public static final String FREQ_AVAIL = CPU_BASE + "cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_CURRENT = CPU_BASE + "cpu%s/cpufreq/scaling_cur_freq";
    public static final String FREQ_MAX = CPU_BASE + "cpu%s/cpufreq/scaling_max_freq";
    public static final String FREQ_MIN = CPU_BASE + "cpu%s/cpufreq/scaling_min_freq";

    public static final String FREQ_TIME_IN_STATE = CPU_BASE + "cpu0/cpufreq/stats/time_in_state";

    public static class Frequency {
        public final String[] available;
        public final String maximum;
        public final String minimum;

        public Frequency(final String[] avail, final String max, final String min) {
            available = avail;
            maximum = max;
            minimum = min;
        }
    }

    public static class State {
        public final List<CpuStateMonitor.CpuState> states;
        public final long totalTime;

        public State(final List<CpuStateMonitor.CpuState> stateList, final long totalStateTime) {
            states = stateList;
            totalTime = totalStateTime;
        }
    }

    public interface FrequencyListener {
        void onFrequency(@NonNull final Frequency cpuFreq);
    }

    public interface CoreListener {
        void onCores(@NonNull final List<CpuCore> cores);
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

    @NonNull public String getCpuFrequencyPath(final int cpu) {
        return String.format(FREQ_CURRENT, cpu);
    }

    @NonNull public String getMaxCpuFrequencyPath(final int cpu) {
        return String.format(FREQ_MAX, cpu);
    }

    @NonNull public String getMinCpuFrequencyPath(final int cpu) {
        return String.format(FREQ_MIN, cpu);
    }

    @NonNull public String getOnlinePath(final int cpu) {
        return String.format(CORE_ONLINE, cpu);
    }

    public int getCpuTemperature() {
        String tmp = Utils.readOneLine(Application.get().getString(R.string.file_thermal_cpu));
        if (!TextUtils.isEmpty(tmp) && !tmp.trim().isEmpty()) {
            int temp;
            try {
                temp = Utils.parseInt(tmp);
            } catch (Exception e) {
                Logger.e(this, "could not read cpu temperature", e);
                return -1;
            }
            temp = (temp < 0 ? 0 : temp);
            temp = (temp > 100 ? 100 : temp);
            return temp;
        }
        return -1;
    }

    @Nullable public String[] getAvailableFrequencies(final boolean sorted) {
        final String freqsRaw = Utils.readOneLine(FREQ_AVAIL);
        if (freqsRaw != null && !freqsRaw.isEmpty()) {
            final String[] freqs = freqsRaw.split(" ");
            if (!sorted) {
                return freqs;
            }
            Arrays.sort(freqs, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Utils.tryValueOf(object1, 0).compareTo(Utils.tryValueOf(object2, 0));
                }
            });
            Collections.reverse(Arrays.asList(freqs));
            return freqs;
        }
        return null;
    }

    /**
     * Get total number of cpus
     *
     * @return total number of cpus
     */
    public int getNumOfCpus() {
        int numOfCpu = 1;
        final String numOfCpus = Utils.readOneLine(CORE_PRESENT);
        if (numOfCpus != null && !numOfCpus.isEmpty()) {
            final String[] cpuCount = numOfCpus.split("-");
            if (cpuCount.length > 1) {
                try {
                    numOfCpu = Utils.parseInt(cpuCount[1]) - Utils.parseInt(cpuCount[0]) + 1;
                    if (numOfCpu < 0) {
                        numOfCpu = 1;
                    }
                } catch (NumberFormatException ex) {
                    numOfCpu = 1;
                }
            }
        }
        return numOfCpu;
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

    public void getCpuFreq(final FrequencyListener listener) {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(FREQ_AVAIL).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(getMaxCpuFrequencyPath(0)).append(" 2> /dev/null;");
            cmd.append("echo -n \"]\";");
            cmd.append("cat ").append(getMinCpuFrequencyPath(0)).append(" 2> /dev/null;");
            cmd.append(");").append("echo $command | busybox tr -d \"\\n\"");
            Logger.v(CpuUtils.class, cmd.toString());

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, cmd.toString()) {
                @Override
                public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    Logger.v(CpuUtils.class, line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    final List<String> result =
                            Arrays.asList(outputCollector.toString().split(" "));
                    final List<String> tmpList = new ArrayList<>();
                    String tmpMax = "", tmpMin = "";

                    if (result.size() <= 0) {
                        return;
                    }

                    for (final String s : result) {
                        if (s.isEmpty()) {
                            continue;
                        }
                        if (s.charAt(0) == '[') {
                            tmpMax = s.substring(1, s.length());
                        } else if (s.charAt(0) == ']') {
                            tmpMin = s.substring(1, s.length());
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String max = tmpMax;
                    final String min = tmpMin;
                    final String[] avail = tmpList.toArray(new String[tmpList.size()]);
                    Application.HANDLER.post(new Runnable() {
                        @Override public void run() {
                            listener.onFrequency(new Frequency(avail, max, min));
                        }
                    });

                }
            };

            if (mShell.isClosed()) { throw new Exception("Shell is closed"); }
            mShell.add(cmdCapture);
        } catch (Exception exc) {
            Logger.e(CpuUtils.class, "Error: " + exc.getMessage());
        }
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

    /**
     * Convert to MHz and append a tag
     *
     * @param mhzString The string to convert to MHz
     * @return tagged and converted String
     */
    @NonNull public static String toMhz(final String mhzString) {
        int value = -1;
        if (!TextUtils.isEmpty(mhzString)) {
            try {
                value = Utils.parseInt(mhzString) / 1000;
            } catch (NumberFormatException exc) {
                Logger.e(CpuUtils.get(), "toMhz", exc);
                value = -1;
            }
        }

        if (value != -1) {
            return String.valueOf(value) + " MHz";
        } else {
            return Application.get().getString(R.string.core_offline);
        }
    }

}
