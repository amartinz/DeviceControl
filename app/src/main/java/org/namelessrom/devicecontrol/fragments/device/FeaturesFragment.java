/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
import org.namelessrom.devicecontrol.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

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
    public void onAttach(final Activity activity) { super.onAttach(activity, ID_FEATURES); }

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

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = DatabaseHandler.getInstance()
                .getAllItems(DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_FEATURES);
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
