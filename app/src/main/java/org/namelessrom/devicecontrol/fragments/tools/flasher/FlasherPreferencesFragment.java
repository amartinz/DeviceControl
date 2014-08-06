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
package org.namelessrom.devicecontrol.fragments.tools.flasher;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class FlasherPreferencesFragment extends AttachPreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants, PerformanceConstants {

    private CustomListPreference mRecoveryType;

    private CustomCheckBoxPreference mMultiUserMode;

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_FLASHER_PREFS);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.tools_flasher_preferences);

        int tmp;

        mRecoveryType = (CustomListPreference) findPreference(PREF_RECOVERY_TYPE);
        if (mRecoveryType != null) {
            tmp = PreferenceHelper.getInt(PREF_RECOVERY_TYPE, 0);
            mRecoveryType.setValue(String.valueOf(tmp));
            setSummary(mRecoveryType, tmp);
            mRecoveryType.setOnPreferenceChangeListener(this);
        }

        mMultiUserMode = (CustomCheckBoxPreference) findPreference(PREF_FLASHER_MULTI_USER);
        if (mMultiUserMode != null) {
            mMultiUserMode.setChecked(PreferenceHelper.getBoolean(PREF_FLASHER_MULTI_USER, false));
            mMultiUserMode.setOnPreferenceChangeListener(this);
        }

    }

    private void setSummary(final Preference preference, final int value) {
        int resId = R.string.unknown;

        if (mRecoveryType == preference) {
            switch (value) {
                case RECOVERY_TYPE_BOTH:
                    resId = R.string.both;
                    break;
                case RECOVERY_TYPE_CWM:
                    resId = R.string.clockworkmod;
                    break;
                case RECOVERY_TYPE_OPEN:
                    resId = R.string.open_recovery;
                    break;
            }
        }

        preference.setSummary(resId);
    }


    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        //TODO: bg
        /*if (view != null) {
            view.setBackgroundResource(R.drawable.preference_drawer_background);
        }*/

        return view;
    }

    @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mRecoveryType == preference) {
            final int value = Integer.parseInt(String.valueOf(newValue));
            PreferenceHelper.setInt(PREF_RECOVERY_TYPE, value);
            setSummary(preference, value);
            return true;
        } else if (mMultiUserMode == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(PREF_FLASHER_MULTI_USER, value);
            return true;
        }

        return false;
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

}
