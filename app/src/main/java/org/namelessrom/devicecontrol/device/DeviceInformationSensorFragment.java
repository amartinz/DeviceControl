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
package org.namelessrom.devicecontrol.device;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeviceInformationSensorFragment extends PreferenceFragment implements DeviceConstants {

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.device_information_sensor);

        final SensorManager sensorManager =
                (SensorManager) Application.get().getSystemService(Context.SENSOR_SERVICE);

        // Sensors
        PreferenceCategory category = (PreferenceCategory) findPreference("sensors");

        // we need an array list to be able to sort it, a normal list throws
        // java.lang.UnsupportedOperationException when sorting
        final ArrayList<Sensor> sensorList = new ArrayList<>(
                sensorManager.getSensorList(Sensor.TYPE_ALL));

        Collections.sort(sensorList, new SortIgnoreCase());

        for (final Sensor s : sensorList) {
            addPreference(category, "", s.getName(), s.getVendor());
        }

        if (category.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(category);
        }
    }

    @Override public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        final String key = preference.getKey();
        if ("sensor_test".equals(key)) {
            final Intent intent = new Intent(getActivity(), SensorActivity.class);
            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private CustomPreference addPreference(final PreferenceCategory category, final String key,
            final String title, final String summary) {
        final CustomPreference preference = new CustomPreference(getActivity());
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(TextUtils.isEmpty(summary) ? getString(R.string.unknown) : summary);
        category.addPreference(preference);
        return preference;
    }

    private class SortIgnoreCase implements Comparator<Sensor> {
        public int compare(final Sensor sensor1, final Sensor sensor2) {
            final String s1 = sensor1 != null ? sensor1.getName() : "";
            final String s2 = sensor2 != null ? sensor2.getName() : "";
            return s1.compareToIgnoreCase(s2);
        }
    }

}
