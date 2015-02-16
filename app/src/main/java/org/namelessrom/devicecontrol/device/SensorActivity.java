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
package org.namelessrom.devicecontrol.device;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.device.sensors.BaseSensor;
import org.namelessrom.devicecontrol.device.sensors.StepSensor;

import java.util.ArrayList;

public class SensorActivity extends BaseActivity {
    private final ArrayList<BaseSensor> mSensorList = new ArrayList<>();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // setup action bar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
        final MaterialMenuIconToolbar materialMenu =
                new MaterialMenuIconToolbar(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
                    @Override public int getToolbarViewId() { return R.id.toolbar; }
                };
        materialMenu.setState(MaterialMenuDrawable.IconState.ARROW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            materialMenu.setNeverDrawTouch(true);
        }

        final LinearLayout sensorContainer = (LinearLayout) findViewById(R.id.sensor_container);
        sensorContainer.removeAllViews();
        final StepSensor stepSensor = new StepSensor(this);
        mSensorList.add(stepSensor);
        sensorContainer.addView(stepSensor);
    }

    @Override protected void onPause() {
        super.onPause();
        for (final BaseSensor sensor : mSensorList) {
            sensor.onPause();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        for (final BaseSensor sensor : mSensorList) {
            sensor.onResume();
        }
    }
}
