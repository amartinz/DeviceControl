/*
 *  Copyright (C) 2012 The CyanogenMod Project
 *  Modifications Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
import android.preference.Preference;
import android.preference.PreferenceFragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.PanelColorTemperature;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

public class DeviceGraphicsFragment extends PreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Fields
    //==============================================================================================

    private static final boolean sHasPanel = Utils.fileExists(FILE_PANEL_COLOR_TEMP);
    private PanelColorTemperature mPanelColor;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.device_graphics_preferences);

        if (!sHasPanel) {
            getPreferenceScreen().removePreference(findPreference(CATEGORY_GRAPHICS));
        } else {
            mPanelColor = (PanelColorTemperature) findPreference(KEY_PANEL_COLOR_TEMP);
            mPanelColor.setValue(Utils.readOneLine(FILE_PANEL_COLOR_TEMP));
            mPanelColor.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        if (preference == mPanelColor) {
            Utils.writeValue(FILE_PANEL_COLOR_TEMP, newValue.toString());
            PreferenceHelper.setString(KEY_PANEL_COLOR_TEMP, newValue.toString());
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static boolean isSupported() {
        return (sHasPanel);
    }
}
