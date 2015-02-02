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
package org.namelessrom.devicecontrol.utils.constants;

import java.io.File;

public interface DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String TAG = "DeviceControl";

    //==============================================================================================
    // IDs
    //==============================================================================================
    public static final int ID_PGREP = Integer.MAX_VALUE - 1000;

    //==============================================================================================
    // Fragments
    //==============================================================================================
    public static final String ID_DEVICE = "id_device";
    public static final String ID_DEVICE_INFORMATION = "id_device_information";
    public static final String ID_FEATURES = "id_features";
    public static final String ID_FAST_CHARGE = "id_fast_charge";
    public static final String ID_SOUND_CONTROL = "id_sound_control";

    public static final String ID_PERFORMANCE = "id_performance";
    public static final String ID_PERFORMANCE_INFO = "id_performance_information";
    public static final String ID_CPU_SETTINGS = "id_cpu_settings";
    public static final String ID_GOVERNOR_TUNABLE = "id_governor_tunable";
    public static final String ID_GPU_SETTINGS = "id_gpu_settings";
    public static final String ID_PERFORMANCE_EXTRA = "id_performance_extras";
    public static final String ID_THERMAL = "id_thermal";
    public static final String ID_KSM = "id_ksm";
    public static final String ID_UKSM = "id_uksm";
    public static final String ID_VOLTAGE = "id_voltage";
    public static final String ID_ENTROPY = "id_entropy";
    public static final String ID_FILESYSTEM = "id_filesystem";
    public static final String ID_IOSCHED_TUNING = "id_iosched_tuning";

    public static final String ID_TOOLS = "id_tools";
    public static final String ID_TOOLS_TASKER = "id_tools_tasker";
    public static final String ID_TOOLS_FLASHER = "id_tools_flasher";
    public static final String ID_TOOLS_MORE = "id_tools_more";
    public static final String ID_TOOLS_VM = "id_tools_vm";
    public static final String ID_TOOLS_EDITORS_VM = "id_tools_editors_vm";
    public static final String ID_TOOLS_BUILD_PROP = "id_tools_build_prop";
    public static final String ID_TOOLS_EDITORS_BUILD_PROP = "id_tools_editors_build_prop";
    public static final String ID_TOOLS_APP_MANAGER = "id_tools_app_manager";
    public static final String ID_TOOLS_WIRELESS_FM = "id_tools_wireless_fm";

    public static final String ID_PREFERENCES = "id_preferences";
    public static final String ID_ABOUT = "id_about";
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String FSTRIM = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    //==============================================================================================
    // Etc
    //==============================================================================================
    public static final String USE_TASKER = "use_tasker";

    //----------------------------------------------------------------------------------------------
    public static final String EXTENSIVE_LOGGING = "extensive_logging";
    public static final String DC_FIRST_START = "dc_first_start";
    public static final String SHOW_LAUNCHER = "show_launcher";
    public static final String SKIP_CHECKS = "skip_checks";

    //----------------------------------------------------------------------------------------------
    public static final String PREF_RECOVERY_TYPE = "pref_recovery_type";
    public static final int RECOVERY_TYPE_BOTH = 0;
    public static final int RECOVERY_TYPE_CWM = 1;
    public static final int RECOVERY_TYPE_OPEN = 2;

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR = File.separator + "Logs";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";
    //==============================================================================================
    public static final String DC_DOWNGRADE = File.separator + ".downgraded";
}
