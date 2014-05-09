/*
 * Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.fragments.device;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.ViewConfiguration;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.DeviceFragmentEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.widgets.preferences.VibratorTuningPreference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class DeviceFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Input
    //==============================================================================================
    private CustomCheckBoxPreference mForceNavBar;
    private CustomCheckBoxPreference mGloveMode;
    private CustomCheckBoxPreference mKnockOn;

    private boolean hasNavBar = false;

    //==============================================================================================
    // Lights
    //==============================================================================================
    private CustomCheckBoxPreference mBacklightKey;
    private CustomCheckBoxPreference mBacklightNotification;
    private CustomCheckBoxPreference mKeyboardBacklight;

    //==============================================================================================
    // Display
    //==============================================================================================
    public static final String  sLcdPowerReduceFile = Utils.checkPaths(FILES_LCD_POWER_REDUCE);
    public static final boolean sLcdPowerReduce     = !sLcdPowerReduceFile.isEmpty();
    private CustomCheckBoxPreference mLcdPowerReduce;
    //----------------------------------------------------------------------------------------------
    public static final String  sHasPanelFile = Utils.checkPaths(FILES_PANEL_COLOR_TEMP);
    public static final boolean sHasPanel     = !sHasPanelFile.isEmpty();
    private CustomListPreference mPanelColor;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(final Activity activity) { super.onAttach(activity, ID_DEVICE); }

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

        try {
            hasNavBar = getResources().getBoolean(
                    (Integer) Class.forName("com.android.internal.R$bool")
                            .getDeclaredField("config_showNavigationBar").get(null)
            );
        } catch (Exception exc) { // fallback
            hasNavBar = getActivity() != null
                    && !ViewConfiguration.get(getActivity()).hasPermanentMenuKey();
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("input_navbar");
        if (category != null) {
            mForceNavBar = (CustomCheckBoxPreference) findPreference(KEY_NAVBAR_FORCE);
            if (mForceNavBar != null) {
                if (hasNavBar || Application.IS_NAMELESS) {
                    category.removePreference(mForceNavBar);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference("input_knockon");
        if (category != null) {
            mKnockOn = (CustomCheckBoxPreference) findPreference(KEY_KNOCK_ON);
            if (mKnockOn != null) {
                if (!Utils.fileExists(FILE_KNOCKON)) {
                    category.removePreference(mKnockOn);
                } else {
                    try {
                        final String value = Utils.readOneLine(FILE_KNOCKON);
                        if (value != null && !value.isEmpty()) {
                            mKnockOn.setChecked("1".equals(value));
                        }
                    } catch (Exception ignored) { /* ignore it */ }
                    mKnockOn.setOnPreferenceChangeListener(this);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference("input_others");
        if (category != null) {
            final VibratorTuningPreference pref =
                    (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);
            if (pref != null && !VibratorTuningPreference.isSupported()) {
                category.removePreference(pref);
            }

            mGloveMode = (CustomCheckBoxPreference) findPreference(KEY_GLOVE_MODE);
            if (mGloveMode != null) {
                try {
                    if (!isHtsSupported()) {
                        category.removePreference(mGloveMode);
                    } else {
                        final String value = DatabaseHandler.getInstance(getActivity())
                                .getValueByName(KEY_GLOVE_MODE, DatabaseHandler.TABLE_BOOTUP);
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

        category = (PreferenceCategory) findPreference(CATEGORY_TOUCHKEY);
        if (category != null) {
            mBacklightKey = (CustomCheckBoxPreference) findPreference(KEY_TOUCHKEY_LIGHT);
            if (mBacklightKey != null) {
                if (!Utils.fileExists(FILE_TOUCHKEY_TOGGLE)) {
                    category.removePreference(mBacklightKey);
                } else {
                    mBacklightKey.setChecked(!Utils.readOneLine(FILE_TOUCHKEY_TOGGLE).equals("0"));
                    mBacklightKey.setOnPreferenceChangeListener(this);
                }
            }

            mBacklightNotification = (CustomCheckBoxPreference) findPreference(KEY_TOUCHKEY_BLN);
            if (mBacklightNotification != null) {
                if (!Utils.fileExists(FILE_BLN_TOGGLE)) {
                    category.removePreference(mBacklightNotification);
                } else {
                    mBacklightNotification.setChecked(
                            Utils.readOneLine(FILE_BLN_TOGGLE).equals("1")
                    );
                    mBacklightNotification.setOnPreferenceChangeListener(this);
                }
            }

            mKeyboardBacklight = (CustomCheckBoxPreference) findPreference(KEY_KEYBOARD_LIGHT);
            if (mKeyboardBacklight != null) {
                if (!Utils.fileExists(FILE_KEYBOARD_TOGGLE)) {
                    category.removePreference(mKeyboardBacklight);
                } else {
                    mKeyboardBacklight.setChecked(
                            Utils.readOneLine(FILE_KEYBOARD_TOGGLE).equals("1")
                    );
                    mKeyboardBacklight.setOnPreferenceChangeListener(this);
                }
            }

            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        // Display

        category = (PreferenceCategory) findPreference(CATEGORY_GRAPHICS);
        if (category != null) {
            mPanelColor = (CustomListPreference) findPreference(KEY_PANEL_COLOR_TEMP);
            if (mPanelColor != null) {
                if (!sHasPanel) {
                    category.removePreference(mPanelColor);
                } else {
                    mPanelColor.setValue(Utils.readOneLine(sHasPanelFile));
                    mPanelColor.setOnPreferenceChangeListener(this);
                }
            }

            mLcdPowerReduce = (CustomCheckBoxPreference) findPreference(KEY_LCD_POWER_REDUCE);
            if (mLcdPowerReduce != null) {
                if (sLcdPowerReduce) {
                    mLcdPowerReduce.setChecked(Utils.readOneLine(sLcdPowerReduceFile).equals("1"));
                    mLcdPowerReduce.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mLcdPowerReduce);
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
            Utils.runRootCommand(Scripts.toggleForceNavBar());
            changed = true;
        } else if (preference == mGloveMode && mGloveMode.isEnabled()) {
            final boolean value = (Boolean) o;
            enableHts(value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mGloveMode.getKey(),
                            KEY_GLOVE_MODE, (value ? "1" : "0"))
            );
            changed = true;
        } else if (preference == mKnockOn) {
            final boolean newValue = (Boolean) o;
            final String value = (newValue) ? "1" : "0";
            Utils.writeValue(FILE_KNOCKON, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mKnockOn.getKey(),
                            FILE_KNOCKON, value)
            );
            changed = true;
        } else if (preference == mBacklightKey) { // ======================================== LIGHTS
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "255" : "0";
            Utils.writeValue(FILE_TOUCHKEY_TOGGLE, value);
            Utils.writeValue(FILE_TOUCHKEY_BRIGHTNESS, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mBacklightKey.getKey() + '0',
                            FILE_TOUCHKEY_TOGGLE, value)
            );
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mBacklightKey.getKey() + '1',
                            FILE_TOUCHKEY_BRIGHTNESS, value)
            );
            changed = true;
        } else if (preference == mBacklightNotification) {
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "1" : "0";
            Utils.writeValue(FILE_BLN_TOGGLE, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mBacklightNotification.getKey(),
                            FILE_BLN_TOGGLE, value)
            );
            changed = true;
        } else if (preference == mKeyboardBacklight) {
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "255" : "0";
            Utils.writeValue(FILE_KEYBOARD_TOGGLE, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mKeyboardBacklight.getKey(),
                            FILE_KEYBOARD_TOGGLE, value)
            );
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
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "1" : "0";
            Utils.writeValue(sLcdPowerReduceFile, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mLcdPowerReduce.getKey(),
                            sLcdPowerReduceFile, value)
            );
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
            logDebug("Glove mode / high touch sensitivity is supported");
        } else {
            logDebug("Glove mode / high touch sensitivity is NOT supported");
        }

        return supported;
    }

    public static String restore(final DatabaseHandler db) {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items =
                db.getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_DEVICE);
        String filename, value;
        for (final DataItem item : items) {
            filename = item.getFileName();
            value = item.getValue();
            if (KEY_GLOVE_MODE.equals(filename)) {
                final String mode = (value.equals("1") ? GLOVE_MODE_ENABLE : GLOVE_MODE_DISABLE);
                Utils.getCommandResult(SHELL_HTS,
                        Utils.getWriteCommand(COMMAND_PATH, mode) +
                                Utils.getReadCommand("/sys/class/sec/tsp/cmd_result")
                );
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
