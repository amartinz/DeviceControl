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
package org.namelessrom.devicecontrol.device;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.hardware.DisplayColorCalibration;
import org.namelessrom.devicecontrol.hardware.DisplayGammaCalibration;
import org.namelessrom.devicecontrol.objects.ShellOutput;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeListPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.hardware.DisplayColor;
import org.namelessrom.devicecontrol.ui.preferences.hardware.DisplayGamma;
import org.namelessrom.devicecontrol.ui.preferences.hardware.VibratorIntensity;
import org.namelessrom.devicecontrol.ui.views.CustomPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.DeviceConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class DeviceFeatureGeneralFragment extends CustomPreferenceFragment implements Preference.OnPreferenceClickListener, ShellOutput.OnShellOutputListener {

    private static final String FC_PATH = "/sys/kernel/fast_charge";

    private static final String[] SOUND_CONTROL_PATHS = new String[]{
            "/sys/devices/virtual/misc/soundcontrol"
    };

    //==============================================================================================
    // Input
    //==============================================================================================
    private CustomTogglePreference mGloveMode;
    private AwesomeTogglePreference mAwesomeGloveMode;
    private AwesomeTogglePreference mKnockOn;
    private AwesomeTogglePreference mResetOnSuspend;

    //==============================================================================================
    // Lights
    //==============================================================================================
    private AwesomeTogglePreference mBacklightKey;
    private AwesomeTogglePreference mBacklightNotification;
    private AwesomeTogglePreference mKeyboardBacklight;

    //==============================================================================================
    // Display
    //==============================================================================================
    private AwesomeTogglePreference mLcdPowerReduce;
    private AwesomeTogglePreference mLcdSunlightEnhancement;
    private AwesomeTogglePreference mLcdColorEnhancement;

    private AwesomeListPreference mPanelColor;

    //==============================================================================================
    // Extras
    //==============================================================================================
    private AwesomeTogglePreference mLoggerMode;
    private CustomPreference mFastCharge;
    private CustomPreference mSoundControl;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_feature_general);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        PreferenceCategory category = (PreferenceCategory) findPreference("input_gestures");
        mKnockOn = (AwesomeTogglePreference) findPreference("knockon_gesture_enable");
        if (mKnockOn.isSupported()) {
            try {
                mKnockOn.initValue();
            } catch (Exception ignored) { }
            mKnockOn.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mKnockOn);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        category = (PreferenceCategory) findPreference("input_others");
        final VibratorIntensity pref = (VibratorIntensity) findPreference("vibrator_tuning");
        if (!VibratorIntensity.isSupported()) {
            category.removePreference(pref);
        }

        mAwesomeGloveMode = (AwesomeTogglePreference) findPreference("input_glove_mode_aw");
        if (mAwesomeGloveMode.isSupported()) {
            mAwesomeGloveMode.initValue();
            mAwesomeGloveMode.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mAwesomeGloveMode);
            mAwesomeGloveMode = null;
        }

        mGloveMode = (CustomTogglePreference) findPreference("input_glove_mode");
        try {
            // if we have already added a glove mode preference, remove it too
            if (mAwesomeGloveMode != null || !isHtsSupported()) {
                category.removePreference(mGloveMode);
            } else {
                final String value = DatabaseHandler.getInstance()
                        .getValueByName(mGloveMode.getKey(), DatabaseHandler.TABLE_BOOTUP);
                final boolean enableGlove = (value != null && value.equals("1"));

                enableHts(enableGlove);
                mGloveMode.setOnPreferenceChangeListener(this);
            }
        } catch (Exception exc) { category.removePreference(mGloveMode); }

        mResetOnSuspend = (AwesomeTogglePreference) findPreference("input_reset_on_suspend");
        if (mResetOnSuspend.isSupported()) {
            mResetOnSuspend.initValue();
            mResetOnSuspend.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mResetOnSuspend);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        // LIGHTS

        category = (PreferenceCategory) findPreference("touchkey");
        mBacklightKey = (AwesomeTogglePreference) findPreference("touchkey_light");
        if (mBacklightKey.isSupported()) {
            mBacklightKey.initValue();
            mBacklightKey.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mBacklightKey);
        }

        mBacklightNotification = (AwesomeTogglePreference) findPreference("touchkey_bln");
        if (mBacklightNotification.isSupported()) {
            mBacklightNotification.initValue();
            mBacklightNotification.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mBacklightNotification);
        }

        mKeyboardBacklight = (AwesomeTogglePreference) findPreference("keyboard_light");
        if (mKeyboardBacklight.isSupported()) {
            mKeyboardBacklight.initValue();
            mKeyboardBacklight.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mKeyboardBacklight);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        // Display

        category = (PreferenceCategory) findPreference("graphics");
        final DisplayColor displayColor =
                (DisplayColor) findPreference(DisplayColorCalibration.TAG);
        if (!DisplayColor.isSupported()) {
            category.removePreference(displayColor);
        }

        final DisplayGamma displayGamma =
                (DisplayGamma) findPreference(DisplayGammaCalibration.TAG);
        if (!DisplayGamma.isSupported()) {
            category.removePreference(displayGamma);
        }

        mPanelColor = (AwesomeListPreference) findPreference("panel_color_temperature");
        if (mPanelColor.isSupported()) {
            mPanelColor.initValue();
            mPanelColor.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mPanelColor);
        }

        mLcdPowerReduce = (AwesomeTogglePreference) findPreference("lcd_power_reduce");
        if (mLcdPowerReduce.isSupported()) {
            mLcdPowerReduce.initValue();
            mLcdPowerReduce.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mLcdPowerReduce);
        }

        mLcdSunlightEnhancement =
                (AwesomeTogglePreference) findPreference("lcd_sunlight_enhancement");
        if (mLcdSunlightEnhancement.isSupported()) {
            mLcdSunlightEnhancement.initValue();
            mLcdSunlightEnhancement.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mLcdSunlightEnhancement);
        }

        mLcdColorEnhancement = (AwesomeTogglePreference) findPreference("lcd_color_enhancement");
        if (mLcdColorEnhancement.isSupported()) {
            mLcdColorEnhancement.initValue();
            mLcdColorEnhancement.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mLcdColorEnhancement);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        category = (PreferenceCategory) findPreference("extras");
        mLoggerMode = (AwesomeTogglePreference) findPreference("logger_mode");
        if (mLoggerMode.isSupported()) {
            mLoggerMode.initValue(true);
            mLoggerMode.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mLoggerMode);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        mFastCharge = (CustomPreference) findPreference("fast_charge");
        if (Utils.fileExists(FC_PATH)) {
            mFastCharge.setOnPreferenceClickListener(this);
        } else {
            preferenceScreen.removePreference(mFastCharge);
        }

        mSoundControl = (CustomPreference) findPreference("sound_control");
        if (Utils.fileExists(SOUND_CONTROL_PATHS)) {
            mSoundControl.setOnPreferenceClickListener(this);
        } else {
            preferenceScreen.removePreference(mSoundControl);
        }

        isSupported(preferenceScreen, getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mGloveMode && mGloveMode.isEnabled()) {
            final boolean value = (Boolean) o;
            enableHts(value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mGloveMode.getKey(),
                            mGloveMode.getKey(), (value ? "1" : "0"))
            );
            return true;
        } else if (preference == mAwesomeGloveMode) {
            mAwesomeGloveMode.writeValue((Boolean) o);
            return true;
        } else if (preference == mResetOnSuspend) {
            mResetOnSuspend.writeValue((Boolean) o);
            return true;
        } else if (preference == mKnockOn) {
            mKnockOn.writeValue((Boolean) o);
            return true;
        } else if (preference == mBacklightKey) {
            mBacklightKey.writeValue((Boolean) o);
            return true;
        } else if (preference == mBacklightNotification) {
            mBacklightNotification.writeValue((Boolean) o);
            return true;
        } else if (preference == mKeyboardBacklight) {
            mKeyboardBacklight.writeValue((Boolean) o);
            return true;
        } else if (preference == mPanelColor) {
            mPanelColor.writeValue(String.valueOf(o));
            return true;
        } else if (preference == mLcdPowerReduce) {
            mLcdPowerReduce.writeValue((Boolean) o);
            return true;
        } else if (preference == mLcdSunlightEnhancement) {
            mLcdSunlightEnhancement.writeValue((Boolean) o);
            return true;
        } else if (preference == mLcdColorEnhancement) {
            mLcdColorEnhancement.writeValue((Boolean) o);
            return true;
        } else if (mLoggerMode == preference) {
            mLoggerMode.writeValue((Boolean) o);
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mFastCharge == preference) {
            MainActivity.loadFragment(getActivity(), DeviceConstants.ID_FAST_CHARGE);
            return true;
        } else if (mSoundControl == preference) {
            MainActivity.loadFragment(getActivity(), DeviceConstants.ID_SOUND_CONTROL);
            return true;
        }

        return false;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = DatabaseHandler.getInstance()
                .getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_DEVICE);

        String filename, value;
        for (final DataItem item : items) {
            filename = item.getFileName();
            value = item.getValue();
            if ("input_glove_mode".equals(filename)) {
                final String mode = ("1".equals(value) ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
                sbCmd.append(Utils.getWriteCommand(COMMAND_PATH, mode));
            } else {
                sbCmd.append(Utils.getWriteCommand(filename, value));
            }
        }

        return sbCmd.toString();
    }

    private static final String COMMAND_PATH = "/sys/class/sec/tsp/cmd";
    private static final String GLOVE_MODE = "glove_mode";
    private static final String GLOVE_MODE_ENABLE = GLOVE_MODE + ",1";
    private static final String GLOVE_MODE_DISABLE = GLOVE_MODE + ",0";

    public void enableHts(final boolean enable) {
        if (mGloveMode != null) mGloveMode.setEnabled(false);
        final String mode = (enable ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
        Utils.getCommandResult(this, Utils.getWriteCommand(COMMAND_PATH, mode) +
                Utils.getReadCommand("/sys/class/sec/tsp/cmd_result"));
    }

    @Override public void onShellOutput(final ShellOutput output) {
        if (output == null || mGloveMode == null) return;

        mGloveMode.setChecked(output.output.contains(GLOVE_MODE_ENABLE));
        mGloveMode.setEnabled(true);
    }

    /**
     * Whether device supports high touch sensitivity.
     *
     * @return boolean Supported devices must return always true
     */
    private boolean isHtsSupported() {
        final File f = new File(COMMAND_PATH);

        // Check to make sure that the kernel supports glove mode
        if (f.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("/sys/class/sec/tsp/cmd_list"));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.equals(GLOVE_MODE)) {
                        Logger.v(DeviceInformationGeneralFragment.class,
                                "Glove mode / high touch sensitivity supported");
                        return true;
                    }
                }
            } catch (IOException ignored) {
                // ignored
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }

        Logger.v(DeviceInformationGeneralFragment.class,
                "Glove mode / high touch sensitivity NOT supported");

        return false;
    }

}
