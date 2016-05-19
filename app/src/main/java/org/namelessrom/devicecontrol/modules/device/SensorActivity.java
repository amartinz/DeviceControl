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
package org.namelessrom.devicecontrol.modules.device;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.theme.AppResources;

import java.util.ArrayList;

import at.amartinz.hardware.sensors.BaseSensor;
import at.amartinz.hardware.sensors.environment.AmbientTemperatureSensor;
import at.amartinz.hardware.sensors.environment.LightSensor;
import at.amartinz.hardware.sensors.environment.PressureSensor;
import at.amartinz.hardware.sensors.environment.RelativeHumiditySensor;
import at.amartinz.hardware.sensors.motion.AccelerometerLinearSensor;
import at.amartinz.hardware.sensors.motion.AccelerometerSensor;
import at.amartinz.hardware.sensors.motion.GravitySensor;
import at.amartinz.hardware.sensors.motion.GyroscopeSensor;
import at.amartinz.hardware.sensors.motion.GyroscopeUncalibratedSensor;
import at.amartinz.hardware.sensors.motion.RotationVectorSensor;
import at.amartinz.hardware.sensors.motion.StepSensor;
import at.amartinz.hardware.sensors.position.GameRotationVectorSensor;
import at.amartinz.hardware.sensors.position.GeomagneticRotationVectorSensor;
import at.amartinz.hardware.sensors.position.MagneticFieldSensor;
import at.amartinz.hardware.sensors.position.MagneticFieldUncalibratedSensor;
import at.amartinz.hardware.sensors.position.ProximitySensor;

public class SensorActivity extends BaseActivity {
    private final ArrayList<BaseSensor> mSensorList = new ArrayList<>();
    private final ArrayList<String> mTitleList = new ArrayList<>();

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

        mTitleList.add(getString(R.string.hardware_environment));
        mTitleList.add(getString(R.string.hardware_motion));
        mTitleList.add(getString(R.string.hardware_position));

        final ViewPagerAdapter adapter = new ViewPagerAdapter(mTitleList);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        final TabLayout tabHost = (TabLayout) findViewById(R.id.tabHost);
        tabHost.setupWithViewPager(viewPager);

        // tint the icons
        final int iconTint = AppResources.get().getAccentColor();
        BaseSensor.setIconTintGlobal(iconTint);

        // and set the background
        BaseSensor.setBackgroundColorGlobal(ContextCompat.getColor(this, R.color.cardview_background));

        // Environment
        final View environmentRoot = findViewById(R.id.environment_layout);
        final LinearLayout environmentContainer =
                (LinearLayout) environmentRoot.findViewById(R.id.sensor_container);

        // Ambient temperature
        if (BaseSensor.isSupported(this, AmbientTemperatureSensor.TYPE)) {
            final AmbientTemperatureSensor amTemperatureSensor = new AmbientTemperatureSensor(this);
            mSensorList.add(amTemperatureSensor);
            environmentContainer.addView(amTemperatureSensor);
        }

        // Light
        if (BaseSensor.isSupported(this, LightSensor.TYPE)) {
            final LightSensor lightSensor = new LightSensor(this);
            mSensorList.add(lightSensor);
            environmentContainer.addView(lightSensor);
        }

        // Pressure
        if (BaseSensor.isSupported(this, PressureSensor.TYPE)) {
            final PressureSensor pressureSensor = new PressureSensor(this);
            mSensorList.add(pressureSensor);
            environmentContainer.addView(pressureSensor);
        }

        // Relative humidity
        if (BaseSensor.isSupported(this, RelativeHumiditySensor.TYPE)) {
            final RelativeHumiditySensor relativeHumiditySensor = new RelativeHumiditySensor(this);
            mSensorList.add(relativeHumiditySensor);
            environmentContainer.addView(relativeHumiditySensor);
        }

        // Motion
        final View motionRoot = findViewById(R.id.motion_layout);
        final LinearLayout motionContainer =
                (LinearLayout) motionRoot.findViewById(R.id.sensor_container);

