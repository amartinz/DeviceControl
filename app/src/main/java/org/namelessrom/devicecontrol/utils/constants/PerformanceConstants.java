package org.namelessrom.devicecontrol.utils.constants;

public interface PerformanceConstants {

    //==============================================================================================
    // Paths
    //==============================================================================================
    public static final String CPU_BASE      = "/sys/devices/system/cpu/";
    public static final String POSSIBLE_CPUS = CPU_BASE + "possible";
    public static final String PRESENT_CPUS  = CPU_BASE + "present";
    public static final String CORE1_ONLINE  = CPU_BASE + "cpu1/online";
    public static final String CORE2_ONLINE  = CPU_BASE + "cpu2/online";
    public static final String CORE3_ONLINE  = CPU_BASE + "cpu3/online";

    //----------------------------------------------------------------------------------------------
    public static final String FREQ_AVAILABLE_PATH     =
            CPU_BASE + "cpu0/cpufreq/scaling_available_frequencies";
    public static final String FREQ_TIME_IN_STATE_PATH =
            CPU_BASE + "cpu0/cpufreq/stats/time_in_state";

    //----------------------------------------------------------------------------------------------
    public static final String CPU0_FREQ_CURRENT_PATH = CPU_BASE + "cpu0/cpufreq/scaling_cur_freq";
    public static final String CPU1_FREQ_CURRENT_PATH = CPU_BASE + "cpu1/cpufreq/scaling_cur_freq";
    public static final String CPU2_FREQ_CURRENT_PATH = CPU_BASE + "cpu2/cpufreq/scaling_cur_freq";
    public static final String CPU3_FREQ_CURRENT_PATH = CPU_BASE + "cpu3/cpufreq/scaling_cur_freq";

    //----------------------------------------------------------------------------------------------
    public static final String FREQ0_INFO_MAX_PATH = CPU_BASE + "cpu0/cpufreq/cpuinfo_max_freq";
    public static final String FREQ0_INFO_MIN_PATH = CPU_BASE + "cpu0/cpufreq/cpuinfo_min_freq";
    public static final String FREQ0_MAX_PATH      = CPU_BASE + "cpu0/cpufreq/scaling_max_freq";
    public static final String FREQ0_MIN_PATH      = CPU_BASE + "cpu0/cpufreq/scaling_min_freq";
    public static final String FREQ1_INFO_MAX_PATH = CPU_BASE + "cpu1/cpufreq/cpuinfo_max_freq";
    public static final String FREQ1_INFO_MIN_PATH = CPU_BASE + "cpu1/cpufreq/cpuinfo_min_freq";
    public static final String FREQ1_MAX_PATH      = CPU_BASE + "cpu1/cpufreq/scaling_max_freq";
    public static final String FREQ1_MIN_PATH      = CPU_BASE + "cpu1/cpufreq/scaling_min_freq";
    public static final String FREQ2_INFO_MAX_PATH = CPU_BASE + "cpu2/cpufreq/cpuinfo_max_freq";
    public static final String FREQ2_INFO_MIN_PATH = CPU_BASE + "cpu2/cpufreq/cpuinfo_min_freq";
    public static final String FREQ2_MAX_PATH      = CPU_BASE + "cpu2/cpufreq/scaling_max_freq";
    public static final String FREQ2_MIN_PATH      = CPU_BASE + "cpu2/cpufreq/scaling_min_freq";
    public static final String FREQ3_INFO_MAX_PATH = CPU_BASE + "cpu3/cpufreq/cpuinfo_max_freq";
    public static final String FREQ3_INFO_MIN_PATH = CPU_BASE + "cpu3/cpufreq/cpuinfo_min_freq";
    public static final String FREQ3_MAX_PATH      = CPU_BASE + "cpu3/cpufreq/scaling_max_freq";
    public static final String FREQ3_MIN_PATH      = CPU_BASE + "cpu3/cpufreq/scaling_min_freq";

    //----------------------------------------------------------------------------------------------
    public static final String GOV0_CURRENT_PATH = CPU_BASE + "cpu0/cpufreq/scaling_governor";
    public static final String GOV1_CURRENT_PATH = CPU_BASE + "cpu1/cpufreq/scaling_governor";
    public static final String GOV2_CURRENT_PATH = CPU_BASE + "cpu2/cpufreq/scaling_governor";
    public static final String GOV3_CURRENT_PATH = CPU_BASE + "cpu3/cpufreq/scaling_governor";

