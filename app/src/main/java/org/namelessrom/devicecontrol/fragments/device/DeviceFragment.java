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
import android.view.View;
import android.view.ViewConfiguration;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.DeviceFragmentEvent;
import org.namelessrom.devicecontrol.objects.HighTouchSensitivity;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import java.util.List;

public class DeviceFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Input
    //==============================================================================================
    public static final String  sKnockOnFile = Utils.checkPaths(FILES_KNOCKON);
    public static final boolean sKnockOn     = !sKnockOnFile.isEmpty();

    private CustomCheckBoxPreference mForceNavBar;
    private CustomCheckBoxPreference mGloveMode;
    private CustomCheckBoxPreference mKnockOn;

    private boolean hasNavBar = false;

    //==============================================================================================
    // Lights
    //==============================================================================================
    public static final boolean sHasTouchkeyToggle = Utils.fileExists(FILE_TOUCHKEY_TOGGLE);
    public static final boolean sHasTouchkeyBLN    = Utils.fileExists(FILE_BLN_TOGGLE);
    public static final boolean sHasKeyboardToggle = Utils.fileExists(FILE_KEYBOARD_TOGGLE);

    private CustomCheckBoxPreference mBacklightKey;
    private CustomCheckBoxPreference mBacklightNotification;
    private CustomCheckBoxPreference mKeyboardBacklight;

    //==============================================================================================
    // Display
    //==============================================================================================
    private CustomCheckBoxPreference mLcdPowerReduce;
    public static final String  sLcdPowerReduceFile = Utils.checkPaths(FILES_LCD_POWER_REDUCE);
    public static final boolean sLcdPowerReduce     = !sLcdPowerReduceFile.isEmpty();
    //----------------------------------------------------------------------------------------------
    public static final String  sHasPanelFile       = Utils.checkPaths(FILES_PANEL_COLOR_TEMP);
    public static final boolean sHasPanel           = !sHasPanelFile.isEmpty();
    private CustomListPreference mPanelColor;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_DEVICE); }

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
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
            hasNavBar = !ViewConfiguration.get(mActivity).hasPermanentMenuKey();
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
                if (!sKnockOn) {
                    category.removePreference(mKnockOn);
                } else {
                    try {
                        final String value = Utils.readOneLine(sKnockOnFile);
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
                    if (!HighTouchSensitivity.isSupported()) {
                        category.removePreference(mGloveMode);
                    } else {
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
                if (!sHasTouchkeyToggle) {
                    category.removePreference(mBacklightKey);
                } else {
                    mBacklightKey.setChecked(!Utils.readOneLine(FILE_TOUCHKEY_TOGGLE).equals("0"));
                    mBacklightKey.setOnPreferenceChangeListener(this);
                }
            }

            mBacklightNotification = (CustomCheckBoxPreference) findPreference(KEY_TOUCHKEY_BLN);
            if (mBacklightNotification != null) {
                if (!sHasTouchkeyBLN) {
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
                if (!sHasKeyboardToggle) {
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
        } else if (preference == mGloveMode) {
            final boolean value = (Boolean) o;
            HighTouchSensitivity.setEnabled(value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mGloveMode.getKey(),
                            KEY_GLOVE_MODE, (value ? "1" : "0"))
            );
            changed = true;
        } else if (preference == mKnockOn) {
            final boolean newValue = (Boolean) o;
            final String value = (newValue) ? "1" : "0";
            Utils.writeValue(sKnockOnFile, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_DEVICE, mKnockOn.getKey(),
                            sKnockOnFile, value)
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

    public static String restore(final DatabaseHandler db) {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items =
                db.getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_DEVICE);
        for (final DataItem item : items) {
            sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
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