        // Step counter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BaseSensor.isSupported(this, StepSensor.TYPE)) {
                final StepSensor stepSensor = new StepSensor(this);
                mSensorList.add(stepSensor);
                motionContainer.addView(stepSensor);
            }
        }

        // Accelerometer
        if (BaseSensor.isSupported(this, AccelerometerSensor.TYPE)) {
            final AccelerometerSensor accelerometerSensor = new AccelerometerSensor(this);
            mSensorList.add(accelerometerSensor);
            motionContainer.addView(accelerometerSensor);
        }

        // Accelerometer linear
        if (BaseSensor.isSupported(this, AccelerometerLinearSensor.TYPE)) {
            final AccelerometerLinearSensor accLinearSensor = new AccelerometerLinearSensor(this);
            mSensorList.add(accLinearSensor);
            motionContainer.addView(accLinearSensor);
        }

        // Gravity
        if (BaseSensor.isSupported(this, GravitySensor.TYPE)) {
            final GravitySensor gravitySensor = new GravitySensor(this);
            mSensorList.add(gravitySensor);
            motionContainer.addView(gravitySensor);
        }

        // Rotation vector
        if (BaseSensor.isSupported(this, RotationVectorSensor.TYPE)) {
            final RotationVectorSensor rotationVectorSensor = new RotationVectorSensor(this);
            mSensorList.add(rotationVectorSensor);
            motionContainer.addView(rotationVectorSensor);
        }

        // Gyroscope
        if (BaseSensor.isSupported(this, GyroscopeSensor.TYPE)) {
            final GyroscopeSensor gyroscopeSensor = new GyroscopeSensor(this);
            mSensorList.add(gyroscopeSensor);
            motionContainer.addView(gyroscopeSensor);
        }

        // Gyroscope uncalibrated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (BaseSensor.isSupported(this, GyroscopeUncalibratedSensor.TYPE)) {
                final GyroscopeUncalibratedSensor gyroscopeUncalibratedSensor =
                        new GyroscopeUncalibratedSensor(this);
                mSensorList.add(gyroscopeUncalibratedSensor);
                motionContainer.addView(gyroscopeUncalibratedSensor);
            }
        }

        // Position
        final View positionRoot = findViewById(R.id.position_layout);
        final LinearLayout positionContainer =
                (LinearLayout) positionRoot.findViewById(R.id.sensor_container);

        // Proximity
        if (BaseSensor.isSupported(this, ProximitySensor.TYPE)) {
            final ProximitySensor proximitySensor = new ProximitySensor(this);
            mSensorList.add(proximitySensor);
            positionContainer.addView(proximitySensor);
        }

        // Magnetic field
        if (BaseSensor.isSupported(this, MagneticFieldSensor.TYPE)) {
            final MagneticFieldSensor magneticFieldSensor = new MagneticFieldSensor(this);
            mSensorList.add(magneticFieldSensor);
            positionContainer.addView(magneticFieldSensor);
        }

        // Magnetic field uncalibrated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (BaseSensor.isSupported(this, MagneticFieldUncalibratedSensor.TYPE)) {
                final MagneticFieldUncalibratedSensor magneticFieldUncalibratedSensor =
                        new MagneticFieldUncalibratedSensor(this);
                mSensorList.add(magneticFieldUncalibratedSensor);
                positionContainer.addView(magneticFieldUncalibratedSensor);
            }
        }

        // Geomagnetic rotation vector
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BaseSensor.isSupported(this, GeomagneticRotationVectorSensor.TYPE)) {
                final GeomagneticRotationVectorSensor geomagneticRotationVectorSensor =
                        new GeomagneticRotationVectorSensor(this);
                mSensorList.add(geomagneticRotationVectorSensor);
                positionContainer.addView(geomagneticRotationVectorSensor);
            }
        }

        // Game rotation vector
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (BaseSensor.isSupported(this, GameRotationVectorSensor.TYPE)) {
                final GameRotationVectorSensor gameRotationVectorSensor =
                        new GameRotationVectorSensor(this);
                mSensorList.add(gameRotationVectorSensor);
                positionContainer.addView(gameRotationVectorSensor);
            }
        }
    }

    @Override protected void onPause() {
        super.onPause();

        // unregister all sensors
        for (final BaseSensor sensor : mSensorList) {
            sensor.onPause();
        }
    }

    @Override protected void onResume() {
        super.onResume();

        // lock current orientation
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            }
            case Configuration.ORIENTATION_LANDSCAPE: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            }
        }

        // register all sensors
        for (final BaseSensor sensor : mSensorList) {
            sensor.onResume();
        }
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private final ArrayList<String> mTitles;

        public ViewPagerAdapter(final ArrayList<String> titles) {
            mTitles = titles;
        }

        @Override
        public String getPageTitle(final int position) {
            return mTitles.get(position);
        }

        @Override public Object instantiateItem(ViewGroup container, int position) {
            final int resId;
            switch (position) {
                default:
                case 0: {
                    resId = R.id.environment_layout;
                    break;
                }
                case 1: {
                    resId = R.id.motion_layout;
                    break;
                }
                case 2: {
                    resId = R.id.position_layout;
                    break;
                }
            }
            return findViewById(resId);
        }

        @Override public int getCount() {
            return mTitles.size();
        }

        @Override public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

}
