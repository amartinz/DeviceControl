/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
    public static final int ID_VOLTAGE                  = ID_PERFORMANCE_EXTRA + 1200;
    public static final int ID_TOOLS_TASKER             = 9;
    public static final int ID_TOOLS_TASKER_LIST        = ID_TOOLS_TASKER + 1000;
    public static final int ID_TOOLS_MORE               = 10;
    public static final int ID_TOOLS_EDITORS            = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_APP_MANAGER        = ID_TOOLS_MORE + 1100;
    public static final int ID_PREFERENCES              = 12;
    public static final int ID_LICENSES                 = 13;
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Extras
    //==============================================================================================

    //==============================================================================================
    // Keys
    //==============================================================================================
    public static final String KEY_TOUCHKEY_LIGHT       = "touchkey_light";
    public static final String KEY_TOUCHKEY_BLN         = "touchkey_bln";
    public static final String KEY_KEYBOARD_LIGHT       = "keyboard_light";
    // Vibration
    public static final String KEY_VIBRATOR_TUNING      = "vibrator_tuning";
    public static final String KEY_NAVBAR_FORCE         = "navbar_force";
    // Input
    public static final String KEY_GLOVE_MODE           = "input_glove_mode";
    public static final String KEY_KNOCK_ON             = "knockon_gesture_enable";
    // Panel
    public static final String KEY_PANEL_COLOR_TEMP     = "panel_color_temperature";
    // Performance Cpu
    //==============================================================================================
    public static final String FORCE_HIGHEND_GFX_PREF   = "pref_force_highend_gfx";
    //==============================================================================================
    public static final String KEY_MPDECISION           = "mpdecision";
    public static final String GROUP_INTELLI_PLUG       = "intelli_plug";
    public static final String KEY_INTELLI_PLUG         = "intelli_plug_active";
    public static final String KEY_LCD_POWER_REDUCE     = "lcd_power_reduce";
    public static final String KEY_INTELLI_PLUG_ECO     = "intelli_plug_eco";
    public static final String KEY_MC_POWER_SCHEDULER   = "sched_mc_power_savings";
    public static final String KEY_POWER_EFFICIENT_WORK = "power_efficient_work";

    //==============================================================================================
    // Categories
    //==============================================================================================
    public static final String CATEGORY_TOUCHKEY    = "touchkey";
    public static final String CATEGORY_GRAPHICS    = "graphics";
    // Performance Cpu
    public static final String CATEGORY_POWERSAVING = "powersaving";

    //==============================================================================================
    // Values
    //==============================================================================================
    public static final int VIBRATOR_INTENSITY_MAX               = 100;
    public static final int VIBRATOR_INTENSITY_MIN               = 0;
    public static final int VIBRATOR_INTENSITY_DEFAULT_VALUE     = 50;
    public static final int VIBRATOR_INTENSITY_WARNING_THRESHOLD = 76;

    //==============================================================================================
    // Preferences
    //==============================================================================================
    public static final String EXTENSIVE_LOGGING = "extensive_logging";
    public static final String DC_FIRST_START    = "dc_first_start";
    public static final String SHOW_LAUNCHER     = "show_launcher";

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String FSTRIM          = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    //==============================================================================================
    // VM settings
    //==============================================================================================
    public static final String PREF_DIRTY_RATIO        = "pref_dirty_ratio";
    public static final String PREF_DIRTY_BACKGROUND   = "pref_dirty_background";
    public static final String PREF_DIRTY_EXPIRE       = "pref_dirty_expire";
    public static final String PREF_DIRTY_WRITEBACK    = "pref_dirty_writeback";
    public static final String PREF_MIN_FREE_KB        = "pref_min_free_kb";
    public static final String PREF_OVERCOMMIT         = "pref_overcommit";
    public static final String PREF_SWAPPINESS         = "pref_swappiness";
    public static final String PREF_VFS                = "pref_vfs";
    public static final String DIRTY_RATIO_PATH        = "/proc/sys/vm/dirty_ratio";
    public static final String DIRTY_BACKGROUND_PATH   = "/proc/sys/vm/dirty_background_ratio";
    public static final String DIRTY_EXPIRE_PATH       = "/proc/sys/vm/dirty_expire_centisecs";
    public static final String DIRTY_WRITEBACK_PATH    = "/proc/sys/vm/dirty_writeback_centisecs";
    public static final String MIN_FREE_PATH           = "/proc/sys/vm/min_free_kbytes";
    public static final String OVERCOMMIT_PATH         = "/proc/sys/vm/overcommit_ratio";
    public static final String SWAPPINESS_PATH         = "/proc/sys/vm/swappiness";
    public static final String VFS_CACHE_PRESSURE_PATH = "/proc/sys/vm/vfs_cache_pressure";

    //==============================================================================================
    // Etc
    //==============================================================================================
    public static final String PREF_FULL_EDITOR     = "pref_full_editor";
    public static final String CUSTOM_SHELL_COMMAND = "custom_shell_command";
    public static final String SOB_SYSCTL           = "sob_sysctl";
    public static final String SOB_VM               = "sob_vm";
    public static final String SOB_CPU              = "sob_cpu";
    public static final String SOB_GPU              = "sob_gpu";
    public static final String SOB_EXTRAS           = "sob_extras";
    public static final String SOB_DEVICE           = "sob_device";
    public static final String SOB_VOLTAGE          = "sob_voltage";

}
