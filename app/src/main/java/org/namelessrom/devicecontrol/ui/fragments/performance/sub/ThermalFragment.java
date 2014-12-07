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
package org.namelessrom.devicecontrol.ui.fragments.performance.sub;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomePreferenceCategory;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class ThermalFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener {
    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mCoreControl;

    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mIntelliThermalEnabled;

    @Override protected int getFragmentId() { return ID_THERMAL; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_thermal);

        PreferenceCategory category;

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mCoreControl = (AwesomeTogglePreference) findPreference("core_control");
        if (mCoreControl != null) {
            if (mCoreControl.isSupported()) {
                mCoreControl.initValue();
                mCoreControl.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mCoreControl);
            }
        }

        //------------------------------------------------------------------------------------------
        // MSM-Thermal
        //------------------------------------------------------------------------------------------
        final AwesomePreferenceCategory msmThermal =
                (AwesomePreferenceCategory) findPreference("msm_thermal");
        if (msmThermal.isSupported()) {
            final String[] files = Utils.listFiles(msmThermal.getPath(), true);
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    PreferenceUtils.addAwesomeEditTextPreference(getActivity(), "msm_thermal_",
                            "extras", msmThermal.getPath(), file, msmThermal, this);
                } else if (PreferenceUtils.TYPE_CHECKBOX == type) {
                    PreferenceUtils.addAwesomeTogglePreference(getActivity(), "msm_thermal_",
                            getString(R.string.thermal_warning), "extras", msmThermal.getPath(),
                            file, msmThermal, this);
                }
            }
        }
        removeIfEmpty(getPreferenceScreen(), msmThermal);

        //------------------------------------------------------------------------------------------
        // Intelli-Thermal
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("intelli_thermal");
        if (category != null) {
            mIntelliThermalEnabled = (AwesomeTogglePreference)
                    findPreference("intelli_thermal_enabled");
            if (mIntelliThermalEnabled != null) {
                if (mIntelliThermalEnabled.isSupported()) {
                    mIntelliThermalEnabled.initValue();
                    mIntelliThermalEnabled.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliThermalEnabled);
                }
            }
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
        } else if (mCoreControl == preference) {
            mCoreControl.writeValue((Boolean) o);
            return true;
        } else if (mIntelliThermalEnabled == preference) {
            mIntelliThermalEnabled.writeValue((Boolean) o);
            return true;
        }

        return false;
    }

}


