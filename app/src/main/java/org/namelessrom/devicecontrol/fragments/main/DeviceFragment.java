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

package org.namelessrom.devicecontrol.fragments.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewConfiguration;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.fragments.parents.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;
import org.namelessrom.devicecontrol.utils.cmdprocessor.CMDProcessor;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

public class DeviceFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final int ID = 100;

    //==============================================================================================
    // Input
    //==============================================================================================
    public static final String  sKnockOnFile = Utils.checkPaths(FILES_KNOCKON);
    public static final boolean sKnockOn     = !sKnockOnFile.equals("");

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
    // Graphics
    //==============================================================================================
    public static final String  sHasPanelFile = Utils.checkPaths(FILES_PANEL_COLOR_TEMP);
    public static final boolean sHasPanel     = !sHasPanelFile.equals("");
    private CustomListPreference mPanelColor;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, DeviceFragment.ID);
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
        addPreferencesFromResource(R.xml.device_preferences);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        mForceNavBar = (CustomCheckBoxPreference) findPreference(KEY_NAVBAR_FORCE);
        try {
            hasNavBar = getResources().getBoolean(
                    (Integer) Class.forName("com.android.internal.R$bool")
                            .getDeclaredField("config_showNavigationBar").get(null)
            );
        } catch (Exception exc) { // fallback
            hasNavBar = !ViewConfiguration.get(mActivity).hasPermanentMenuKey();
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("input_knockon");
        if (category != null) {
            mKnockOn = (CustomCheckBoxPreference) findPreference(KEY_KNOCK_ON);
            if (!sKnockOn) {
                category.removePreference(mKnockOn);
            } else {
                try { // In case the file does not have read permissions
                    mKnockOn.setChecked(Utils.readOneLine(sKnockOnFile).equals("1"));
                    mKnockOn.setOnPreferenceChangeListener(this);
                } catch (Exception ignored) {
                    // Don't worry, be happy
                }
            }
            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        category = (PreferenceCategory) findPreference("input_others");
        if (category != null) {
            if (!VibratorTuningPreference.isSupported()) {
                category.removePreference(findPreference(KEY_VIBRATOR_TUNING));
            }
            mGloveMode = (CustomCheckBoxPreference) findPreference(KEY_GLOVE_MODE);
            if (!HighTouchSensitivity.isSupported()) {
                category.removePreference(mGloveMode);
            } else {
                mGloveMode.setOnPreferenceChangeListener(this);
            }
            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        // LIGHTS

        category = (PreferenceCategory) findPreference(CATEGORY_TOUCHKEY);
        if (category != null) {
            mBacklightKey = (CustomCheckBoxPreference) findPreference(KEY_TOUCHKEY_LIGHT);
            if (!sHasTouchkeyToggle) {
                category.removePreference(mBacklightKey);
            } else {
                mBacklightKey.setChecked(!Utils.readOneLine(FILE_TOUCHKEY_TOGGLE).equals("0"));
                mBacklightKey.setOnPreferenceChangeListener(this);
            }
            mBacklightNotification = (CustomCheckBoxPreference) findPreference(KEY_TOUCHKEY_BLN);
            if (!sHasTouchkeyBLN) {
                category.removePreference(mBacklightNotification);
            } else {
                mBacklightNotification.setChecked(Utils.readOneLine(FILE_BLN_TOGGLE).equals("1"));
                mBacklightNotification.setOnPreferenceChangeListener(this);
            }
            mKeyboardBacklight = (CustomCheckBoxPreference) findPreference(KEY_KEYBOARD_LIGHT);
            if (!sHasKeyboardToggle) {
                category.removePreference(mKeyboardBacklight);
            } else {
                mKeyboardBacklight.setChecked(Utils.readOneLine(FILE_KEYBOARD_TOGGLE).equals("1"));
                mKeyboardBacklight.setOnPreferenceChangeListener(this);
            }
            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        // GRAPHICS

        category = (PreferenceCategory) findPreference(CATEGORY_GRAPHICS);
        if (category != null) {
            mPanelColor = (CustomListPreference) findPreference(KEY_PANEL_COLOR_TEMP);
            if (!sHasPanel) {
                category.removePreference(mPanelColor);
            } else {
                mPanelColor.setValue(Utils.readOneLine(sHasPanelFile));
                mPanelColor.setOnPreferenceChangeListener(this);
            }
            if (category.getPreferenceCount() == 0) {
                preferenceScreen.removePreference(category);
            }
        }

        new DeviceTask().execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        // INPUT
        if (preference == mForceNavBar) {
            final boolean value = (Boolean) o;
            Utils.runRootCommand(Scripts.toggleForceNavBar());
            PreferenceHelper.setBoolean(KEY_NAVBAR_FORCE, value);
            changed = true;
        } else if (preference == mGloveMode) {
            final boolean value = (Boolean) o;
            HighTouchSensitivity.setEnabled(value);
            PreferenceHelper.setBoolean(KEY_GLOVE_MODE, value);
            changed = true;
        } else if (preference == mKnockOn) {
            final boolean newValue = (Boolean) o;
            final String value = (newValue) ? "1" : "0";
            CMDProcessor.runSuCommand(
                    Utils.getWriteCommand(sKnockOnFile, value)
            );
            PreferenceHelper.setBoolean(KEY_KNOCK_ON, newValue);
            changed = true;
        } else if (preference == mBacklightKey) { // ======================================== LIGHTS
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "255" : "0";

            CMDProcessor.runSuCommand(
                    Utils.getWriteCommand(FILE_TOUCHKEY_TOGGLE, value) +
                            Utils.getWriteCommand(FILE_TOUCHKEY_BRIGHTNESS, value)
            );

            PreferenceHelper.setBoolean(KEY_TOUCHKEY_LIGHT, newValue);
            changed = true;
        } else if (preference == mBacklightNotification) {
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "1" : "0";
            CMDProcessor.runSuCommand(
                    Utils.getWriteCommand(FILE_BLN_TOGGLE, value)
            );
            PreferenceHelper.setBoolean(KEY_TOUCHKEY_BLN, newValue);
            changed = true;
        } else if (preference == mKeyboardBacklight) {
            final boolean newValue = (Boolean) o;
            final String value = newValue ? "255" : "0";
            CMDProcessor.runSuCommand(
                    Utils.getWriteCommand(FILE_KEYBOARD_TOGGLE, value)
            );
            PreferenceHelper.setBoolean(KEY_KEYBOARD_LIGHT, newValue);
            changed = true;
        } else if (preference == mPanelColor) { // ======================================== GRAPHICS
            final String value = String.valueOf(o);
            CMDProcessor.runSuCommand(
                    Utils.getWriteCommand(sHasPanelFile, value)
            );
            PreferenceHelper.setString(KEY_PANEL_COLOR_TEMP, value);
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void setResult(List<Boolean> paramResult) {
        int i = 0;

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        final PreferenceCategory category =
                (PreferenceCategory) preferenceScreen.findPreference("input_navbar");

        boolean tmp;
        if (Application.HAS_ROOT && !Application.IS_NAMELESS) {
            tmp = paramResult.get(i);
            if (hasNavBar) {
                category.removePreference(mForceNavBar);
            } else {
                mForceNavBar.setChecked(tmp);
                mForceNavBar.setEnabled(true);
                mForceNavBar.setOnPreferenceChangeListener(this);
            }
            i++;
        } else {
            category.removePreference(mForceNavBar);
        }

        if (category.getPreferenceCount() == 0) {
            preferenceScreen.removePreference(category);
        }
    }

    //==============================================================================================
    // Internal Classes
    //==============================================================================================

    class DeviceTask extends AsyncTask<Void, Integer, List<Boolean>>
            implements DeviceConstants {

        @Override
        protected List<Boolean> doInBackground(Void... voids) {
            final List<Boolean> tmpList = new ArrayList<Boolean>();

            if (Application.HAS_ROOT && !Application.IS_NAMELESS) {
                tmpList.add(Scripts.getForceNavBar());
            }

            return tmpList;
        }

        @Override
        protected void onPostExecute(List<Boolean> booleans) {
            setResult(booleans);
        }
    }
}
