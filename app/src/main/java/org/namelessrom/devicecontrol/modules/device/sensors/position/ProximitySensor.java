/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.device.sensors.position;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.device.sensors.BaseSensor;

public class ProximitySensor extends BaseSensor {
    public static final int TYPE = Sensor.TYPE_PROXIMITY;

    private Sensor mSensor;
    private float mMaxRange;

    private TextView mState;

    @Override public int getImageResourceId() {
        // TODO: icon
        return R.drawable.empty_icon;
    }

    @Override public Sensor getSensor() {
        return mSensor;
    }

    public ProximitySensor(final Context context) {
        super(context);
        getInflater().inflate(R.layout.merge_sensor_data_single, getDataContainer(), true);

        mSensor = getSensorManager().getDefaultSensor(TYPE);
        mMaxRange = mSensor.getMaximumRange();

        setup(R.string.sensor_proximity);

        mState = (TextView) findViewById(R.id.sensor_data_single);
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (mState == null || event.values[0] > Integer.MAX_VALUE) {
            return;
        }

        final float state = event.values[0];
        final boolean isFar = state >= mMaxRange;
        final String stateString = isFar
                ? getResources().getString(R.string.far)
                : getResources().getString(R.string.near);
        mState.post(new Runnable() {
            @Override public void run() {
                mState.setText(String.format("%s (%s)", stateString, String.valueOf(state)));
            }
        });
    }

}
