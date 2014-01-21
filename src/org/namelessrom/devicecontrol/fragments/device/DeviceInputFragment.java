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

import android.view
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

public class DeviceInputFragment extends PreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    
    // Check for hardware keys, don't need this if device requires navbar
    public boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey(); {
        if(!hasMenuKey) {
    private CheckBoxPreference mForceNavBar;
        }
    }
    private CheckBoxPreference mGloveMode;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_input_preferences);

        VibratorTuningPreference mVibratorTuning =
                (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);

        mForceNavBar = (CheckBoxPreference) findPreference(KEY_NAVBAR_FORCE);
        mForceNavBar.setEnabled(false);
        mGloveMode = (CheckBoxPreference) findPreference(KEY_GLOVE_MODE);
        mGloveMode.setOnPreferenceChangeListener(this);

        PreferenceGroup inputOthers = (PreferenceGroup) findPreference("input_others");

        if (!VibratorTuningPreference.isSupported()) {
            inputOthers.removePreference(mVibratorTuning);
        }
        if (!HighTouchSensitivity.isSupported()) {
            inputOthers.removePreference(mGloveMode);
        }

        if (inputOthers.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(inputOthers);
        }

        new DeviceInputTask().execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceNavBar) {
            Shell.SU.run(Scripts.toggleForceNavBar());
            changed = true;
        } else if (preference == mGloveMode) {
            HighTouchSensitivity.setEnabled(!mGloveMode.isChecked());
            PreferenceHelper.setBoolean(KEY_GLOVE_MODE, !mGloveMode.isChecked());
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void setResult(List<Boolean> paramResult) {
        int i = 0;

        PreferenceGroup preferenceGroup =
                (PreferenceGroup) getPreferenceScreen().findPreference("input_navbar");

        if (Application.HAS_ROOT) {
            mForceNavBar.setChecked(paramResult.get(i));
            mForceNavBar.setEnabled(true);
            mForceNavBar.setOnPreferenceChangeListener(this);
        } else {
            preferenceGroup.removePreference(mForceNavBar);
        }

        if (preferenceGroup.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(preferenceGroup);
        }
    }

    public static boolean isSupported() {
        return (true); // eg mForceNavBar is always showing, so no need to remove this screen
    }

    //==============================================================================================
    // Internal Classes
    //==============================================================================================

    class DeviceInputTask extends AsyncTask<Void, Integer, List<Boolean>>
            implements DeviceConstants {

        @Override
        protected List<Boolean> doInBackground(Void... voids) {
            List<Boolean> tmpList = new ArrayList<Boolean>();

            publishProgress(0);
            if (Application.HAS_ROOT) {
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
