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

import java.io.File;

public class DeviceConstants {

    //==============================================================================================
    // IDs
    //==============================================================================================
    public static final int ID_PGREP = Integer.MAX_VALUE - 1000;

    //==============================================================================================
    // Fragments
    //==============================================================================================
    public static final int ID_DEVICE_INFORMATION = 100000;
    public static final int ID_FEATURES = 200000;
    public static final int ID_FAST_CHARGE = ID_FEATURES + 1000;
    public static final int ID_SOUND_CONTROL = ID_FEATURES + 1100;
    public static final int ID_KSM = ID_FEATURES + 1200;
    public static final int ID_UKSM = ID_FEATURES + 1300;
    public static final int ID_VOLTAGE = ID_FEATURES + 1400;
    public static final int ID_ENTROPY = ID_FEATURES + 1500;
    public static final int ID_PERFORMANCE_INFO = 300000;
    public static final int ID_PERFORMANCE_CPU_SETTINGS = 400000;
    public static final int ID_GOVERNOR_TUNABLE = ID_PERFORMANCE_CPU_SETTINGS + 1000;
    public static final int ID_PERFORMANCE_GPU_SETTINGS = 500000;
    public static final int ID_FILESYSTEM = 600000;
    public static final int ID_IOSCHED_TUNING = ID_FILESYSTEM + 1000;
    public static final int ID_THERMAL = 700000;
    public static final int ID_TOOLS_APP_MANAGER = 800000;
    public static final int ID_TOOLS_TASKER = 900000;
    public static final int ID_TOOLS_FLASHER = 1000000;
    public static final int ID_TOOLS_MORE = 1100000;
    public static final int ID_TOOLS_VM = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_EDITORS_VM = ID_TOOLS_MORE + 1020;
    public static final int ID_TOOLS_EDITORS_BUILD_PROP = ID_TOOLS_MORE + 1100;
    public static final int ID_TOOLS_WIRELESS_FM = ID_TOOLS_MORE + 1300;
    public static final int ID_TOOLS_BOOTUP_RESTORATION = 1200000;
    public static final int ID_PREFERENCES = 8000000;
    public static final int ID_ABOUT = 9000000;
    //----------------------------------------------------------------------------------------------

    //==============================================================================================
    // Directories
    //==============================================================================================
    public static final String DC_LOG_DIR = File.separator + "Logs";
    //==============================================================================================
    public static final String DC_LOG_FILE_FSTRIM = DC_LOG_DIR + File.separator + "fstrim.log";

    //==============================================================================================
    // Donations
    //==============================================================================================
    /**
     * Donation  2€ *
     */
    public static final String SKU_DONATION_1 = "donation_1";
    /**
     * Donation  5€ *
     */
    public static final String SKU_DONATION_2 = "donation_2";
    /**
     * Donation 10€ *
     */
    public static final String SKU_DONATION_3 = "donation_3";
    /**
     * Donation 20€ *
     */
    public static final String SKU_DONATION_4 = "donation_4";
    /**
     * Donation 50€ *
     */
    public static final String SKU_DONATION_5 = "donation_5";

    /**
     * Donation, subscription  2€ *
     */
    public static final String SUB_DONATION_1 = "donation_sub_1";
    /**
     * Donation, subscription  5€ *
     */
    public static final String SUB_DONATION_2 = "donation_sub_2";
    /**
     * Donation, subscription 10€ *
     */
    public static final String SUB_DONATION_3 = "donation_sub_3";
    /**
     * Donation, subscription 20€ *
     */
    public static final String SUB_DONATION_4 = "donation_sub_4";
    /**
     * Donation, subscription 50€ *
     */
    public static final String SUB_DONATION_5 = "donation_sub_5";
}
