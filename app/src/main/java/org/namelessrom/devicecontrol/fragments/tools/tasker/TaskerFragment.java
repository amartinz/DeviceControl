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
package org.namelessrom.devicecontrol.fragments.tools.tasker;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class TaskerFragment extends AttachPreferenceFragment implements DeviceConstants,
        Preference.OnPreferenceChangeListener {

    private CustomCheckBoxPreference mFstrim;
    private CustomListPreference     mFstrimInterval;

    private CustomPreference mTasker;

    @Override protected int getFragmentId() { return ID_TOOLS_TASKER; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tasker);

        mFstrim = (CustomCheckBoxPreference) findPreference(FSTRIM);
        if (mFstrim != null) {
            mFstrim.setChecked(PreferenceHelper.getBoolean(FSTRIM));
            mFstrim.setOnPreferenceChangeListener(this);
        }

        mFstrimInterval = (CustomListPreference) findPreference(FSTRIM_INTERVAL);
        if (mFstrimInterval != null) {
            mFstrimInterval.setValueIndex(getFstrim());
            mFstrimInterval.setOnPreferenceChangeListener(this);
        }

        mTasker = (CustomPreference) findPreference("tasker");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mTasker == preference) {
            Utils.startTaskerService();
            MainActivity.loadFragment(getActivity(), ID_TOOLS_TASKER_LIST);
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
                        parseFstrim(mFstrimInterval.getValue()));
            } else {
                AlarmHelper.cancelAlarmFstrim(getActivity());
            }
            mFstrim.setChecked(value);
            Logger.v(this, String.format("mFstrim: %s", value));
            return true;
        } else if (mFstrimInterval == preference) {
            final String value = String.valueOf(newValue);
            final int realValue = parseFstrim(value);
            PreferenceHelper.setInt(FSTRIM_INTERVAL, realValue);
            if (mFstrim.isChecked()) {
                AlarmHelper.setAlarmFstrim(getActivity(), realValue);
            }
            Logger.v(this, "mFstrimInterval: " + value);
            return true;
        }

        return false;
    }

    private int parseFstrim(final String position) {
        try {
            return parseFstrim(Integer.parseInt(position));
        } catch (Exception exc) {
            return 480;
        }
    }

    private int parseFstrim(final int position) {
        int value;
        switch (position) {
            case 0:
                value = 5;
                break;
            case 1:
                value = 10;
                break;
            case 2:
                value = 20;
                break;
            case 3:
                value = 30;
                break;
            case 4:
                value = 60;
                break;
            case 5:
                value = 120;
                break;
            case 6:
                value = 240;
                break;
            default:
            case 7:
                value = 480;
                break;
        }
        return value;
    }

    private int getFstrim() {
        int position;

        final int value = PreferenceHelper.getInt(FSTRIM_INTERVAL, 480);
        switch (value) {
            case 5:
                position = 0;
                break;
            case 10:
                position = 1;
                break;
            case 20:
                position = 2;
                break;
            case 30:
                position = 3;
                break;
            case 60:
                position = 4;
                break;
            case 120:
                position = 5;
                break;
            case 240:
                position = 6;
                break;
            default:
            case 480:
                position = 7;
                break;
        }

        return position;
    }
}
