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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class FeaturesFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final int ID = 101;

    private static final int ID_LOGGER_MODE = 1001;

    private CustomCheckBoxPreference mLoggerMode;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID); }

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
        addPreferencesFromResource(R.xml.features);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        mLoggerMode = (CustomCheckBoxPreference) findPreference("logger_mode");
        if (mLoggerMode != null) {
            if (Utils.fileExists(LOGGER_MODE_PATH)) {
                Utils.getCommandResult(ID_LOGGER_MODE, Utils.getReadCommand(LOGGER_MODE_PATH));
            } else {
                preferenceScreen.removePreference(mLoggerMode);
            }
        }
        isSupported(preferenceScreen, getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mLoggerMode) {
            final boolean value = (Boolean) o;
            Utils.writeValue(LOGGER_MODE_PATH, value ? "1" : "0");
            PreferenceHelper.setBoolean("logger_mode", value);
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();
        String value;

        if (Utils.fileExists(LOGGER_MODE_PATH)) {
            value = PreferenceHelper.getBoolean("logger_mode", true) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(LOGGER_MODE_PATH, value));
        }

        return sbCmd.toString();
    }

    @Subscribe
    public void onShellOutput(final ShellOutputEvent event) {
        if (event == null) { return; }

        final String result = event.getOutput();
        final int id = event.getId();

        switch (id) {
            case ID_LOGGER_MODE:
                if (mLoggerMode != null) {
                    mLoggerMode.setChecked(result.contains("enabled"));
                    mLoggerMode.setOnPreferenceChangeListener(this);
                }
                break;
            default:
                break;
        }
    }

}
