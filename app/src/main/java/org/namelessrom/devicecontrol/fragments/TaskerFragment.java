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
package org.namelessrom.devicecontrol.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.fragments.tasker.TaskListFragment;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.ParseUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class TaskerFragment extends AttachPreferenceFragment implements DeviceConstants,
        Preference.OnPreferenceChangeListener {

    public static final int ID = 300;

    private CustomCheckBoxPreference mFstrim;
    private CustomListPreference     mFstrimInterval;

    private CustomPreference mTasker;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID); }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tasker);
        setHasOptionsMenu(true);

        final DatabaseHandler db = DatabaseHandler.getInstance(getActivity());

        mFstrim = (CustomCheckBoxPreference) findPreference(FSTRIM);
        if (mFstrim != null) {
            mFstrim.setChecked(PreferenceHelper.getBoolean(FSTRIM));
            mFstrim.setOnPreferenceChangeListener(this);
        }

        mFstrimInterval = (CustomListPreference) findPreference(FSTRIM_INTERVAL);
        if (mFstrimInterval != null) {
            mFstrimInterval.setValueIndex(ParseUtils.getFstrim());
            mFstrimInterval.setOnPreferenceChangeListener(this);
        }

        mTasker = (CustomPreference) findPreference("tasker");
        if (mTasker != null) {
            mTasker.setSummary("Count: " + db.getTableCount(DatabaseHandler.TABLE_TASKER));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mTasker == preference) {
            Utils.startTaskerService(getActivity());
            BusProvider.getBus().post(new TaskListFragment());
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mFstrim == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(FSTRIM, value);
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
            final int realValue = ParseUtils.parseFstrim(value);
            PreferenceHelper.setInt(FSTRIM_INTERVAL, realValue);
            if (mFstrim.isChecked()) {
                AlarmHelper.setAlarmFstrim(getActivity(), realValue);
            }
            logDebug("mFstrimInterval: " + value);
            return true;
        }

        return false;
    }
}
