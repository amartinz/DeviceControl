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
    // Actions
    //==============================================================================================
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    //==============================================================================================
    // Extras
    //==============================================================================================

    //==============================================================================================
    // Keys
    //==============================================================================================
    public static final String KEY_TOUCHKEY_LIGHT     = "touchkey_light";
    public static final String KEY_TOUCHKEY_BLN       = "touchkey_bln";
    public static final String KEY_KEYBOARD_LIGHT     = "keyboard_light";
    // Vibration
    public static final String KEY_VIBRATOR_TUNING    = "vibrator_tuning";
    public static final String KEY_NAVBAR_FORCE       = "navbar_force";
    // Input
    public static final String KEY_GLOVE_MODE         = "input_glove_mode";
    public static final String KEY_KNOCK_ON           = "knockon_gesture_enable";
    // Panel
    public static final String KEY_PANEL_COLOR_TEMP   = "panel_color_temperature";
    // Performance Cpu
    //==============================================================================================
    public static final String FORCE_HIGHEND_GFX_PREF = "pref_force_highend_gfx";
    //==============================================================================================
    public static final String GROUP_INTELLI_PLUG     = "intelli_plug";
    public static final String KEY_INTELLI_PLUG       = "intelli_plug_active";
    public static final String KEY_LCD_POWER_REDUCE   = "lcd_power_reduce";
    public static final String KEY_INTELLI_PLUG_ECO   = "intelli_plug_eco";
    public static final String KEY_MC_POWER_SCHEDULER = "sched_mc_power_savings";

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

    //==============================================================================================
    // Tasker
    //==============================================================================================
    public static final String FSTRIM          = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    //==============================================================================================
    // Handler Actions
    //==============================================================================================
    public static final int    READ_VALUE_ACTION_RESULT = 0x1000;
    public static final String READ_VALUE_ACTION        = "read_value_actopn";
    public static final String READ_VALUE_TEXT          = "read_value_text";

    //==============================================================================================
    // Etc
    //==============================================================================================
    public static final String CUSTOM_SHELL_COMMAND = "custom_shell_command";
    public static final String SOB_SYSCTL           = "sob_sysctl";
    public static final String SOB_VM               = "sob_vm";
    public static final String SOB_CPU              = "sob_cpu";

}
