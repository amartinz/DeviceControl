/*
 *  Copyright (C) 2012 The CyanogenMod Project
 *  Modifications Copyright (C) 2013 Alexander "Evisceration" Martinz
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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.threads.WriteAndForget;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.util.ArrayList;
import java.util.List;

public class DeviceLightsFragment extends PreferenceFragment
        implements DeviceConstants, FileConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private CheckBoxPreference mBacklightKey;
    private CheckBoxPreference mBacklightNotification;
    public static final boolean sHasTouchkeyToggle = Utils.fileExists(FILE_TOUCHKEY_TOGGLE);
    public static final boolean sHasTouchkeyBLN = Utils.fileExists(FILE_BLN_TOGGLE);

    private CheckBoxPreference mKeyboardBacklight;
    public static final boolean sHasKeyboardToggle = Utils.fileExists(FILE_KEYBOARD_TOGGLE);

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.device_lights_preferences);

        PreferenceCategory prefs = (PreferenceCategory)
                findPreference(CATEGORY_TOUCHKEY);
        if (!sHasTouchkeyToggle) {
            prefs.removePreference(findPreference(KEY_TOUCHKEY_LIGHT));
        } else {
            mBacklightKey = (CheckBoxPreference) findPreference(KEY_TOUCHKEY_LIGHT);
            mBacklightKey.setChecked(!Utils.readOneLine(FILE_TOUCHKEY_TOGGLE).equals("0"));
        }
        if (!sHasTouchkeyBLN) {
            prefs.removePreference(findPreference(KEY_TOUCHKEY_BLN));
        } else {
            mBacklightNotification = (CheckBoxPreference) findPreference(KEY_TOUCHKEY_BLN);
            mBacklightNotification.setChecked(Utils.readOneLine(FILE_BLN_TOGGLE).equals("1"));
        }
        if (!sHasKeyboardToggle) {
            prefs.removePreference(findPreference(KEY_KEYBOARD_LIGHT));
        } else {
            mKeyboardBacklight = (CheckBoxPreference) findPreference(KEY_KEYBOARD_LIGHT);
            mKeyboardBacklight.setChecked(Utils.readOneLine(FILE_KEYBOARD_TOGGLE).equals("1"));
        }
        if (prefs.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(prefs);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mBacklightKey) {
            List<String> fileList = new ArrayList<String>();
            List<String> valueList = new ArrayList<String>();

            fileList.add(FILE_TOUCHKEY_TOGGLE);
            valueList.add(mBacklightKey.isChecked() ? "255" : "0");

            fileList.add(FILE_TOUCHKEY_BRIGHTNESS);
            valueList.add(mBacklightKey.isChecked() ? "255" : "0");

            new WriteAndForget(fileList, valueList).run();

            PreferenceHelper.setBoolean(KEY_TOUCHKEY_LIGHT, mBacklightKey.isChecked());
        } else if (preference == mBacklightNotification) {
            new WriteAndForget(FILE_BLN_TOGGLE
                    , mBacklightNotification.isChecked() ? "1" : "0").run();
            PreferenceHelper.setBoolean(KEY_TOUCHKEY_BLN, mBacklightNotification.isChecked());
        } else if (preference == mKeyboardBacklight) {
            new WriteAndForget(FILE_KEYBOARD_TOGGLE
                    , mKeyboardBacklight.isChecked() ? "255" : "0").run();
            PreferenceHelper.setBoolean(KEY_KEYBOARD_LIGHT, mKeyboardBacklight.isChecked());
        }
        return true;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static boolean isSupported() {
        return (sHasKeyboardToggle || sHasTouchkeyBLN || sHasTouchkeyToggle);
    }
}
