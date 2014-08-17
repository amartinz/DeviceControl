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
package org.namelessrom.devicecontrol.sound.soundcontrol;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to control Faux123's SoundControl Kernel Modules
 */
public class SoundControlHelper {

    //=========================
    // Fields
    //=========================
    private static final String TAG = "SoundControlHelper";
    private static SoundControlHelper soundControlHelper;

    //=========================
    private static final ArrayList<Integer> mPresets = new ArrayList<Integer>();

    //=========================
    // Presets
    //=========================
    private static final int[] mPresetsList = {
            R.string.custom,
            R.string.quality,
            R.string.loudness,
            R.string.quiet,
            R.string.stock
    };

    private static final int[] mPresetQuality  = {33, 33, 40, 40, 33};
    private static final int[] mPresetLoudness = {50, 50, 40, 40, 40};
    private static final int[] mPresetQuiet    = {20, 20, 40, 40, 20};
    private static final int[] mPresetStock    = {40, 40, 40, 40, 40};

    //=========================
    // Paths
    //=========================
    private static String HEADPHONE_GAIN    =
            "/sys/kernel/sound_control/gpl_headphone_gain";
    private static String HEADPHONE_PA_GAIN =
            "/sys/kernel/sound_control/gpl_headphone_pa_gain";
    private static String MIC_GAIN          =
            "/sys/kernel/sound_control/gpl_mic_gain";
    private static String CAM_GAIN          =
            "/sys/kernel/sound_control/gpl_cam_mic_gain";
    private static String SPEAKER_GAIN      =
            "/sys/kernel/sound_control/gpl_speaker_gain";
    private static String VERSION           =
            "/sys/kernel/sound_control/gpl_sound_control_version";
    private static String LOCKED            =
            "/sys/kernel/sound_control_3/gpl_sound_control_locked";

    //=========================
    // Version Paths
    //=========================
    private static final String SC_GPL   =
            "/sys/kernel/sound_control/gpl_sound_control_version";
    private static final String SC_GPL_3 =
            "/sys/kernel/sound_control_3/gpl_sound_control_version";
    private static final String SC_3X    = "/sys/kernel/mm/tabla_mm/scv";

    private SoundControlHelper() {
        switch (getModuleVersion()) {
            default:
            case 0:
                break;
            case 1: // GPL
                HEADPHONE_GAIN = "/sys/kernel/sound_control/gpl_headphone_gain";
                HEADPHONE_PA_GAIN = "/sys/kernel/sound_control/gpl_headphone_pa_gain";
                MIC_GAIN = "/sys/kernel/sound_control/gpl_mic_gain";
                CAM_GAIN = "/sys/kernel/sound_control/gpl_cam_mic_gain";
                SPEAKER_GAIN = "/sys/kernel/sound_control/gpl_speaker_gain";
                VERSION = "/sys/kernel/sound_control/gpl_sound_control_version";
                LOCKED = "";
                break;
            case 2: // GPL 3
                HEADPHONE_GAIN = "/sys/kernel/sound_control_3/gpl_headphone_gain";
                HEADPHONE_PA_GAIN = "/sys/kernel/sound_control_3/gpl_headphone_pa_gain";
                MIC_GAIN = "/sys/kernel/sound_control_3/gpl_mic_gain";
                CAM_GAIN = "/sys/kernel/sound_control_3/gpl_cam_mic_gain";
                SPEAKER_GAIN = "/sys/kernel/sound_control_3/gpl_speaker_gain";
                VERSION = "/sys/kernel/sound_control_3/gpl_sound_control_version";
                LOCKED = "/sys/kernel/sound_control_3/gpl_sound_control_locked";
                break;
            case 3: // 3
                HEADPHONE_GAIN = "/sys/kernel/mm/tabla_mm/hpg";
                HEADPHONE_PA_GAIN = "/sys/kernel/mm/tabla_mm/hppg";
                MIC_GAIN = "/sys/kernel/mm/tabla_mm/mg";
                CAM_GAIN = "/sys/kernel/mm/tabla_mm/cmg";
                SPEAKER_GAIN = "/sys/kernel/mm/tabla_mm/sg";
                VERSION = "/sys/kernel/mm/tabla_mm/scv";
                LOCKED = "";
                break;
        }

        if (SoundControlHelper.mPresets.size() == 0) {
            loadPresets();
        }
    }

