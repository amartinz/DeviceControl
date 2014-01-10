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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.VibratorTuningPreference;
import org.namelessrom.devicecontrol.utils.DeviceConstants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.classes.HighTouchSensitivity;

import java.util.ArrayList;
import java.util.List;

public class DeviceInputFragment extends PreferenceFragment implements DeviceConstants, Preference.OnPreferenceChangeListener {

    private static final boolean sVibratorTuning = Utils.fileExists(FILE_VIBRATOR);

    private CheckBoxPreference mForceNavBar;
    private CheckBoxPreference mGloveMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_input_preferences);

        VibratorTuningPreference mVibratorTuning =
                (VibratorTuningPreference) findPreference(KEY_VIBRATOR_TUNING);

        mForceNavBar = (CheckBoxPreference) findPreference(KEY_NAVBAR_FORCE);
        mGloveMode = (CheckBoxPreference) findPreference(KEY_GLOVE_MODE);
        mGloveMode.setOnPreferenceChangeListener(this);

        PreferenceGroup inputOthers = (PreferenceGroup) findPreference("input_others");

        if (!sVibratorTuning) {
            inputOthers.removePreference(mVibratorTuning);
        }
        if (!HighTouchSensitivity.isSupported()) {
            inputOthers.removePreference(mGloveMode);
        }

        if (inputOthers.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(inputOthers);
        }

        new DeviceInputTask(this).execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceNavBar) {
            Scripts.runScript(Scripts.toggleForceNavBar());
            changed = true;
        } else if (preference == mGloveMode) {
            HighTouchSensitivity.setEnabled(!mGloveMode.isChecked());
            PreferenceHelper.setBoolean(KEY_GLOVE_MODE, !mGloveMode.isChecked());
            changed = true;
        }

        return changed;
    }

    private void setResult(List<Boolean> paramResult) {
        mForceNavBar.setChecked(paramResult.get(0));
        mForceNavBar.setOnPreferenceChangeListener(this);
    }

    public static boolean isSupported() {
        return (true); // eg mForceNavBar is always showing, so no need to remove this screen
    }

    //

    class DeviceInputTask extends AsyncTask<Void, Integer, List<Boolean>>
            implements DeviceConstants {

        private final Context mContext;
        private final DeviceInputFragment mFragment;
        private ProgressDialog mDialog;

        public DeviceInputTask(DeviceInputFragment paramFragment) {
            mFragment = paramFragment;
            mContext = mFragment.getActivity();
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setTitle("");
            mDialog.setMessage(mContext.getString(R.string.dialog_getting_information, ""));
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected List<Boolean> doInBackground(Void... voids) {
            List<Boolean> tmpList = new ArrayList<Boolean>();

            publishProgress(0);
            tmpList.add(Scripts.getForceNavBar());

            return tmpList;
        }

        @Override
        protected void onProgressUpdate(Integer... integers) {
            switch (integers[0]) {
                default:
                case 0:
                    mDialog.setMessage(mContext.getString(
                            R.string.dialog_getting_information, "Navigationbar"));
                    break;
            }
        }

        @Override
        protected void onPostExecute(List<Boolean> booleans) {
            mDialog.dismiss();
            mFragment.setResult(booleans);
        }
    }
}
