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
package org.namelessrom.devicecontrol.device.sensors;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;

@TargetApi(Build.VERSION_CODES.KITKAT)
public class StepSensor extends BaseSensor {
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private TextView mSteps;

    @Override public int getImageResourceId() {
        return R.drawable.ic_walk;
    }

    @Override public Sensor getSensor() {
        return mSensor;
    }

    public StepSensor(final Context context) {
        super(context);
        getInflater().inflate(R.layout.merge_sensor_step, getDataContainer(), true);

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // Set sensor name as title
        getTitle().setText(mSensor.getName());
        // Set sensor vendor (version) as summary
        getSummary().setText(String.format("%s (%s)", mSensor.getVendor(), mSensor.getVersion()));

        mSteps = (TextView) findViewById(R.id.sensor_step_steps);
    }

    @Override public void registerSensor() {
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override public void unregisterSensor() {
        mSensorManager.unregisterListener(this, mSensor);
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (mSteps == null || event.values[0] > Integer.MAX_VALUE) {
            return;
        }

        final int steps = (int) event.values[0];
        mSteps.post(new Runnable() {
            @Override public void run() {
                mSteps.setText(String.valueOf(steps));
            }
        });
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Logger.d(this, "onAccuracyChanged: %s (%s), %s", sensor.getName(), sensor.getVendor(),
                accuracy);
    }
}
