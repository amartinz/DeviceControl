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
package org.namelessrom.devicecontrol.modules.device.sensors.motion;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.device.sensors.BaseSensor;

public class RotationVectorSensor extends BaseSensor {
    public static final int TYPE = Sensor.TYPE_ROTATION_VECTOR;

    private Sensor mSensor;

    private TextView mValue;

    @Override public int getImageResourceId() {
        return R.drawable.ic_rotation_sensor;
    }

    @Override public Sensor getSensor() {
        return mSensor;
    }

    public RotationVectorSensor(final Context context) {
        super(context);
        getInflater().inflate(R.layout.merge_sensor_data_single, getDataContainer(), true);

        mSensor = getSensorManager().getDefaultSensor(TYPE);

        setup(R.string.sensor_rotation_vector);

        mValue = (TextView) findViewById(R.id.sensor_data_single);
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (mValue == null || event.values.length < 3 || event.values[0] > Integer.MAX_VALUE) {
            return;
        }

        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

        // on some devices in the wild the scalar value does not exist
        final float scalar;
        if (event.values.length >= 4) {
            scalar = event.values[3];
        } else {
            scalar = 0f;
        }
        mValue.post(new Runnable() {
            @Override public void run() {
                mValue.setText(String.format("x: %s\ny: %s\nz: %s\nscalar: %s", x, y, z, scalar));
            }
        });
    }

}
