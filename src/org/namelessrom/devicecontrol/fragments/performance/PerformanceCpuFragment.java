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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DeviceConstants;
import org.namelessrom.devicecontrol.utils.tasks.PerformanceCpuTask;

import java.util.List;

public class PerformanceCpuFragment extends PreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.performance_cpu);


        new PerformanceCpuTask(this).execute();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {

        boolean changed = false;

        return changed;
    }

    public void setResult(List<Boolean> paramResult) {
        PreferenceGroup prefs = (PreferenceCategory) findPreference(CATEGORY_HOTPLUT);

    }

    public static boolean isSupported() {
        return (false);
    }
}
