/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by alex on 18.12.13.
 */
public interface DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String TAG = "DeviceControl";

    //==============================================================================================
    // Actions
    //==============================================================================================

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
    public static final String KEY_NAVBAR_FORCE = "navbar_force";
    // Input
    public static final String KEY_GLOVE_MODE = "input_glove_mode";
    // Panel
    public static final String KEY_PANEL_COLOR_TEMP = "panel_color_temperature";
    // Performance Cpu
    public static final String KEY_CPU_MPDECISION = "toggle_mpdecision";

    //==============================================================================================
    // Categories
    //==============================================================================================
    public static final String CATEGORY_SENSORS = "sensors";
    public static final String CATEGORY_TOUCHSCREEN = "touchscreen";
    public static final String CATEGORY_TOUCHKEY = "touchkey";
    public static final String CATEGORY_GRAPHICS = "graphics";
    // Performance Cpu
    public static final String CATEGORY_HOTPLUT = "hotplug";

    //==============================================================================================
    // Files
    //==============================================================================================
    public static final String FILE_TOUCHKEY_TOGGLE =
            "/sys/class/leds/button-backlight/max_brightness";
    public static final String FILE_TOUCHKEY_BRIGHTNESS =
            "/sys/class/leds/button-backlight/brightness";
    public static final String FILE_BLN_TOGGLE =
            "/sys/class/misc/backlightnotification/enabled";
    public static final String FILE_KEYBOARD_TOGGLE =
            "/sys/class/leds/keyboard-backlight/max_brightness";
    public static final String FILE_USE_GYRO_CALIB = "/sys/class/sec/gsensorcal/calibration";
    public static final String FILE_VIBRATOR = "/sys/class/timed_output/vibrator/pwm_value";
    public static final String FILE_PANEL_COLOR_TEMP = "/sys/class/lcd/panel/panel_colors";
    //==============================================================================================
    public static final String FILE_INFO_DISPLAY_LCD_TYPE = "/sys/class/lcd/panel/lcd_type";
    //==============================================================================================
    public static final String FILE_FLASH_LIGHT = "/sys/class/camera/flash/rear_flash";
    //==============================================================================================
    public static final String FILE_MPDECISION = "/system/bin/mpdecision";
    //==============================================================================================
    public static final String JF_LOG_FILE_FSSTRIM =
            JfDirectories.JF_LOG_DIR + File.separator + "fstrim.log";

    //==============================================================================================
    // Values
    //==============================================================================================
    public static final int VIBRATOR_INTENSITY_MAX = 100;
    public static final int VIBRATOR_INTENSITY_MIN = 0;
    public static final int VIBRATOR_INTENSITY_DEFAULT_VALUE = 50;
    public static final int VIBRATOR_INTENSITY_WARNING_TRESHOLD = 75;

    //==============================================================================================
    // Preferences
    //==============================================================================================
    public static final String JF_EXTENSIVE_LOGGING = "jf_extensive_logging";

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

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static class JfDirectories {
        public static final String JF_DATA_DIR =
                Environment.getExternalStorageDirectory().getPath() + File.separator +
                        "DeviceControl";
        public static final String JF_LOG_DIR = JF_DATA_DIR + File.separator + "Logs";
        public static final String JF_BACKUP_DIR = JF_DATA_DIR + File.separator + "Backup";
    }

}
