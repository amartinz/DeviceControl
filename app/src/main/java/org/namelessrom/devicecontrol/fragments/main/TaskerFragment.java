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
package org.namelessrom.devicecontrol.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.fragments.parents.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.AlarmHelper;
import org.namelessrom.devicecontrol.utils.helpers.ParseUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class TaskerFragment extends AttachPreferenceFragment implements DeviceConstants,
        Preference.OnPreferenceChangeListener {

    public static final int ID = 300;

    private CustomCheckBoxPreference mFstrim;
    private ListPreference           mFstrimInterval;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, TaskerFragment.ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tasker);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFstrim = (CustomCheckBoxPreference) findPreference(FSTRIM);
        mFstrim.setChecked(PreferenceHelper.getBoolean(FSTRIM));
        mFstrim.setOnPreferenceChangeListener(this);

        mFstrimInterval = (ListPreference) findPreference(FSTRIM_INTERVAL);
        mFstrimInterval.setValueIndex(ParseUtils.getFstrim());
        mFstrimInterval.setOnPreferenceChangeListener(this);

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mFstrim == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(FSTRIM, value);
            mFstrimInterval.setEnabled(value);
            if (value) {
                AlarmHelper.setAlarmFstrim(getActivity(),
                        ParseUtils.parseFstrim(mFstrimInterval.getValue())
                );
            } else {
                AlarmHelper.cancelAlarmFstrim(getActivity());
            }
            logDebug("mFstrim: " + (value ? "true" : "false"));
            return true;
        } else if (mFstrimInterval == preference) {
            final String value = String.valueOf(newValue);
            PreferenceHelper.setInt(FSTRIM_INTERVAL, ParseUtils.parseFstrim(value));
            if (mFstrim.isChecked()) {
                AlarmHelper.setAlarmFstrim(getActivity(), ParseUtils.parseFstrim(value));
            }
            logDebug("mFstrimInterval: " + value);
            return true;
        }

        return false;
    }
}
