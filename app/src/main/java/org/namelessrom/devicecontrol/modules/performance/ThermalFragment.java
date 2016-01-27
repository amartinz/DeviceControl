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
package org.namelessrom.devicecontrol.modules.performance;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.preferences.AwesomePreferenceCategory;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.Utils;

public class ThermalFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override protected int getFragmentId() { return DeviceConstants.ID_CTRL_THERMAL; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_thermal);

        PreferenceCategory category;

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        final AwesomeTogglePreference coreControl =
                (AwesomeTogglePreference) findPreference("core_control");
        if (coreControl.isSupported()) {
            coreControl.initValue();
            coreControl.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(coreControl);
        }

        //------------------------------------------------------------------------------------------
        // MSM-Thermal
        //------------------------------------------------------------------------------------------
        final AwesomePreferenceCategory msmThermal =
                (AwesomePreferenceCategory) findPreference("msm_thermal");
        if (msmThermal.isSupported()) {
            final String path = msmThermal.getPath();
            final String[] files = Utils.listFiles(path, true);
            if (Utils.fileExists(path + "enabled")) {
                AwesomeTogglePreference togglePref = PreferenceUtils.addAwesomeTogglePreference(
                        getActivity(), "msm_thermal_", getString(R.string.thermal_warning),
                        "extras", msmThermal.getPath(), "enabled", msmThermal, this);
                if (togglePref != null) {
                    togglePref.setupTitle();
                }
            }
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    PreferenceUtils.addAwesomeEditTextPreference(getActivity(), "msm_thermal_",
                            "extras", path, file, msmThermal, this);
                }
            }
        }
        removeIfEmpty(getPreferenceScreen(), msmThermal);

        //------------------------------------------------------------------------------------------
        // Intelli-Thermal
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("intelli_thermal");
        final AwesomeTogglePreference mIntelliThermal =
                (AwesomeTogglePreference) findPreference("intelli_thermal_enabled");
        if (mIntelliThermal.isSupported()) {
            mIntelliThermal.initValue();
            mIntelliThermal.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mIntelliThermal);
        }
        removeIfEmpty(getPreferenceScreen(), category);

        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (preference instanceof AwesomeEditTextPreference) {
            ((AwesomeEditTextPreference) preference).writeValue(String.valueOf(o));
            return true;
        } else if (preference instanceof AwesomeTogglePreference) {
            ((AwesomeTogglePreference) preference).writeValue((Boolean) o);
            return true;
        }

        return false;
    }

}


