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

import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils implements PerformanceConstants {

    //==============================================================================================
    // Paths
    //==============================================================================================
    public static final String POSSIBLE_CPUS = "/sys/devices/system/cpu/possible";
    public static final String PRESENT_CPUS = "/sys/devices/system/cpu/present";
    public static final String CORE1_ONLINE = "/sys/devices/system/cpu/cpu1/online";
    public static final String CORE2_ONLINE = "/sys/devices/system/cpu/cpu2/online";
    public static final String CORE3_ONLINE = "/sys/devices/system/cpu/cpu3/online";
    //----------------------------------------------------------------------------------------------
    public static final String CPU0_FREQ_CURRENT_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String CPU1_FREQ_CURRENT_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    public static final String CPU2_FREQ_CURRENT_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    public static final String CPU3_FREQ_CURRENT_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";
    //----------------------------------------------------------------------------------------------
    public static final String FREQ0_INFO_MAX_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    public static final String FREQ0_INFO_MIN_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
    public static final String FREQ0_MAX_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String FREQ0_MIN_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String FREQ1_INFO_MAX_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_max_freq";
    public static final String FREQ1_INFO_MIN_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_min_freq";
    public static final String FREQ1_MAX_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq";
    public static final String FREQ1_MIN_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq";
    public static final String FREQ2_INFO_MAX_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/cpuinfo_max_freq";
    public static final String FREQ2_INFO_MIN_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/cpuinfo_min_freq";
    public static final String FREQ2_MAX_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq";
    public static final String FREQ2_MIN_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq";
    public static final String FREQ3_INFO_MAX_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/cpuinfo_max_freq";
    public static final String FREQ3_INFO_MIN_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/cpuinfo_min_freq";
    public static final String FREQ3_MAX_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq";
    public static final String FREQ3_MIN_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq";
    //----------------------------------------------------------------------------------------------
    public static final String FREQ_AVAILABLE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_TIME_IN_STATE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    //----------------------------------------------------------------------------------------------
    public static final String GOV0_CURRENT_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String GOV1_CURRENT_PATH = "/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor";
    public static final String GOV2_CURRENT_PATH = "/sys/devices/system/cpu/cpu2/cpufreq/scaling_governor";
    public static final String GOV3_CURRENT_PATH = "/sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
    //----------------------------------------------------------------------------------------------
    public static final String GOV_AVAIALBLE_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    //----------------------------------------------------------------------------------------------
    public static final String INTELLI_PLUG_PATH = "/sys/module/intelli_plug/parameters/intelli_plug_active";
    public static final String INTELLI_PLUG_ECO_MODE_PATH = "/sys/module/intelli_plug/parameters/eco_mode_active";
    public static final String INTELLI_PLUG_SUSPEND_PATH = "/sys/kernel/intelliplug/sleep_active_status";
    //----------------------------------------------------------------------------------------------
    public static final String CPU_TEMP_PATH = "/sys/class/thermal/thermal_zone0/temp";
    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String ACTION_FREQ_MAX = "action_freq_max";
    public static final String ACTION_FREQ_MIN = "action_freq_min";
    public static final String ACTION_GOV = "action_gov";

    //==============================================================================================
    // Methods
    //==============================================================================================
    public static void enableIntelliPlug(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_PATH, (enable ? "1" : "0"));
    }

    public static void enableIntelliPlugEcoMode(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_ECO_MODE_PATH, (enable ? "1" : "0"));
    }

    public static List<Integer> getAvailableFrequencies() {
        List<Integer> tmpList = new ArrayList<Integer>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(FREQ_AVAILABLE_PATH));
            String line;
            while ((line = br.readLine()) != null) {
                String[] nums = line.split(" ");
                tmpList.add(Integer.parseInt(nums[0]));
            }
        } catch (IOException e) {
        } finally {
            try {
                br.close();
            } catch (Exception exc) {
                // ignore
            }
        }

        return tmpList;
    }

    public static int getCpuFrequency(final int cpu) {
        String path;

        switch (cpu) {
            default:
            case 0:
                path = CPU0_FREQ_CURRENT_PATH;
                break;
            case 1:
                path = CPU1_FREQ_CURRENT_PATH;
                break;
            case 2:
                path = CPU2_FREQ_CURRENT_PATH;
                break;
            case 3:
                path = CPU3_FREQ_CURRENT_PATH;
                break;
        }

        return Integer.parseInt(Utils.readOneLine(path).trim());
    }

    public static int getCpuTemperature() {
        String tmpString = Utils.readOneLine(CPU_TEMP_PATH);
        tmpString = ((tmpString != null && !tmpString.trim().isEmpty()) ? tmpString : "-1");
        int temp = Integer.parseInt(tmpString);
        temp = (temp < 0 ? 0 : temp);
        temp = (temp > 100 ? 100 : temp);
        return temp;
    }

    public static List<Integer> getCurrentFrequencies() {
        final int cpuCount = getPossibleCpus();
        List<Integer> tmpList = new ArrayList<Integer>(cpuCount + 1);
        for (int i = 0; i <= cpuCount; i++) {
            tmpList.add(getCpuFrequency(i));
        }
        return tmpList;
    }

    public static boolean getIntelliPlugActive() {
        return Utils.readOneLine(INTELLI_PLUG_PATH).trim().equals("1");
    }

    public static boolean getIntelliPlugEcoMode() {
        return Utils.readOneLine(INTELLI_PLUG_ECO_MODE_PATH).trim().equals("1");
    }

    public static int getPossibleCpus() throws NumberFormatException {
        final String f = Utils.readOneLine(POSSIBLE_CPUS).trim();
        return Integer.parseInt(f.substring(f.length() - 1));
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
        String numOfCpus = Utils.readOneLine(PRESENT_CPUS);
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
        String pathOnline;

        switch (cpu) {
            default:
            case 0: // CPU 0
                if (action.equals(ACTION_FREQ_MAX)) {
                    path = FREQ0_MAX_PATH;
                } else if (action.equals(ACTION_FREQ_MIN)) {
                    path = FREQ0_MIN_PATH;
                } else if (action.equals(ACTION_GOV)) {
                    path = GOV0_CURRENT_PATH;
                }
                pathOnline = "";
                break;
            case 1: // CPU 1
                if (action.equals(ACTION_FREQ_MAX)) {
                    path = FREQ1_MAX_PATH;
                } else if (action.equals(ACTION_FREQ_MIN)) {
                    path = FREQ1_MIN_PATH;
                } else if (action.equals(ACTION_GOV)) {
                    path = GOV1_CURRENT_PATH;
                }
                pathOnline = CORE1_ONLINE;
                break;
            case 2: // CPU 2
                if (action.equals(ACTION_FREQ_MAX)) {
                    path = FREQ2_MAX_PATH;
                } else if (action.equals(ACTION_FREQ_MIN)) {
                    path = FREQ2_MIN_PATH;
                } else if (action.equals(ACTION_GOV)) {
                    path = GOV2_CURRENT_PATH;
                }
                pathOnline = CORE2_ONLINE;
                break;
            case 3: // CPU 3
                if (action.equals(ACTION_FREQ_MAX)) {
                    path = FREQ3_MAX_PATH;
                } else if (action.equals(ACTION_FREQ_MIN)) {
                    path = FREQ3_MIN_PATH;
                } else if (action.equals(ACTION_GOV)) {
                    path = GOV3_CURRENT_PATH;
                }
                pathOnline = CORE3_ONLINE;
                break;
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
                Shell.SU.run(sb.toString());
            }
        } else {
            if (!path.isEmpty() && Utils.fileExists(path)) {
                return Utils.readOneLine(path);
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
     * @param mhzString
     * @return tagged and converted String
     */
    public static String toMHz(final String mhzString) {
        return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }

}
