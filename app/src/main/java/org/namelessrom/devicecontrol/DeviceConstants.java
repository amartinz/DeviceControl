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
package org.namelessrom.devicecontrol;

import org.namelessrom.devicecontrol.R;

import java.io.File;

public class DeviceConstants {

    //==============================================================================================
    // IDs
    //==============================================================================================
    public static final int ID_PGREP = Integer.MAX_VALUE - 1000;

    //==============================================================================================
    // Fragments
    //==============================================================================================
    public static final int ID_DEVICE_INFORMATION = R.string.device_information;
    public static final int ID_FEATURES = R.string.features;
    public static final int ID_FAST_CHARGE = ID_FEATURES + 1000;
    public static final int ID_SOUND_CONTROL = ID_FEATURES + 1100;
    public static final int ID_KSM = ID_FEATURES + 1200;
    public static final int ID_UKSM = ID_FEATURES + 1300;
    public static final int ID_VOLTAGE = ID_FEATURES + 1400;
    public static final int ID_ENTROPY = ID_FEATURES + 1500;
    public static final int ID_PERFORMANCE_INFO = R.string.information;
    public static final int ID_PERFORMANCE_CPU_SETTINGS = R.string.cpusettings;
    public static final int ID_GOVERNOR_TUNABLE = ID_PERFORMANCE_CPU_SETTINGS + 1000;
    public static final int ID_PERFORMANCE_GPU_SETTINGS = R.string.gpusettings;
    public static final int ID_FILESYSTEM = R.string.filesystem;
    public static final int ID_IOSCHED_TUNING = ID_FILESYSTEM + 1000;
    public static final int ID_THERMAL = R.string.thermal;
    public static final int ID_TOOLS_TASKER = R.string.tasker;
    public static final int ID_TOOLS_FLASHER = R.string.flasher;
    public static final int ID_TOOLS_MORE = R.string.more;
    public static final int ID_TOOLS_VM = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_EDITORS_VM = ID_TOOLS_MORE + 1020;
    public static final int ID_TOOLS_EDITORS_BUILD_PROP = ID_TOOLS_MORE + 1100;
    public static final int ID_TOOLS_APP_MANAGER = ID_TOOLS_MORE + 1200;
    public static final int ID_TOOLS_WIRELESS_FM = ID_TOOLS_MORE + 1300;
    public static final int ID_PREFERENCES = R.string.preferences;
    public static final int ID_ABOUT = R.string.about;
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR = File.separator + "Logs";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";
    //==============================================================================================
    public static final String DC_DOWNGRADE = File.separator + ".downgraded";
}
