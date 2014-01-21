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
    public static final String TAG = "DeviceControl";
    public static final String PACKAGE_NAME = "org.namelessrom.devicecontrol";

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
    public static final String KEY_USE_GYRO_CALIBRATION = "use_gyro_calibration";
    public static final String KEY_CALIBRATE_GYRO = "calibrate_gyro";
    public static final String KEY_TOUCHSCREEN_SENSITIVITY = "touchscreen_sensitivity";
    public static final String KEY_TOUCHKEY_LIGHT = "touchkey_light";
    public static final String KEY_TOUCHKEY_BLN = "touchkey_bln";
    public static final String KEY_KEYBOARD_LIGHT = "keyboard_light";
    // Vibration
    public static final String KEY_VIBRATOR_TUNING = "vibrator_tuning";
    // Check for Hardware Keys
    // If present, display option
    // If not (nexus, g2, etc) DO NOT display option for NavBar
    boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
        if(!hasMenuKey) {
    public static final String KEY_NAVBAR_FORCE = "navbar_force";
        }
    // Input
    public static final String KEY_GLOVE_MODE = "input_glove_mode";
    // Panel
    public static final String KEY_PANEL_COLOR_TEMP = "panel_color_temperature";
    // Performance Cpu
    public static final String KEY_LCD_POWER_REDUCE = "lcd_power_reduce";
    public static final String KEY_INTELLI_PLUG_ECO = "intelli_plug_eco";

    //==============================================================================================
    // Categories
    //==============================================================================================
    public static final String CATEGORY_SENSORS = "sensors";
    public static final String CATEGORY_TOUCHSCREEN = "touchscreen";
    public static final String CATEGORY_TOUCHKEY = "touchkey";
    public static final String CATEGORY_GRAPHICS = "graphics";
    // Performance Cpu
    public static final String CATEGORY_POWERSAVING = "powersaving";

    //==============================================================================================
    // Values
    //==============================================================================================
    public static final int VIBRATOR_INTENSITY_MAX = 100;
    public static final int VIBRATOR_INTENSITY_MIN = 0;
    public static final int VIBRATOR_INTENSITY_DEFAULT_VALUE = 50;
    public static final int VIBRATOR_INTENSITY_WARNING_THRESHOLD = 76;

    //==============================================================================================
    // Preferences
    //==============================================================================================
    public static final String JF_EXTENSIVE_LOGGING = "jf_extensive_logging";
    public static final String DC_FIRST_START = "dc_first_start";

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String TASKER_TOOLS_FSTRIM = "tasker_tools_fstrim";
    public static final String TASKER_TOOLS_FSTRIM_INTERVAL = "tasker_tools_fstrim_interval";

    // Etc
    public static final String CUSTOM_SHELL_COMMAND = "custom_shell_command";
    public static final String DYNAMIC_DIRTY_WRITEBACK_PATH =
            "/proc/sys/vm/dynamic_dirty_writeback";
    public static final String SYSCTL_SOB = "sysctl_sob";
    public static final String VM_SOB = "vm_sob";

}
