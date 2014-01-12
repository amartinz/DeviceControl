/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.fragments.performance;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.util.ArrayList;
import java.util.List;

public class PerformanceGeneralFragment extends PreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final String sLcdPowerReduceFile = Utils.checkPaths(FILES_LCD_POWER_REDUCE);
    private static final boolean sLcdPowerReduce = !sLcdPowerReduceFile.equals("");

    private static final String sIntelliPlugEcoFile = Utils.checkPaths(FILES_INTELLI_PLUG_ECO);
    private static final boolean sIntelliPlugEco = !sIntelliPlugEcoFile.equals("");

    private SwitchPreference mLcdPowerReduce;
    private SwitchPreference mIntelliPlugEco;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.performance_general);

        PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference(CATEGORY_POWERSAVING);

        mLcdPowerReduce = (SwitchPreference) findPreference(KEY_LCD_POWER_REDUCE);
        if (sLcdPowerReduce) {
            mLcdPowerReduce.setChecked(Utils.readOneLine(sLcdPowerReduceFile).equals("1"));
            mLcdPowerReduce.setOnPreferenceChangeListener(this);
        } else {
            preferenceGroup.removePreference(mLcdPowerReduce);
        }

        mIntelliPlugEco = (SwitchPreference) findPreference(KEY_INTELLI_PLUG_ECO);
        if (sIntelliPlugEco) {
            mIntelliPlugEco.setChecked(Utils.readOneLine(sIntelliPlugEcoFile).equals("1"));
            mIntelliPlugEco.setOnPreferenceChangeListener(this);
        } else {
            preferenceGroup.removePreference(mIntelliPlugEco);
        }

        if (preferenceGroup.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(preferenceGroup);
        }

        new PerformanceCpuTask().execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mLcdPowerReduce) {
            boolean value = (Boolean) o;
            Utils.writeValue(sLcdPowerReduceFile, (value ? "1" : "0"));
            PreferenceHelper.setBoolean(KEY_LCD_POWER_REDUCE, value);
            changed = true;
        } else if (preference == mIntelliPlugEco) {
            boolean value = (Boolean) o;
            Utils.writeValue(sIntelliPlugEcoFile, (value ? "1" : "0"));
            PreferenceHelper.setBoolean(KEY_INTELLI_PLUG_ECO, value);
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public void setResult(List<Boolean> paramResult) {
        PreferenceGroup prefs = (PreferenceCategory) findPreference(CATEGORY_POWERSAVING);

    }

    public static boolean isSupported() {
        return (sLcdPowerReduce || sIntelliPlugEco);
    }

    public static void restore() {
        if (sLcdPowerReduce) {
            Utils.writeValue(sLcdPowerReduceFile,
                    (PreferenceHelper.getBoolean(KEY_LCD_POWER_REDUCE, false) ? "1" : "0"));
        }
        if (sIntelliPlugEco) {
            Utils.writeValue(sIntelliPlugEcoFile,
                    (PreferenceHelper.getBoolean(KEY_INTELLI_PLUG_ECO, false) ? "1" : "0"));
        }
    }

    //==============================================================================================
    // Internal Classes
    //==============================================================================================

    class PerformanceCpuTask extends AsyncTask<Void, Integer, List<Boolean>>
            implements DeviceConstants {

        @Override
        protected List<Boolean> doInBackground(Void... voids) {
            List<Boolean> tmpList = new ArrayList<Boolean>();

            return tmpList;
        }

        @Override
        protected void onPostExecute(List<Boolean> booleans) {
            setResult(booleans);
        }
    }
}
