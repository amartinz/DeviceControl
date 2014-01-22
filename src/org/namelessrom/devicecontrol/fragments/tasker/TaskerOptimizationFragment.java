/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.tasker;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;


public class TaskerOptimizationFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    private SwitchPreference mFstrim;
    private ListPreference mFstrimInterval;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.tasker_optimizations);

        mFstrim = (SwitchPreference) findPreference("tasker_tools_fstrim");
        mFstrim.setChecked(PreferenceHelper.getBoolean(TASKER_TOOLS_FSTRIM));
        mFstrim.setOnPreferenceChangeListener(this);

        mFstrimInterval = (ListPreference) findPreference("tasker_tools_fstrim_interval");
        if (mFstrimInterval.getValue() == null)
            mFstrimInterval.setValueIndex(3);
        mFstrimInterval.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference == mFstrim) {
            boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(TASKER_TOOLS_FSTRIM, value);
            if (value) {
                AlarmHelper.setAlarmFstrim(
                        getActivity(), Integer.parseInt(mFstrimInterval.getValue()));
            } else {
                AlarmHelper.cancelAlarmFstrim(getActivity());
            }
            return true;
        } else if (preference == mFstrimInterval) {
            PreferenceHelper.setString(TASKER_TOOLS_FSTRIM_INTERVAL, newValue.toString());
            if (mFstrim.isChecked()) {
                AlarmHelper.setAlarmFstrim(
                        getActivity(), Integer.parseInt(newValue.toString()));
            }
            return true;
        }

        return false;
    }
}
