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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.ViewConfiguration;
import android.widget.Toast;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
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
    private CheckBoxPreference mForceNavBar;
    private CheckBoxPreference mGloveMode;
    private CheckBoxPreference mKnockOn;

    private boolean hasMenuKey;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_input_preferences);

        hasMenuKey = ViewConfiguration.get(getActivity()).hasPermanentMenuKey();

        VibratorTuningPreference mVibratorTuning =
                (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);

        mForceNavBar = (CheckBoxPreference) findPreference(KEY_NAVBAR_FORCE);
        mForceNavBar.setEnabled(false);
        mGloveMode = (CheckBoxPreference) findPreference(KEY_GLOVE_MODE);
        mGloveMode.setOnPreferenceChangeListener(this);

        PreferenceGroup group = (PreferenceGroup) findPreference("input_knockon");

        if (!Utils.fileExists(FILE_KNOCKON)) {
            getPreferenceScreen().removePreference(group);
        } else {
            mKnockOn = (CheckBoxPreference) findPreference(KEY_KNOCK_ON);
            try { // In case the file does not have read permissions
                mKnockOn.setChecked(Utils.readOneLine(FILE_KNOCKON).equals("1"));
            } catch (Exception ignored) {
                // Don't worry, be happy
            }
            mKnockOn.setOnPreferenceChangeListener(this);
        }

        group = (PreferenceGroup) findPreference("input_others");

        if (!VibratorTuningPreference.isSupported()) {
            group.removePreference(mVibratorTuning);
        }
        if (!HighTouchSensitivity.isSupported()) {
            group.removePreference(mGloveMode);
        }

        if (group.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(group);
        }

        new DeviceInputTask().execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceNavBar) {
            final boolean value = (Boolean) o;
            Shell.SU.run(Scripts.toggleForceNavBar());
            PreferenceHelper.setBoolean(KEY_NAVBAR_FORCE, value);
            if (!value && !hasMenuKey) {
                Toast.makeText(getActivity(), getString(R.string.navbar_warning),
                        Toast.LENGTH_LONG).show();
            }
            changed = true;
        } else if (preference == mGloveMode) {
            HighTouchSensitivity.setEnabled(!mGloveMode.isChecked());
            PreferenceHelper.setBoolean(KEY_GLOVE_MODE, !mGloveMode.isChecked());
            changed = true;
        } else if (preference == mKnockOn) {
            final boolean newValue = (Boolean) o;
            final String value = (newValue) ? "1" : "0";
            Utils.writeValue(FILE_KNOCKON, value);
            PreferenceHelper.setBoolean(KEY_KNOCK_ON, newValue);
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

        boolean tmp;

        if (Application.HAS_ROOT) {
            tmp = paramResult.get(i);
            if (tmp || hasMenuKey) {
                mForceNavBar.setChecked(tmp);
                mForceNavBar.setEnabled(true);
                mForceNavBar.setOnPreferenceChangeListener(this);
            } else {
                preferenceGroup.removePreference(mForceNavBar);
            }
        } else {
            preferenceGroup.removePreference(mForceNavBar);
        }

        if (preferenceGroup.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(preferenceGroup);
        }
    }

    public static void restore() { // Check for every possible error, caller is unprotected
        if (Utils.fileExists(FILE_KNOCKON)) {
            Utils.writeValue(FILE_KNOCKON,
                    (PreferenceHelper.getBoolean(KEY_KNOCK_ON, false) ? "1" : "0"));
        }
        if (VibratorTuningPreference.isSupported()) {
            VibratorTuningPreference.restore();
        }
    }

    public static boolean isSupported(Activity activity) {
        return (ViewConfiguration.get(activity).hasPermanentMenuKey()
                || VibratorTuningPreference.isSupported()
                || HighTouchSensitivity.isSupported());
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
