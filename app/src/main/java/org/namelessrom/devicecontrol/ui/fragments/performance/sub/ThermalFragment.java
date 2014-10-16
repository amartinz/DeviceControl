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
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.Constants;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class ThermalFragment extends AttachPreferenceFragment
        implements DeviceConstants, Constants, Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen          mRoot;
    //----------------------------------------------------------------------------------------------
    private AwesomeCheckBoxPreference mCoreControl;
    //----------------------------------------------------------------------------------------------
    private AwesomeCheckBoxPreference mMsmThermalEnabled;
    private CustomEditTextPreference  mMsmThermalLimit;
    private CustomEditTextPreference  mMsmThermalCoreLimit;
    private CustomEditTextPreference  mMsmThermalCoreMax;
    private CustomEditTextPreference  mMsmThermalCoreMin;
    //----------------------------------------------------------------------------------------------
    private AwesomeCheckBoxPreference mIntelliThermalEnabled;

    @Override protected int getFragmentId() { return ID_THERMAL; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_thermal);

        mRoot = getPreferenceScreen();
        PreferenceCategory category;
        String tmpString;

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mCoreControl = (AwesomeCheckBoxPreference) findPreference("core_control");
        if (mCoreControl != null) {
            if (mCoreControl.isSupported()) {
                mCoreControl.initValue();
                mCoreControl.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mCoreControl);
            }
        }

        //------------------------------------------------------------------------------------------
        // MSM-Thermal
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("msm_thermal");
        if (category != null) {
            mMsmThermalEnabled = (AwesomeCheckBoxPreference) findPreference("msm_thermal_enabled");
            if (mMsmThermalEnabled != null) {
                if (mMsmThermalEnabled.isSupported()) {
                    mMsmThermalEnabled.initValue();
                    mMsmThermalEnabled.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmThermalEnabled);
                }
            }

            mMsmThermalLimit = (CustomEditTextPreference) findPreference("msm_thermal_temp_limit");
            if (mMsmThermalLimit != null) {
                if (Utils.fileExists(MSM_THERMAL_TEMP_LIMIT)) {
                    tmpString = Utils.readOneLine(MSM_THERMAL_TEMP_LIMIT);
                    mMsmThermalLimit.setText(tmpString);
                    mMsmThermalLimit.setSummary(tmpString);
                    mMsmThermalLimit.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmThermalLimit);
                }
            }

            mMsmThermalCoreLimit =
                    (CustomEditTextPreference) findPreference("msm_thermal_core_temp_limit");
            if (mMsmThermalCoreLimit != null) {
                if (Utils.fileExists(MSM_THERMAL_CORE_TEMP_LIMIT)) {
                    tmpString = Utils.readOneLine(MSM_THERMAL_CORE_TEMP_LIMIT);
                    mMsmThermalCoreLimit.setText(tmpString);
                    mMsmThermalCoreLimit.setSummary(tmpString);
                    mMsmThermalCoreLimit.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmThermalCoreLimit);
                }
            }

            mMsmThermalCoreMax = (CustomEditTextPreference) findPreference("msm_thermal_core_max");
            if (mMsmThermalCoreMax != null) {
                if (Utils.fileExists(MSM_THERMAL_MAX_CORE)) {
                    tmpString = Utils.readOneLine(MSM_THERMAL_MAX_CORE);
                    mMsmThermalCoreMax.setText(tmpString);
                    mMsmThermalCoreMax.setSummary(tmpString);
                    mMsmThermalCoreMax.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmThermalCoreMax);
                }
            }

            mMsmThermalCoreMin = (CustomEditTextPreference) findPreference("msm_thermal_core_min");
            if (mMsmThermalCoreMin != null) {
                if (Utils.fileExists(MSM_THERMAL_MIN_CORE)) {
                    tmpString = Utils.readOneLine(MSM_THERMAL_MIN_CORE);
                    mMsmThermalCoreMin.setText(tmpString);
                    mMsmThermalCoreMin.setSummary(tmpString);
                    mMsmThermalCoreMin.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmThermalCoreMin);
                }
            }
        }

        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Intelli-Thermal
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("intelli_thermal");
        if (category != null) {
            mIntelliThermalEnabled = (AwesomeCheckBoxPreference)
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

        removeIfEmpty(category);

        isSupported(mRoot, getActivity());
    }

    private void removeIfEmpty(final PreferenceCategory preferenceCategory) {
        if (mRoot != null && preferenceCategory.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceCategory);
        }
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mCoreControl == preference) {
            mCoreControl.writeValue((Boolean) o);
            return true;
        } else if (mMsmThermalEnabled == preference) {
            mMsmThermalEnabled.writeValue((Boolean) o);
            return true;
        } else if (mIntelliThermalEnabled == preference) {
            mIntelliThermalEnabled.writeValue((Boolean) o);
            return true;
        } else if (mMsmThermalLimit == preference) {
            final String value = validateValue(String.valueOf(o), 0);
            if (value.isEmpty()) return false;
            preferenceChange(mMsmThermalLimit, value, MSM_THERMAL_TEMP_LIMIT);
            return true;
        } else if (mMsmThermalCoreLimit == preference) {
            final String value = validateValue(String.valueOf(o), 1);
            if (value.isEmpty()) return false;
            preferenceChange(mMsmThermalCoreLimit, value, MSM_THERMAL_CORE_TEMP_LIMIT);
            return true;
        } else if (mMsmThermalCoreMax == preference) {
            final String value = validateValue(String.valueOf(o), 2);
            if (value.isEmpty()) return false;
            preferenceChange(mMsmThermalCoreMax, value, MSM_THERMAL_MAX_CORE);
            return true;
        } else if (mMsmThermalCoreMin == preference) {
            final String value = validateValue(String.valueOf(o), 3);
            if (value.isEmpty()) return false;
            preferenceChange(mMsmThermalCoreMin, value, MSM_THERMAL_MIN_CORE);
            return true;
        }

        return false;
    }

    private void preferenceChange(final CustomEditTextPreference pref, final String value,
            final String file) {
        Utils.writeValue(file, value);
        final String currentValue = Utils.readOneLine(file);
        pref.setSummary(currentValue);
        pref.setText(currentValue);
        PreferenceHelper.setBootup(
                new DataItem(DatabaseHandler.CATEGORY_EXTRAS, pref.getKey(), file, value));
    }

    private String validateValue(final String value, final int type) {
        final int parsed;
        try {
            parsed = Utils.parseInt(value);

        } catch (Exception e) { return "";}

        switch (type) {
            case 0:
                if (parsed < 50) {
                    return "50";
                } else if (parsed > 95) {
                    return "95";
                } else {
                    return String.valueOf(parsed);
                }
            case 1:
                if (parsed < 75) {
                    return "75";
                } else if (parsed > 110) {
                    return "110";
                } else {
                    return String.valueOf(parsed);
                }
            case 2:
            case 3:
                if (parsed < 1) {
                    return "1";
                } else if (parsed > 4) {
                    return "4";
                } else {
                    return String.valueOf(parsed);
                }
            default:
                return "";
        }
    }

}


