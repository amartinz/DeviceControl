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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils {

    //==============================================================================================
    // Paths
    //==============================================================================================
    public static final String POSSIBLE_CPUS = "/sys/devices/system/cpu/possible";
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
    // ---

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
        String path = "";

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
        int temp = Integer.parseInt(Utils.readOneLine(CPU_TEMP_PATH).trim());
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

}
