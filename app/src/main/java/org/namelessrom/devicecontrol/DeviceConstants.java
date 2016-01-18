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
    public static final int ID_HOME = R.id.nav_item_home;
    //--- INFO
    public static final int ID_INFO_DEVICE = R.id.nav_item_info_device;
    public static final int ID_INFO_PERFORMANCE = R.id.nav_item_info_performance;
    //--- CTRL
    public static final int ID_CTRL_DEVICE = R.id.nav_item_controls_device;
    public static final int ID_FAST_CHARGE = ID_CTRL_DEVICE + 1000;
    public static final int ID_SOUND_CONTROL = ID_CTRL_DEVICE + 1100;
    public static final int ID_KSM = ID_CTRL_DEVICE + 1200;
    public static final int ID_UKSM = ID_CTRL_DEVICE + 1300;
    public static final int ID_VOLTAGE = ID_CTRL_DEVICE + 1400;
    public static final int ID_ENTROPY = ID_CTRL_DEVICE + 1500;
    public static final int ID_CTRL_PROCESSOR = R.id.nav_item_controls_processor;
    public static final int ID_GOVERNOR_TUNABLE = ID_CTRL_PROCESSOR + 1000;
    public static final int ID_CTRL_GRAPHICS = R.id.nav_item_controls_graphics;
    public static final int ID_CTRL_FILE_SYSTEM = R.id.nav_item_controls_file_system;
    public static final int ID_IOSCHED_TUNING = ID_CTRL_FILE_SYSTEM + 1000;
    public static final int ID_CTRL_THERMAL = R.id.nav_item_controls_thermal;
    //--- TOOLS
    public static final int ID_TOOLS_BOOTUP_RESTORATION = R.id.nav_item_tools_bootup_restoration;
    public static final int ID_TOOLS_APP_MANAGER = R.id.nav_item_tools_app_manager;
    public static final int ID_TOOLS_TASKER = R.id.nav_item_tools_tasker;
    public static final int ID_TOOLS_FLASHER = R.id.nav_item_tools_flasher;
    public static final int ID_TOOLS_MORE = R.id.nav_item_tools_more;
    public static final int ID_TOOLS_VM = ID_TOOLS_MORE + 1000;
    public static final int ID_TOOLS_EDITORS_VM = ID_TOOLS_MORE + 1020;
    public static final int ID_TOOLS_EDITORS_BUILD_PROP = ID_TOOLS_MORE + 1100;
    public static final int ID_TOOLS_WIRELESS_FM = ID_TOOLS_MORE + 1300;
    //--- EXTRA
    public static final int ID_PREFERENCES = R.id.drawer_header_settings;
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
    /** Donation  2€ * */
    public static final String SKU_DONATION_1 = "donation_1";
    /** Donation  5€ * */
    public static final String SKU_DONATION_2 = "donation_2";
    /** Donation 10€ * */
    public static final String SKU_DONATION_3 = "donation_3";
    /** Donation 20€ * */
    public static final String SKU_DONATION_4 = "donation_4";
    /** Donation 50€ * */
    public static final String SKU_DONATION_5 = "donation_5";

    /** Donation, subscription  2€ * */
    public static final String SUB_DONATION_1 = "donation_sub_1";
    /** Donation, subscription  5€ */
    public static final String SUB_DONATION_2 = "donation_sub_2";
    /** Donation, subscription 10€ * */
    public static final String SUB_DONATION_3 = "donation_sub_3";
    /** Donation, subscription 20€ * */
    public static final String SUB_DONATION_4 = "donation_sub_4";
    /** Donation, subscription 50€ * */
    public static final String SUB_DONATION_5 = "donation_sub_5";
}
