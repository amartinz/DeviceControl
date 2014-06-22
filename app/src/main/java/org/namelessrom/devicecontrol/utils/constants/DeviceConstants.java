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

public interface DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String TAG          = "DeviceControl";
    public static final String PACKAGE_NAME = "org.namelessrom.devicecontrol";

    //==============================================================================================
    // IDs
    //==============================================================================================
    public static final int ID_PGREP = Integer.MAX_VALUE - 1000;

    //==============================================================================================
    // Fragments
    //==============================================================================================
    public static final int ID_HOME                     = -100;
    public static final int ID_DUMMY                    = -5;
    public static final int ID_FIRST_MENU               = -4;
    public static final int ID_SECOND_MENU              = -3;
    public static final int ID_RESTORE                  = -2;
    public static final int ID_RESTORE_FROM_SUB         = -1;
    //----------------------------------------------------------------------------------------------
    public static final int ID_DEVICE                   = 1;
    public static final int ID_FEATURES                 = 2;
    public static final int ID_FAST_CHARGE              = ID_FEATURES + 1000;
    public static final int ID_PERFORMANCE_INFO         = 4;
    public static final int ID_PERFORMANCE_CPU_SETTINGS = 5;
    public static final int ID_GOVERNOR_TUNABLE         = ID_PERFORMANCE_CPU_SETTINGS + 1000;
    public static final int ID_PERFORMANCE_GPU_SETTINGS = 6;
    public static final int ID_PERFORMANCE_EXTRA        = 7;
    public static final int ID_HOTPLUGGING              = ID_PERFORMANCE_EXTRA + 1000;
    public static final int ID_THERMAL                  = ID_PERFORMANCE_EXTRA + 1100;
    public static final int ID_KSM                      = ID_PERFORMANCE_EXTRA + 1200;
    public static final int ID_VOLTAGE                  = ID_PERFORMANCE_EXTRA + 1300;
    public static final int ID_ENTROPY                  = ID_PERFORMANCE_EXTRA + 1400;
    public static final int ID_TOOLS_TASKER             = 9;
    public static final int ID_TOOLS_TASKER_LIST        = ID_TOOLS_TASKER + 1000;
    public static final int ID_TOOLS_FLASHER            = 10;
    public static final int ID_TOOLS_MORE               = 11;
    public static final int ID_TOOLS_VM                 = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_EDITORS_VM         = ID_TOOLS_MORE + 1010;
    public static final int ID_TOOLS_BUILD_PROP         = ID_TOOLS_MORE + 1100;
    public static final int ID_TOOLS_EDITORS_BUILD_PROP = ID_TOOLS_MORE + 1110;
    public static final int ID_TOOLS_APP_MANAGER        = ID_TOOLS_MORE + 1200;
    public static final int ID_TOOLS_WIRELESS_FM        = ID_TOOLS_MORE + 1300;
    public static final int ID_PREFERENCES              = 13;
    public static final int ID_LICENSES                 = 14;
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String FSTRIM          = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    //==============================================================================================
    // Etc
    //==============================================================================================
    public static final String USE_TASKER           = "use_tasker";
    //----------------------------------------------------------------------------------------------
    public static final String EXTENSIVE_LOGGING    = "extensive_logging";
    public static final String DC_FIRST_START       = "dc_first_start";
    public static final String SHOW_LAUNCHER        = "show_launcher";
    public static final String SKIP_CHECKS          = "skip_checks";
    //----------------------------------------------------------------------------------------------
    public static final String PREF_RECOVERY_TYPE   = "pref_recovery_type";
    public static final int    RECOVERY_TYPE_BOTH   = 0;
    public static final int    RECOVERY_TYPE_CWM    = 1;
    public static final int    RECOVERY_TYPE_OPEN   = 2;
    //----------------------------------------------------------------------------------------------
    public static final String CUSTOM_SHELL_COMMAND = "custom_shell_command";
    public static final String SOB_SYSCTL           = "sob_sysctl";
    public static final String SOB_CPU              = "sob_cpu";
    public static final String SOB_GPU              = "sob_gpu";
    public static final String SOB_EXTRAS           = "sob_extras";
    public static final String SOB_DEVICE           = "sob_device";
    public static final String SOB_VOLTAGE          = "sob_voltage";

}
