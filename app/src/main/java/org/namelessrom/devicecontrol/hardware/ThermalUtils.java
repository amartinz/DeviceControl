package org.namelessrom.devicecontrol.hardware;

public class ThermalUtils {
    //----------------------------------------------------------------------------------------------
    public static final String CPU_TEMP_PATH = "/sys/class/thermal/thermal_zone0/temp";

    //----------------------------------------------------------------------------------------------
    public static final String MSM_THERMAL_PARAMS = "/sys/module/msm_thermal/parameters/";
    public static final String MSM_THERMAL_MIN_CORE = MSM_THERMAL_PARAMS + "min_cpus_online";
    public static final String MSM_THERMAL_MAX_CORE = MSM_THERMAL_PARAMS + "max_cpus_online";
    public static final String MSM_THERMAL_CORE_TEMP_LIMIT = MSM_THERMAL_PARAMS + "core_limit_temp";
    public static final String MSM_THERMAL_TEMP_LIMIT = MSM_THERMAL_PARAMS + "temp_threshold";

    private static ThermalUtils sInstance;

    private ThermalUtils() { }

    public static ThermalUtils get() {
        if (sInstance == null) {
            sInstance = new ThermalUtils();
        }
        return sInstance;
    }

}
