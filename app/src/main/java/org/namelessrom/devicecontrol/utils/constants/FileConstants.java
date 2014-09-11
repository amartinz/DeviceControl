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

public interface FileConstants {


    //==============================================================================================
    // Files
    //==============================================================================================
    public static final String   FILE_INFO_DISPLAY_LCD_TYPE = "/sys/class/lcd/panel/lcd_type";
    //==============================================================================================
    public static final String   FILE_FLASH_LIGHT           = "/sys/class/camera/flash/rear_flash";
    //==============================================================================================
    public static final String   MPDECISION_PATH            = "/system/bin/mpdecision";
    //==============================================================================================
    public static final String   RNG_PATH                   = "/system/bin/rngd";
    public static final String   QRNG_PATH                  = "/system/bin/qrngd";
    public static final String   RNG_STARTUP_PATH           = "/system/etc/init.d/90rng";
    //==============================================================================================

    public static final String URL_RNG =
            "http://sourceforge.net/projects/namelessrom/files/romextras/binaries/rngd/download";

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR         = File.separator + "Logs";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";
    //==============================================================================================
    public static final String DC_DOWNGRADE       = File.separator + ".downgraded";
    //==============================================================================================
}
