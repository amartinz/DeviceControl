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
package org.namelessrom.devicecontrol.fragments.device;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.DeviceFragmentEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class DeviceFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Input
    //==============================================================================================
    private CustomCheckBoxPreference  mForceNavBar;
    private CustomCheckBoxPreference  mGloveMode;
    private AwesomeCheckBoxPreference mKnockOn;

    private boolean hasNavBar = false;

    //==============================================================================================
    // Lights
    //==============================================================================================
    private AwesomeCheckBoxPreference mBacklightKey;
    private AwesomeCheckBoxPreference mBacklightNotification;
    private AwesomeCheckBoxPreference mKeyboardBacklight;

    //==============================================================================================
    // Display
    //==============================================================================================
    private AwesomeCheckBoxPreference mLcdPowerReduce;
    private AwesomeCheckBoxPreference mLcdSunlightEnhancement;
    private AwesomeCheckBoxPreference mLcdColorEnhancement;
    //----------------------------------------------------------------------------------------------
    public static final String  sHasPanelFile = Utils.checkPaths(FILES_PANEL_COLOR_TEMP);
    public static final boolean sHasPanel     = !sHasPanelFile.isEmpty();
    private CustomListPreference mPanelColor;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return ID_DEVICE; }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        if (!Application.IS_NAMELESS) { // we have our own way to force the navigation bar
            try {
                hasNavBar = Resources.getSystem().getBoolean(Resources.getSystem()
                        .getIdentifier("config_showNavigationBar", "bool", "android"));
            } catch (Exception exc) { // fallback
                Logger.e(this, "Failed to get hasNavBar, showing it to do not lock someone out");
                hasNavBar = true;
            }
        }
        Logger.i(this, String.format("hasNavBar: %s", hasNavBar));

        PreferenceCategory category = (PreferenceCategory) findPreference("input_navbar");
        if (category != null) {
            mForceNavBar = (CustomCheckBoxPreference) findPreference("navbar_force");
            if (mForceNavBar != null) {
                if (hasNavBar || Application.IS_NAMELESS) {
                    category.removePreference(mForceNavBar);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference("input_gestures");
        if (category != null) {
            mKnockOn = (AwesomeCheckBoxPreference) findPreference("knockon_gesture_enable");
            if (mKnockOn != null) {
                if (mKnockOn.isSupported()) {
                    try {
                        mKnockOn.initValue();
                    } catch (Exception ignored) { }
                    mKnockOn.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mKnockOn);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference("input_others");
        if (category != null) {
            final VibratorTuningPreference pref =
                    (VibratorTuningPreference) findPreference("vibrator_tuning");
            if (pref != null && !VibratorTuningPreference.isSupported()) {
                category.removePreference(pref);
            }

            mGloveMode = (CustomCheckBoxPreference) findPreference("input_glove_mode");
            if (mGloveMode != null) {
                try {
                    if (!isHtsSupported()) {
                        category.removePreference(mGloveMode);
                    } else {
                        final String value = DatabaseHandler.getInstance()
                                .getValueByName(mGloveMode.getKey(), DatabaseHandler.TABLE_BOOTUP);
                        final boolean enableGlove = (value != null && value.equals("1"));

                        enableHts(enableGlove);
                        mGloveMode.setOnPreferenceChangeListener(this);
                    }
                } catch (Exception exc) { category.removePreference(mGloveMode); }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        // LIGHTS

        category = (PreferenceCategory) findPreference("touchkey");
        if (category != null) {
            mBacklightKey = (AwesomeCheckBoxPreference) findPreference("touchkey_light");
            if (mBacklightKey != null) {
                if (mBacklightKey.isSupported()) {
                    mBacklightKey.initValue();
                    mBacklightKey.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mBacklightKey);
                }
            }

            mBacklightNotification = (AwesomeCheckBoxPreference) findPreference("touchkey_bln");
            if (mBacklightNotification != null) {
                if (mBacklightNotification.isSupported()) {
                    mBacklightNotification.initValue();
                    mBacklightNotification.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mBacklightNotification);
                }
            }

            mKeyboardBacklight = (AwesomeCheckBoxPreference) findPreference("keyboard_light");
            if (mKeyboardBacklight != null) {
                if (mKeyboardBacklight.isSupported()) {
                    mKeyboardBacklight.initValue();
                    mKeyboardBacklight.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mKeyboardBacklight);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        // Display

        category = (PreferenceCategory) findPreference("graphics");
        if (category != null) {
            mPanelColor = (CustomListPreference) findPreference("panel_color_temperature");
            if (mPanelColor != null) {
                if (!sHasPanel) {
                    category.removePreference(mPanelColor);
                } else {
                    mPanelColor.setValue(Utils.readOneLine(sHasPanelFile));
                    mPanelColor.setOnPreferenceChangeListener(this);
                }
            }

            mLcdPowerReduce = (AwesomeCheckBoxPreference) findPreference("lcd_power_reduce");
            if (mLcdPowerReduce != null) {
                if (mLcdPowerReduce.isSupported()) {
                    mLcdPowerReduce.initValue();
                    mLcdPowerReduce.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mLcdPowerReduce);
                }
            }

            mLcdSunlightEnhancement =
                    (AwesomeCheckBoxPreference) findPreference("lcd_sunlight_enhancement");
            if (mLcdSunlightEnhancement != null) {
                if (mLcdSunlightEnhancement.isSupported()) {
                    mLcdSunlightEnhancement.initValue();
                    mLcdSunlightEnhancement.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mLcdSunlightEnhancement);
                }
            }

            mLcdColorEnhancement =
                    (AwesomeCheckBoxPreference) findPreference("lcd_color_enhancement");
            if (mLcdColorEnhancement != null) {
                if (mLcdColorEnhancement.isSupported()) {
                    mLcdColorEnhancement.initValue();
                    mLcdColorEnhancement.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mLcdColorEnhancement);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        isSupported(preferenceScreen, getActivity());

        new DeviceTask().execute();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceNavBar) { // ================================================= INPUT
            final String cmd = Scripts.toggleForceNavBar();
            Logger.v(this, String.format("mForceNavBar: %s", cmd));
            Utils.runRootCommand(cmd);
            changed = true;
        } else if (preference == mGloveMode && mGloveMode.isEnabled()) {
            final boolean value = (Boolean) o;
            enableHts(value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mGloveMode.getKey(),
                            mGloveMode.getKey(), (value ? "1" : "0"))
            );
            changed = true;
        } else if (preference == mKnockOn) {
            mKnockOn.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mBacklightKey) { // ======================================== LIGHTS
            mBacklightKey.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mBacklightNotification) {
            mBacklightNotification.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mKeyboardBacklight) {
            mKeyboardBacklight.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mPanelColor) { // ======================================== GRAPHICS
            final String value = String.valueOf(o);
            Utils.writeValue(sHasPanelFile, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mPanelColor.getKey(),
                            sHasPanelFile, value)
            );
            changed = true;
        } else if (preference == mLcdPowerReduce) {
            mLcdPowerReduce.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mLcdSunlightEnhancement) {
            mLcdSunlightEnhancement.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mLcdColorEnhancement) {
            mLcdColorEnhancement.writeValue((Boolean) o);
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private static final int SHELL_HTS = 1000;

    private static String COMMAND_PATH       = "/sys/class/sec/tsp/cmd";
    private static String GLOVE_MODE         = "glove_mode";
    private static String GLOVE_MODE_ENABLE  = GLOVE_MODE + ",1";
    private static String GLOVE_MODE_DISABLE = GLOVE_MODE + ",0";

    private void enableHts(final boolean enable) {
        if (mGloveMode != null) mGloveMode.setEnabled(false);
        final String mode = (enable ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
        Utils.getCommandResult(SHELL_HTS,
                Utils.getWriteCommand(COMMAND_PATH, mode) +
                        Utils.getReadCommand("/sys/class/sec/tsp/cmd_result")
        );
    }

    @Subscribe
    public void onShellOutputEvent(final ShellOutputEvent event) {
        if (event == null) return;

        final int id = event.getId();
        final String output = event.getOutput();
        switch (id) {
            case SHELL_HTS:
                if (output == null || mGloveMode == null) break;
                mGloveMode.setChecked(output.contains(GLOVE_MODE_ENABLE));
                mGloveMode.setEnabled(true);
                break;
            default:
                break;
        }
    }

    /**
     * Whether device supports high touch sensitivity.
     *
     * @return boolean Supported devices must return always true
     */
    private static boolean isHtsSupported() {
        boolean supported = false;
        File f = new File(COMMAND_PATH);

        // Check to make sure that the kernel supports glove mode
        if (f.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader("/sys/class/sec/tsp/cmd_list"));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.equals(GLOVE_MODE)) {
                        supported = true;
                        break;
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

        if (supported) {
            Logger.v(DeviceFragment.class, "Glove mode / high touch sensitivity is supported");
        } else {
            Logger.v(DeviceFragment.class, "Glove mode / high touch sensitivity is NOT supported");
        }

        return supported;
    }

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items =
                DatabaseHandler.getInstance()
                        .getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_DEVICE);
        String filename, value;
        for (final DataItem item : items) {
            filename = item.getFileName();
            value = item.getValue();
            if ("input_glove_mode".equals(filename)) {
                final String mode = (value.equals("1") ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
                sbCmd.append(Utils.getWriteCommand(COMMAND_PATH, mode));
            } else {
                sbCmd.append(Utils.getWriteCommand(filename, value));
            }
        }

        return sbCmd.toString();
    }

    @Subscribe
    public void onDeviceFragment(final DeviceFragmentEvent event) {
        if (event == null) { return; }
        final boolean isForceNavBar = event.isForceNavBar();

        if (mForceNavBar != null) {
            mForceNavBar.setChecked(isForceNavBar);
            mForceNavBar.setEnabled(true);
            mForceNavBar.setOnPreferenceChangeListener(this);
        }
    }

//==============================================================================================
// Internal Classes
//==============================================================================================

    class DeviceTask extends AsyncTask<Void, Void, DeviceFragmentEvent> {

        @Override
        protected DeviceFragmentEvent doInBackground(Void... voids) {
            boolean isForceNavBar = false;

            if (!hasNavBar && !Application.IS_NAMELESS) {
                isForceNavBar = Scripts.getForceNavBar();
            }

            return new DeviceFragmentEvent(isForceNavBar);
        }

        @Override
        protected void onPostExecute(final DeviceFragmentEvent event) {
            if (event != null) {
                BusProvider.getBus().post(event);
            }
        }
    }
}