    //----------------------------------------------------------------------------------------------
    public static final String GOV_AVAILALBLE_PATH =
            CPU_BASE + "cpu0/cpufreq/scaling_available_governors";

    //----------------------------------------------------------------------------------------------
    public static final String INTELLI_PLUG_BASE          = "/sys/module/intelli_plug/";
    public static final String INTELLI_PLUG_PATH          =
            INTELLI_PLUG_BASE + "parameters/intelli_plug_active";
    public static final String INTELLI_PLUG_ECO_MODE_PATH =
            INTELLI_PLUG_BASE + "parameters/eco_mode_active";
    public static final String INTELLI_PLUG_SUSPEND_PATH  =
            "/sys/kernel/intelliplug/sleep_active_status";

    //----------------------------------------------------------------------------------------------
    public static final String CPU_TEMP_PATH = "/sys/class/thermal/thermal_zone0/temp";

    //----------------------------------------------------------------------------------------------
    public static final String[] IO_SCHEDULER_PATH = {
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/mmcblk1/queue/scheduler"};

    //----------------------------------------------------------------------------------------------
    public static final String GPU_FOLDER           = "/sys/class/kgsl";
    public static final String GPU_MAX_FREQ_FILE    = GPU_FOLDER + "/kgsl-3d0/max_gpuclk";
    public static final String GPU_FREQUENCIES_FILE =
            GPU_FOLDER + "/kgsl-3d0/gpu_available_frequencies";
    public static final String GPU_GOV_PATH         =
            GPU_FOLDER + "/kgsl-3d0/pwrscale/trustzone/governor";

    //----------------------------------------------------------------------------------------------
    public static final String[] GPU_GOVS = {"performance", "simple", "ondemand", "interactive"};

    //----------------------------------------------------------------------------------------------
    public static final String MSM_DCVS_FILE = "/sys/module/msm_dcvs/parameters/enable";

    //----------------------------------------------------------------------------------------------
    public static final String UV_TABLE_FILE  = CPU_BASE + "cpu0/cpufreq/UV_mV_table";
    public static final String VDD_TABLE_FILE = CPU_BASE + "cpufreq/vdd_table/vdd_levels";

    //----------------------------------------------------------------------------------------------
    public static final String MSM_THERMAL_PARAMS          = "/sys/module/msm_thermal/parameters/";
    public static final String MSM_THERMAL_TEMP_LIMIT      = MSM_THERMAL_PARAMS + "temp_threshold";
    public static final String MSM_THERMAL_CORE_TEMP_LIMIT = MSM_THERMAL_PARAMS + "core_limit_temp";
    public static final String MSM_THERMAL_MAX_CORE        = MSM_THERMAL_PARAMS + "max_cpus_online";
    public static final String MSM_THERMAL_MIN_CORE        = MSM_THERMAL_PARAMS + "min_cpus_online";

    //----------------------------------------------------------------------------------------------
    public static final String INTELLI_THERMAL_BASE       = "/sys/module/msm_intelli_thermal/";
    public static final String INTELLI_THERMAL_CC_ENABLED =
            INTELLI_THERMAL_BASE + "core_control/enabled";
    public static final String INTELLI_THERMAL_ENABLED    =
            INTELLI_THERMAL_BASE + "parameters/enabled";

    //----------------------------------------------------------------------------------------------
    public static final String KSM_PATH           = "/sys/kernel/mm/ksm/";
    public static final String KSM_DEFERRED       = KSM_PATH + "deferred_timer";
    public static final String KSM_FULL_SCANS     = KSM_PATH + "full_scans";
    public static final String KSM_PAGES_SHARED   = KSM_PATH + "pages_shared";
    public static final String KSM_PAGES_SHARING  = KSM_PATH + "pages_sharing";
    public static final String KSM_PAGES_TO_SCAN  = KSM_PATH + "pages_to_scan";
    public static final String KSM_PAGES_UNSHARED = KSM_PATH + "pages_unshared";
    public static final String KSM_PAGES_VOLATILE = KSM_PATH + "pages_volatile";
    public static final String KSM_RUN            = KSM_PATH + "run";
    public static final String KSM_SLEEP          = KSM_PATH + "sleep_millisecs";

    //----------------------------------------------------------------------------------------------
    public static final String ENTROPY_AVAIL = "/proc/sys/kernel/random/entropy_avail";

    //----------------------------------------------------------------------------------------------
    public static final String PREF_3D_SCALING = "3d_scaling";
    public static final String FILE_3D_SCALING = "/sys/devices/gr3d/enable_3d_scaling";
}

