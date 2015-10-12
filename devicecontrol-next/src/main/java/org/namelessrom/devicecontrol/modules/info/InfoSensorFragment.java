/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */
package org.namelessrom.devicecontrol.modules.info;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import hugo.weaving.DebugLog;

public class InfoSensorFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener {

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_sensors;
    }

    @DebugLog @Override public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MaterialPreference sensorTest = (MaterialPreference) view.findViewById(R.id.sensor_test);
        sensorTest.setOnPreferenceClickListener(this);

        final MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_sensors);

        AsyncTask.execute(new Runnable() {
            @Override public void run() {
                final SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

                // we need an array list, a normal list throws java.lang.UnsupportedOperationException when sorting
                final ArrayList<Sensor> sensorList = new ArrayList<>(sensorManager.getSensorList(Sensor.TYPE_ALL));
                Collections.sort(sensorList, new SortIgnoreCase());
                for (final Sensor s : sensorList) {
                    view.post(new Runnable() {
                        @Override public void run() {
                            addPreference(category, "", s.getName(), s.getVendor());
                        }
                    });
                }
            }
        });
    }

    private MaterialPreference addPreference(MaterialPreferenceCategory category, String key, String title, String summary) {
        final Context context = getActivity();
        final MaterialPreference preference = new MaterialPreference(context);
        preference.init(context);
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(TextUtils.isEmpty(summary) ? getString(R.string.unknown) : summary);
        category.addPreference(preference);
        return preference;
    }

    @Override public boolean onPreferenceClicked(MaterialPreference preference) {
        final String key = preference.getKey();
        if ("sensor_test".equals(key)) {
            final Intent intent = new Intent(getActivity(), SensorActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }

    private class SortIgnoreCase implements Comparator<Sensor> {
        public int compare(final Sensor sensor1, final Sensor sensor2) {
            final String s1 = sensor1 != null ? sensor1.getName() : "";
            final String s2 = sensor2 != null ? sensor2.getName() : "";
            return s1.compareToIgnoreCase(s2);
        }
    }

}