    /**
     * Get an instance of the SoundControlHelper
     *
     * @return An instance of the Sound Control Helper
     */
    public static SoundControlHelper getSoundControlHelper() {
        if (soundControlHelper == null) {
            soundControlHelper = new SoundControlHelper();
        }
        return soundControlHelper;
    }

    /**
     * Loads all presets
     */
    private void loadPresets() {
        if (mPresets.size() == 0) {
            mPresets.add(mPresetsList[0]);
            mPresets.add(mPresetsList[1]);
            mPresets.add(mPresetsList[2]);
            mPresets.add(mPresetsList[3]);
            mPresets.add(mPresetsList[4]);
        }
    }

    //=========================
    // Save and Load
    //=========================

    public void restore() {
        final int presetId = PreferenceHelper.getInt("preset_profile", 4);

        if (mPresets.size() == 0) {
            loadPresets();
        }

        switch (presetId) {
            default:
            case 0:
                final StringBuilder sbCmd = new StringBuilder();
                final List<DataItem> items = DatabaseHandler.getInstance().getAllItems(
                        DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_SOUND);
                for (DataItem item : items) {
                    sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
                }
                Utils.runRootCommand(sbCmd.toString());
            case 1:
                applyHeadphone(mPresetQuality[0], false);
                applyHeadphone(mPresetQuality[1], true);
                applyMicrophoneHandset(mPresetQuality[2]);
                applyMicrophoneCamcorder(mPresetQuality[3]);
                applySpeaker(mPresetQuality[4]);
                return;
            case 2:
                applyHeadphone(mPresetLoudness[0], false);
                applyHeadphone(mPresetLoudness[1], true);
                applyMicrophoneHandset(mPresetLoudness[2]);
                applyMicrophoneCamcorder(mPresetLoudness[3]);
                applySpeaker(mPresetLoudness[4]);
                return;
            case 3:
                applyHeadphone(mPresetQuiet[0], false);
                applyHeadphone(mPresetQuiet[1], true);
                applyMicrophoneHandset(mPresetQuiet[2]);
                applyMicrophoneCamcorder(mPresetQuiet[3]);
                applySpeaker(mPresetQuiet[4]);
                return;
            case 4:
                applyHeadphone(mPresetStock[0], false);
                applyHeadphone(mPresetStock[1], true);
                applyMicrophoneHandset(mPresetStock[2]);
                applyMicrophoneCamcorder(mPresetStock[3]);
                applySpeaker(mPresetStock[4]);
        }
    }

    //=========================
    // Apply
    //=========================

    public void applyHeadphone(final int paramInt, final boolean isRight) {
        final String value = String.format("%s %s", paramInt, readHeadphone(isRight));
        Utils.writeValue(HEADPHONE_GAIN, value);

        PreferenceHelper.setBootup(new DataItem(
                DatabaseHandler.CATEGORY_SOUND, "headphone_gain", HEADPHONE_GAIN, value));
    }

    public void applyMicrophoneHandset(final int paramInt) {
        final String value = String.valueOf(paramInt);
        Utils.writeValue(MIC_GAIN, value);

        PreferenceHelper.setBootup(new DataItem(
                DatabaseHandler.CATEGORY_SOUND, "handset_mic", MIC_GAIN, value));
    }

    public void applyMicrophoneCamcorder(final int paramInt) {
        final String value = String.valueOf(paramInt);
        Utils.writeValue(CAM_GAIN, value);

        PreferenceHelper.setBootup(new DataItem(
                DatabaseHandler.CATEGORY_SOUND, "camcorder_mic", CAM_GAIN, value));
    }

    public void applySpeaker(final int paramInt) {
        final String value = String.valueOf(paramInt);
        Utils.writeValue(SPEAKER_GAIN, value);

        PreferenceHelper.setBootup(new DataItem(
                DatabaseHandler.CATEGORY_SOUND, "speaker", SPEAKER_GAIN, value));
    }

