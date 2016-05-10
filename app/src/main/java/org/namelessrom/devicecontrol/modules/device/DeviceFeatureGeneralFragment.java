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
package org.namelessrom.devicecontrol.modules.device;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.preferences.AwesomeListPreference;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.preferences.hardware.DisplayColor;
import org.namelessrom.devicecontrol.preferences.hardware.DisplayGamma;
import org.namelessrom.devicecontrol.preferences.hardware.VibratorIntensity;
import org.namelessrom.devicecontrol.views.CustomPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class DeviceFeatureGeneralFragment extends CustomPreferenceFragment implements Preference.OnPreferenceClickListener {

    private static final String FC_PATH = "/sys/kernel/fast_charge";

    private static final String[] SOUND_CONTROL_PATHS = new String[]{
            "/sys/devices/virtual/misc/soundcontrol"
    };

    //==============================================================================================
    // Input
    //==============================================================================================
    private CustomTogglePreference mGloveMode;

    private AwesomeListPreference mPanelColor;

    private CustomPreference mFastCharge;
    private CustomPreference mSoundControl;

    private final ShellOutput.OnShellOutputListener mShellOutputListener =
            new ShellOutput.OnShellOutputListener() {
                @Override public void onShellOutput(final ShellOutput output) {
                    if (output == null || mGloveMode == null) { return; }

                    mGloveMode.setChecked(output.output.contains(GLOVE_MODE_ENABLE));
                    mGloveMode.setEnabled(true);
                }
            };

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_feature_general);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        PreferenceCategory category = (PreferenceCategory) findPreference("input_gestures");
        AwesomeTogglePreference mKnockOn =
                (AwesomeTogglePreference) findPreference("knockon_gesture_enable");
        if (mKnockOn.isSupported()) {
            try {
                mKnockOn.initValue();
            } catch (Exception ignored) { }
            mKnockOn.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mKnockOn);
        }

        AwesomeTogglePreference sweepToWake =
                (AwesomeTogglePreference) findPreference("sweep_to_wake");
        if (sweepToWake.isSupported()) {
            sweepToWake.initValue();
            sweepToWake.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(sweepToWake);
        }

        AwesomeTogglePreference sweepToVolume =
                (AwesomeTogglePreference) findPreference("sweep_to_volume");
        if (sweepToVolume.isSupported()) {
            sweepToVolume.initValue();
            sweepToVolume.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(sweepToVolume);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        category = (PreferenceCategory) findPreference("input_others");
        final VibratorIntensity pref = (VibratorIntensity) findPreference("vibrator_tuning");
        if (!VibratorIntensity.isSupported()) {
            category.removePreference(pref);
        }

        AwesomeTogglePreference awesomeGloveMode =
                (AwesomeTogglePreference) findPreference("input_glove_mode_aw");
        if (awesomeGloveMode.isSupported()) {
            awesomeGloveMode.initValue();
            awesomeGloveMode.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(awesomeGloveMode);
            awesomeGloveMode = null;
        }

        mGloveMode = (CustomTogglePreference) findPreference("input_glove_mode");
        try {
            // if we have already added a glove mode preference, remove it too
            if (awesomeGloveMode != null || !isHtsSupported()) {
                category.removePreference(mGloveMode);
            } else {
                final BootupItem bootupItem = BootupConfig.get()
                        .getItemByName(mGloveMode.getKey());
                final String value = (bootupItem != null ? bootupItem.value : null);
                final boolean enableGlove = (value != null && value.equals("1"));

                enableHts(enableGlove);
                mGloveMode.setOnPreferenceChangeListener(this);
            }
        } catch (Exception exc) { category.removePreference(mGloveMode); }

        AwesomeTogglePreference resetOnSuspend =
                (AwesomeTogglePreference) findPreference("input_reset_on_suspend");
        if (resetOnSuspend.isSupported()) {
            resetOnSuspend.initValue();
            resetOnSuspend.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(resetOnSuspend);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        // LIGHTS

        category = (PreferenceCategory) findPreference("touchkey");
        AwesomeTogglePreference backlightKey =
                (AwesomeTogglePreference) findPreference("touchkey_light");
        if (backlightKey.isSupported()) {
            backlightKey.initValue();
            backlightKey.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(backlightKey);
        }

        AwesomeTogglePreference backlightNotification =
                (AwesomeTogglePreference) findPreference("touchkey_bln");
        if (backlightNotification.isSupported()) {
            backlightNotification.initValue();
            backlightNotification.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(backlightNotification);
        }

        AwesomeTogglePreference keyboardBacklight =
                (AwesomeTogglePreference) findPreference("keyboard_light");
        if (keyboardBacklight.isSupported()) {
            keyboardBacklight.initValue();
            keyboardBacklight.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(keyboardBacklight);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        // Display

        category = (PreferenceCategory) findPreference("graphics");
        final DisplayColor displayColor =
                (DisplayColor) findPreference("display_color_calibration");
        if (!DisplayColor.isSupported()) {
            category.removePreference(displayColor);
        }

        final DisplayGamma displayGamma =
                (DisplayGamma) findPreference("display_gamma_calibration");
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

        AwesomeTogglePreference lcdPowerReduce =
                (AwesomeTogglePreference) findPreference("lcd_power_reduce");
        if (lcdPowerReduce.isSupported()) {
            lcdPowerReduce.initValue();
            lcdPowerReduce.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(lcdPowerReduce);
        }

        AwesomeTogglePreference lcdSunlightEnhancement =
                (AwesomeTogglePreference) findPreference("lcd_sunlight_enhancement");
        if (lcdSunlightEnhancement.isSupported()) {
            lcdSunlightEnhancement.initValue();
            lcdSunlightEnhancement.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(lcdSunlightEnhancement);
        }

        AwesomeTogglePreference lcdColorEnhancement =
                (AwesomeTogglePreference) findPreference("lcd_color_enhancement");
        if (lcdColorEnhancement.isSupported()) {
            lcdColorEnhancement.initValue();
            lcdColorEnhancement.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(lcdColorEnhancement);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }

        category = (PreferenceCategory) findPreference("extras");
        AwesomeTogglePreference loggerMode =
                (AwesomeTogglePreference) findPreference("logger_mode");
        if (loggerMode.isSupported()) {
            loggerMode.initValue(true);
            loggerMode.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(loggerMode);
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
            BootupConfig.setBootup(
                    new BootupItem(BootupConfig.CATEGORY_DEVICE, mGloveMode.getKey(),
                            mGloveMode.getKey(), (value ? "1" : "0"), true));
            return true;
        } else if (preference instanceof AwesomeTogglePreference) {
            ((AwesomeTogglePreference) preference).writeValue((Boolean) o);
            return true;
        } else if (preference == mPanelColor) {
            mPanelColor.writeValue(String.valueOf(o));
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceClick(Preference preference) {
        final int id;
        if (mFastCharge == preference) {
            id = DeviceConstants.ID_FAST_CHARGE;
        } else if (mSoundControl == preference) {
            id = DeviceConstants.ID_SOUND_CONTROL;
        } else {
            id = Integer.MIN_VALUE;
        }

        if (id != Integer.MIN_VALUE) {
            final Activity activity = getActivity();
            if (activity instanceof ActivityCallbacks) {
                ((ActivityCallbacks) activity).shouldLoadFragment(id);
            }
            return true;
        }

        return false;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore(BootupConfig config) {
        final ArrayList<BootupItem> items = config.getItemsByCategory(BootupConfig.CATEGORY_DEVICE);
        if (items.size() == 0) {
            return "";
        }

        final StringBuilder sbCmd = new StringBuilder();
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            if ("input_glove_mode".equals(item.filename)) {
                String mode = ("1".equals(item.value) ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
                sbCmd.append(Utils.getWriteCommand(COMMAND_PATH, mode));
            } else {
                sbCmd.append(Utils.getWriteCommand(item.filename, item.value));
            }
        }

        return sbCmd.toString();
    }

    private static final String COMMAND_PATH = "/sys/class/sec/tsp/cmd";
    private static final String GLOVE_MODE = "glove_mode";
    private static final String GLOVE_MODE_ENABLE = GLOVE_MODE + ",1";
    private static final String GLOVE_MODE_DISABLE = GLOVE_MODE + ",0";

    private void enableHts(final boolean enable) {
        if (mGloveMode != null) { mGloveMode.setEnabled(false); }
        final String mode = (enable ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
        Utils.getCommandResult(mShellOutputListener, Utils.getWriteCommand(COMMAND_PATH, mode) +
                                                     Utils.getReadCommand("/sys/class/sec/tsp/cmd_result"));
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

        return false;
    }

}
