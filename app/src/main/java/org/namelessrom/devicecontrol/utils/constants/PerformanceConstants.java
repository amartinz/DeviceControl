package org.namelessrom.devicecontrol.utils.constants;

public interface PerformanceConstants {

    //==============================================================================================
    // Paths
    //==============================================================================================
    public static final String   POSSIBLE_CPUS               = "/sys/devices/system/cpu/possible";
    public static final String   PRESENT_CPUS                = "/sys/devices/system/cpu/present";
    public static final String   CORE1_ONLINE                =
            "/sys/devices/system/cpu/cpu1/online";
    public static final String   CORE2_ONLINE                =
            "/sys/devices/system/cpu/cpu2/online";
    public static final String   CORE3_ONLINE                =
            "/sys/devices/system/cpu/cpu3/online";
    //----------------------------------------------------------------------------------------------
    public static final String   CPU0_FREQ_CURRENT_PATH      =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    public static final String   CPU1_FREQ_CURRENT_PATH      =
            "/sys/devices/system/cpu/cpu1/cpufreq/scaling_cur_freq";
    public static final String   CPU2_FREQ_CURRENT_PATH      =
            "/sys/devices/system/cpu/cpu2/cpufreq/scaling_cur_freq";
    public static final String   CPU3_FREQ_CURRENT_PATH      =
            "/sys/devices/system/cpu/cpu3/cpufreq/scaling_cur_freq";
    //----------------------------------------------------------------------------------------------
    public static final String   FREQ0_INFO_MAX_PATH         =
            "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
    public static final String   FREQ0_INFO_MIN_PATH         =
            "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
    public static final String   FREQ0_MAX_PATH              =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
    public static final String   FREQ0_MIN_PATH              =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
    public static final String   FREQ1_INFO_MAX_PATH         =
            "/sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_max_freq";
    public static final String   FREQ1_INFO_MIN_PATH         =
            "/sys/devices/system/cpu/cpu1/cpufreq/cpuinfo_min_freq";
    public static final String   FREQ1_MAX_PATH              =
            "/sys/devices/system/cpu/cpu1/cpufreq/scaling_max_freq";
    public static final String   FREQ1_MIN_PATH              =
            "/sys/devices/system/cpu/cpu1/cpufreq/scaling_min_freq";
    public static final String   FREQ2_INFO_MAX_PATH         =
            "/sys/devices/system/cpu/cpu2/cpufreq/cpuinfo_max_freq";
    public static final String   FREQ2_INFO_MIN_PATH         =
            "/sys/devices/system/cpu/cpu2/cpufreq/cpuinfo_min_freq";
    public static final String   FREQ2_MAX_PATH              =
            "/sys/devices/system/cpu/cpu2/cpufreq/scaling_max_freq";
    public static final String   FREQ2_MIN_PATH              =
            "/sys/devices/system/cpu/cpu2/cpufreq/scaling_min_freq";
    public static final String   FREQ3_INFO_MAX_PATH         =
            "/sys/devices/system/cpu/cpu3/cpufreq/cpuinfo_max_freq";
    public static final String   FREQ3_INFO_MIN_PATH         =
            "/sys/devices/system/cpu/cpu3/cpufreq/cpuinfo_min_freq";
    public static final String   FREQ3_MAX_PATH              =
            "/sys/devices/system/cpu/cpu3/cpufreq/scaling_max_freq";
    public static final String   FREQ3_MIN_PATH              =
            "/sys/devices/system/cpu/cpu3/cpufreq/scaling_min_freq";
    //----------------------------------------------------------------------------------------------
    public static final String   FREQ_AVAILABLE_PATH         =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
    public static final String   FREQ_TIME_IN_STATE_PATH     =
            "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
    //----------------------------------------------------------------------------------------------
    public static final String   GOV0_CURRENT_PATH           =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
    public static final String   GOV1_CURRENT_PATH           =
            "/sys/devices/system/cpu/cpu1/cpufreq/scaling_governor";
    public static final String   GOV2_CURRENT_PATH           =
            "/sys/devices/system/cpu/cpu2/cpufreq/scaling_governor";
    public static final String   GOV3_CURRENT_PATH           =
            "/sys/devices/system/cpu/cpu3/cpufreq/scaling_governor";
    //----------------------------------------------------------------------------------------------
    public static final String   GOV_AVAILALBLE_PATH         =
            "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors";
    //----------------------------------------------------------------------------------------------
    public static final String   INTELLI_PLUG_PATH           =
            "/sys/module/intelli_plug/parameters/intelli_plug_active";
    public static final String   INTELLI_PLUG_ECO_MODE_PATH  =
            "/sys/module/intelli_plug/parameters/eco_mode_active";
    public static final String   INTELLI_PLUG_SUSPEND_PATH   =
            "/sys/kernel/intelliplug/sleep_active_status";
    //----------------------------------------------------------------------------------------------
    public static final String   CPU_TEMP_PATH               =
            "/sys/class/thermal/thermal_zone0/temp";
    //----------------------------------------------------------------------------------------------
    public static final String[] IO_SCHEDULER_PATH           = {
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/mmcblk1/queue/scheduler"};
    //----------------------------------------------------------------------------------------------
    public static final String   GPU_FOLDER                  = "/sys/class/kgsl";
    public static final String   GPU_FREQUENCIES_FILE        =
            GPU_FOLDER + "/kgsl-3d0/gpu_available_frequencies";
    public static final String   GPU_MAX_FREQ_FILE           = GPU_FOLDER + "/kgsl-3d0/max_gpuclk";
    public static final String   GPU_GOV_PATH                =
            GPU_FOLDER + "/kgsl-3d0/pwrscale/trustzone/governor";
    //----------------------------------------------------------------------------------------------
    public static final String[] GPU_GOVS                    =
            {"performance", "simple", "ondemand", "interactive"};
    //----------------------------------------------------------------------------------------------
    public static final String   MSM_DCVS_FILE               =
            "/sys/module/msm_dcvs/parameters/enable";
    //----------------------------------------------------------------------------------------------
    public static final String   UV_TABLE_FILE               =
            "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
    public static final String   VDD_TABLE_FILE              =
            "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
    //----------------------------------------------------------------------------------------------
    public static final String   MSM_THERMAL_TEMP_LIMIT      =
            "/sys/module/msm_thermal/parameters/temp_threshold";
    public static final String   MSM_THERMAL_CORE_TEMP_LIMIT =
            "/sys/module/msm_thermal/parameters/core_limit_temp";
    public static final String   MSM_THERMAL_MAX_CORE        =
            "/sys/module/msm_thermal/parameters/max_cpus_online";
    public static final String   MSM_THERMAL_MIN_CORE        =
            "/sys/module/msm_thermal/parameters/min_cpus_online";
    //----------------------------------------------------------------------------------------------
    public static final String   INTELLI_THERMAL_CC_ENABLED  =
            "/sys/module/msm_intelli_thermal/core_control/enabled";
    public static final String   INTELLI_THERMAL_ENABLED     =
            "/sys/module/msm_intelli_thermal/parameters/enabled";
    //----------------------------------------------------------------------------------------------
    public static final String   KSM_PATH                    = "/sys/kernel/mm/ksm/";
    public static final String   KSM_FULL_SCANS              = KSM_PATH + "full_scans";
    public static final String   KSM_PAGES_SHARE             = KSM_PATH + "pages_shared";
    public static final String   KSM_PAGES_SHARING           = KSM_PATH + "pages_sharing";
    public static final String   KSM_PAGES_TO_SCAN           = KSM_PATH + "pages_to_scan";
    public static final String   KSM_PAGES_UNSHARE           = KSM_PATH + "pages_unshared";
    public static final String   KSM_PAGES_VOLATILE          = KSM_PATH + "pages_volatile";
    public static final String   KSM_RUN                     = KSM_PATH + "run";
    public static final String   KSM_SLEEP                   = KSM_PATH + "sleep_millisecs";
    //----------------------------------------------------------------------------------------------
    public static final String   PREF_HIDE_CPU_INFO          = "pref_hide_cpu_info";
    public static final String   PREF_INTERVAL_CPU_INFO      = "pref_interval_cpu_info";
    public static final String   PREF_MAX_CPU                = "pref_max_cpu";
    public static final String   PREF_MIN_CPU                = "pref_min_cpu";
    public static final String   PREF_GOV                    = "pref_gov";
    public static final String   PREF_IO                     = "pref_io";
    public static final String   PREF_MAX_GPU                = "pref_max_gpu";
    public static final String   PREF_GPU_GOV                = "pref_gpu_gov";
    //----------------------------------------------------------------------------------------------
    public static final String   PREF_3D_SCALING             = "3d_scaling";
    public static final String   FILE_3D_SCALING             =
            "/sys/devices/gr3d/enable_3d_scaling";
}