    public void applyHeadphonePowerAmp(final int paramInt, final boolean isRight) {
        final String value = String.format("%s %s",
                paramInt, readHeadphonePowerAmp(isRight));
        Utils.writeValue(HEADPHONE_GAIN, value);

        PreferenceHelper.setBootup(new DataItem(
                DatabaseHandler.CATEGORY_SOUND, "headphone_gain_pa", HEADPHONE_PA_GAIN, value));
    }

    //=========================
    // Read
    //=========================

    public int readHeadphone(final boolean isRight) {
        final String str = Utils.readOneLine(HEADPHONE_GAIN);
        try {
            if (isRight) {
                return Integer.parseInt(str.split(" ")[1]);
            } else {
                return Integer.parseInt(str.split(" ")[0]);
            }
        } catch (Exception localException) {
            Logger.e(TAG, "bad str->int conversion!");
        }
        return 0;
    }

    public int readMicrophoneHandset() {
        final String str = Utils.readOneLine(MIC_GAIN);
        try {
            return Integer.parseInt(str);
        } catch (Exception localException) {
            Logger.e(TAG, "bad str->int conversion!");
        }
        return 0;
    }

    public int readMicrophoneCamcorder() {
        final String str = Utils.readOneLine(CAM_GAIN);
        try {
            return Integer.parseInt(str);
        } catch (Exception localException) {
            Logger.e(TAG, "bad str->int conversion!");
        }
        return 0;
    }

    public int readSpeaker() {
        final String str = Utils.readOneLine(SPEAKER_GAIN);
        try {
            return Integer.parseInt(str);
        } catch (Exception localException) {
            Logger.e(TAG, "bad str->int conversion!");
        }
        return 0;
    }

    public int readHeadphonePowerAmp(final boolean isRight) {
        final String str = Utils.readOneLine(HEADPHONE_PA_GAIN);
        try {
            if (isRight) {
                return Integer.parseInt(str.split(" ")[1]);
            } else {
                return Integer.parseInt(str.split(" ")[0]);
            }
        } catch (Exception localException) {
            Logger.e(TAG, "bad str->int conversion!");
        }
        return 0;
    }

    //=========================
    // Get
    //=========================

    /**
     * @return null if not existing
     */
    public boolean getHeadphone() { return Utils.readOneLine(HEADPHONE_GAIN) != null; }

    /**
     * @return null if not existing
     */
    public boolean getSpeaker() { return Utils.readOneLine(SPEAKER_GAIN) != null; }

    /**
     * @return null if not existing
     */
    public boolean getMicrophoneHandset() { return Utils.readOneLine(MIC_GAIN) != null; }

    /**
     * @return null if not existing
     */
    public boolean getMicrophoneCam() { return Utils.readOneLine(CAM_GAIN) != null; }

    /**
     * @return null if not existing
     */
    public boolean getHeadphonePa() { return Utils.readOneLine(HEADPHONE_PA_GAIN) != null; }

    /**
     * @return The Version of the Sound Control Module
     */
    public String getVersion() { return Utils.readOneLine(VERSION); }

    public int[] getPresetsList() { return mPresetsList; }

    //=========================
    // Static Methods
    //=========================

    /**
     * @return <b>true</b>, if SoundControl is supported
     */
    public static boolean isSupported() { return getModuleVersion() != 0; }

    //=========================
    // Methods
    //=========================

    /**
     * 0 = none <br />
     * 1 = GPL < 3 <br />
     * 2 = GPL Version 3+ <br />
     * 3 = Version 3+ <br />
     *
     * @return The Version of sound control or -1 if none
     */
    private static int getModuleVersion() {
        int i = 0;

        if (Utils.fileExists(SC_GPL)) {
            i = 1;
        }
        if (Utils.fileExists(SC_GPL_3)) {
            i = 0; //change to 2 once tested
        }
        if (Utils.fileExists(SC_3X)) {
            i = 0; //change to 3 once tested
        }

        return i;
    }

}
