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
package org.namelessrom.devicecontrol.ui.fragments.preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class FlasherPreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    private CustomListPreference mRecoveryType;

    @Override public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.tools_flasher_preferences);

        int tmp;

        mRecoveryType = (CustomListPreference) findPreference(PREF_RECOVERY_TYPE);
        if (mRecoveryType != null) {
            tmp = PreferenceHelper.getInt(PREF_RECOVERY_TYPE, 0);
            mRecoveryType.setValue(String.valueOf(tmp));
            setSummary(mRecoveryType, tmp);
            mRecoveryType.setOnPreferenceChangeListener(this);
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

    @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mRecoveryType == preference) {
            final int value = Utils.parseInt(String.valueOf(newValue));
            PreferenceHelper.setInt(PREF_RECOVERY_TYPE, value);
            setSummary(preference, value);
            return true;
        }

        return false;
    }

}
