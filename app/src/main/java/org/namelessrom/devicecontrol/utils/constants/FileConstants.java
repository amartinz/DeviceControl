/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */
package org.namelessrom.devicecontrol.utils.constants;

import java.io.File;

public interface FileConstants {


    //==============================================================================================
    // Files
    //==============================================================================================
    public static final String[] FILES_VIBRATOR             = {
            "/sys/class/timed_output/vibrator/pwm_value",
            "/sys/devices/platform/tspdrv/nforce_timed",
            "/sys/vibrator/pwm_val"
    };
    public static final String   FILE_TOUCHKEY_TOGGLE       =
            "/sys/class/leds/button-backlight/max_brightness";
    public static final String   FILE_TOUCHKEY_BRIGHTNESS   =
            "/sys/class/leds/button-backlight/brightness";
    public static final String   FILE_BLN_TOGGLE            =
            "/sys/class/misc/backlightnotification/enabled";
    public static final String   FILE_KEYBOARD_TOGGLE       =
            "/sys/class/leds/keyboard-backlight/max_brightness";
    public static final String   FILE_USE_GYRO_CALIB        =
            "/sys/class/sec/gsensorcal/calibration";
    public static final String[] FILES_PANEL_COLOR_TEMP     = {
            "/sys/class/lcd/panel/panel_colors",
            "/sys/class/sec/tsp/panel_colors"
    };
    //==============================================================================================
    public static final String   FILE_KNOCKON               =
            "/sys/devices/virtual/input/lge_touch/touch_gesture";
    //==============================================================================================
    public static final String   FILE_INFO_DISPLAY_LCD_TYPE = "/sys/class/lcd/panel/lcd_type";
    //==============================================================================================
    public static final String   FILE_FLASH_LIGHT           = "/sys/class/camera/flash/rear_flash";
    //==============================================================================================
    public static final String[] FILES_LCD_POWER_REDUCE     = {"/sys/class/lcd/panel/power_reduce"};
    public static final String[] FILES_POWER_EFFICIENT_WORK =
            {"/sys/module/workqueue/parameters/power_efficient"};
    public static final String[] FILES_MC_POWER_SCHEDULER   =
            {"/sys/devices/system/cpu/sched_mc_power_savings"};
    //==============================================================================================
    public static final String   MPDECISION_PATH            = "/system/bin/mpdecision";
    //==============================================================================================
    public static final String   LOGGER_MODE_PATH           = "/sys/kernel/logger_mode/logger_mode";
    //==============================================================================================

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR         = File.separator + "Logs";
    public static final String DC_BACKUP_DIR      = File.separator + "Backup";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";
    //==============================================================================================
    public static final String DC_DOWNGRADE       = File.separator + ".downgraded";
    //==============================================================================================
}
