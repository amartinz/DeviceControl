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

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

import java.util.List;

public class FeaturesFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private static final String FC_PATH = "/sys/kernel/fast_charge";

    private AwesomeCheckBoxPreference mLoggerMode;
    private CustomPreference          mFastCharge;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_FEATURES); }

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
        addPreferencesFromResource(R.xml.features);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();

        mLoggerMode = (AwesomeCheckBoxPreference) findPreference("logger_mode");
        if (mLoggerMode != null) {
            if (mLoggerMode.isSupported()) {
                mLoggerMode.initValue(true);
                mLoggerMode.setOnPreferenceChangeListener(this);
            } else {
                preferenceScreen.removePreference(mLoggerMode);
            }
        }

        mFastCharge = (CustomPreference) findPreference("fast_charge");
        if (mFastCharge != null) {
            if (Utils.fileExists(FC_PATH)) {
                mFastCharge.setOnPreferenceClickListener(this);
            } else {
                preferenceScreen.removePreference(mFastCharge);
            }
        }

        isSupported(preferenceScreen, getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (mLoggerMode == preference) {
            mLoggerMode.writeValue((Boolean) o);
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
                db.getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_FEATURES);
        for (final DataItem item : items) {
            sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
        }

        return sbCmd.toString();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mFastCharge == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_FAST_CHARGE));
            return true;
        }

        return false;
    }
}
