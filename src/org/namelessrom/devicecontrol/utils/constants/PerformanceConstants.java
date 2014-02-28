package org.namelessrom.devicecontrol.utils.constants;

/**
 * Created by alex on 28.02.14.
 */
public interface PerformanceConstants {

    public static final String CPU_PATH = "/sys/devices/system/cpu/cpu";
    public static final String CPU_FREQ_TAIL = "/cpufreq/scaling_cur_freq";
    public static final String STEPS_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String[] IO_SCHEDULER_PATH = {"/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/mmcblk1/queue/scheduler"};
    // Preferences
    public static final String PREF_MAX_CPU = "pref_max_cpu";
    public static final String PREF_MIN_CPU = "pref_min_cpu";
    public static final String PREF_GOV = "pref_gov";
    public static final String PREF_IO = "pref_io";
    public static final String CPU_SOB = "cpu_sob";
    public static final String GOV_SOB = "gov_settings_sob";
    public static final String GOV_SETTINGS = "gov_settings";
    public static final String GOV_NAME = "gov_name";
    public static final String GOV_SETTINGS_PATH = "/sys/devices/system/cpu/cpufreq/";
}

