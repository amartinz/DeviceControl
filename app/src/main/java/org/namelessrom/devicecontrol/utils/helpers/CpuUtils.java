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
package org.namelessrom.devicecontrol.utils.helpers;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils implements PerformanceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String ACTION_FREQ_MAX = "action_freq_max";
    public static final String ACTION_FREQ_MIN = "action_freq_min";
    public static final String ACTION_GOV      = "action_gov";

    //==============================================================================================
    // Methods
    //==============================================================================================
    public static void enableIntelliPlug(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_PATH, (enable ? "1" : "0"));
    }

    public static void enableIntelliPlugEcoMode(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_ECO_MODE_PATH, (enable ? "1" : "0"));
    }

    public static String getCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return CPU0_FREQ_CURRENT_PATH;
            case 1:
                return CPU1_FREQ_CURRENT_PATH;
            case 2:
                return CPU2_FREQ_CURRENT_PATH;
            case 3:
                return CPU3_FREQ_CURRENT_PATH;
        }
    }

    public static String getMaxCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return FREQ0_MAX_PATH;
            case 1:
                return FREQ1_MAX_PATH;
            case 2:
                return FREQ2_MAX_PATH;
            case 3:
                return FREQ3_MAX_PATH;
        }
    }

    public static String getMinCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return FREQ0_MIN_PATH;
            case 1:
                return FREQ1_MIN_PATH;
            case 2:
                return FREQ2_MIN_PATH;
            case 3:
                return FREQ3_MIN_PATH;
        }
    }

    public static String getGovernorPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return GOV0_CURRENT_PATH;
            case 1:
                return GOV1_CURRENT_PATH;
            case 2:
                return GOV2_CURRENT_PATH;
            case 3:
                return GOV3_CURRENT_PATH;
        }
    }

    public static String getOnlinePath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return "";
            case 1:
                return CORE1_ONLINE;
            case 2:
                return CORE2_ONLINE;
            case 3:
                return CORE3_ONLINE;
        }
    }

    public static String getCpuFrequency(final int cpu) {
        final String path = getCpuFrequencyPath(cpu);

        if (Utils.fileExists(path)) {
            return Utils.readOneLine(path);
        } else {
            return "0";
        }
    }

    public static int getCpuTemperature() {
        String tmpString = Utils.readOneLine(CPU_TEMP_PATH);
        if (tmpString != null && !tmpString.trim().isEmpty()) {
            int temp = Integer.parseInt(tmpString);
            temp = (temp < 0 ? 0 : temp);
            temp = (temp > 100 ? 100 : temp);
            return temp;
        } else {
            return -1;
        }
    }

    public static boolean getIntelliPlugActive() {
        return Utils.readOneLine(INTELLI_PLUG_PATH).trim().equals("1");
    }

    public static boolean getIntelliPlugEcoMode() {
        return Utils.readOneLine(INTELLI_PLUG_ECO_MODE_PATH).trim().equals("1");
    }

    public static boolean hasIntelliPlug() {
        return Utils.fileExists(INTELLI_PLUG_PATH);
    }

    public static boolean hasIntelliPlugEcoMode() {
        return Utils.fileExists(INTELLI_PLUG_ECO_MODE_PATH);
    }

    /**
     * Get total number of cpus
     *
     * @return total number of cpus
     */
    public static int getNumOfCpus() {
        int numOfCpu = 1;
        final String numOfCpus = Utils.readOneLine(PRESENT_CPUS);
        if (numOfCpus != null && !numOfCpus.isEmpty()) {
            final String[] cpuCount = numOfCpus.split("-");
            if (cpuCount.length > 1) {
                try {
                    numOfCpu = Integer.parseInt(cpuCount[1]) - Integer.parseInt(cpuCount[0]) + 1;

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

    public static String getValue(final int cpu, final String action) {
        return getOrSetValue(cpu, "", action, false);
    }

    public static void setValue(final int cpu, final String value, final String action) {
        getOrSetValue(cpu, value, action, true);
    }

    private static String getOrSetValue(final int cpu, final String value,
            final String action, final boolean set) {
        String path = "";
        final String pathOnline = getOnlinePath(cpu);

        if (action.equals(ACTION_FREQ_MAX)) {
            path = getMaxCpuFrequencyPath(cpu);
        } else if (action.equals(ACTION_FREQ_MIN)) {
            path = getMinCpuFrequencyPath(cpu);
        } else if (action.equals(ACTION_GOV)) {
            path = getGovernorPath(cpu);
        }

        if (set) {
            if (!path.isEmpty()) {
                // Bring them online to apply values on all cores
                final StringBuilder sb = new StringBuilder();
                if (!pathOnline.isEmpty()) {
                    sb.append("busybox echo 0 > ").append(pathOnline).append(";");
                    sb.append("busybox echo 1 > ").append(pathOnline).append(";");
                }
                sb.append("busybox echo ").append(value).append(" > ").append(path);
                Utils.runRootCommand(sb.toString());
            }
        } else {
            if (!path.isEmpty() && Utils.fileExists(path)) {
                final String result = Utils.readOneLine(path);
                return (result != null ? result.trim() : "0");
            } else {
                return "0";
            }
        }

        return null;
    }

    /**
     * Gets available schedulers from file
     *
     * @return available schedulers
     */
    public static String[] getAvailableIOSchedulers() {
        String[] schedulers = null;
        final String[] aux = Utils.readStringArray(IO_SCHEDULER_PATH[0]);
        if (aux != null) {
            schedulers = new String[aux.length];
            for (int i = 0; i < aux.length; i++) {
                if (aux[i].charAt(0) == '[') {
                    schedulers[i] = aux[i].substring(1, aux[i].length() - 1);
                } else {
                    schedulers[i] = aux[i];
                }
            }
        }
        return schedulers;
    }

    /**
     * Get current IO Scheduler
     *
     * @return current io scheduler
     */
    public static String getIOScheduler() {
        String scheduler = null;
        final String[] schedulers = Utils.readStringArray(IO_SCHEDULER_PATH[0]);
        if (schedulers != null) {
            for (String s : schedulers) {
                if (s.charAt(0) == '[') {
                    scheduler = s.substring(1, s.length() - 1);
                    break;
                }
            }
        }
        return scheduler;
    }

    public static String getAvailableGovernors() {
        return Utils.readOneLine(GOV_AVAIALBLE_PATH);
    }

    /**
     * Convert to MHz and append a tag
     *
     * @param mhzString The string to convert to MHz
     * @return tagged and converted String
     */
    public static String toMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
        } else {
            if (Application.applicationContext != null) {
                return Application.applicationContext.getString(R.string.core_offline);
            } else {
                return "Offline";
            }
        }
    }

    public static void restore() {
        final StringBuilder sb = new StringBuilder();

        final int cpuCount = getNumOfCpus();
        for (int i = 0; i < cpuCount; i++) {
            setValue(i,
                    PreferenceHelper.getString(PREF_MAX_CPU,
                            getValue(i, ACTION_FREQ_MAX)),
                    ACTION_FREQ_MAX
            );
            setValue(i,
                    PreferenceHelper.getString(PREF_MIN_CPU,
                            getValue(i, ACTION_FREQ_MIN)),
                    ACTION_FREQ_MIN
            );
            setValue(i,
                    PreferenceHelper.getString(PREF_GOV,
                            getValue(i, ACTION_GOV)),
                    ACTION_GOV
            );
        }
        final String io = PreferenceHelper.getString(PREF_IO, getIOScheduler());
        for (String aIO_SCHEDULER_PATH : IO_SCHEDULER_PATH) {
            if (Utils.fileExists(aIO_SCHEDULER_PATH)) {
                sb.append("busybox echo ").append(io).append(" > ")
                        .append(aIO_SCHEDULER_PATH).append(";\n");
            }
        }
        Utils.runRootCommand(sb.toString());
    }

}
