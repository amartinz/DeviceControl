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
package org.namelessrom.devicecontrol.modules.device.sensors;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public abstract class BaseSensor extends FrameLayout implements SensorEventListener {
    private SensorManager mSensorManager;
    private LayoutInflater mInflater;

    private ImageView mIcon;
    private TextView mTitle;
    private TextView mName;
    private TextView mVendor;
    private TextView mPowerUsage;
    private LinearLayout mDataContainer;

    public abstract Sensor getSensor();

    public static boolean isSupported(final Context context, final int type) {
        return ((SensorManager) context.getSystemService(Context.SENSOR_SERVICE))
                .getDefaultSensor(type) != null;
    }

    public int getImageResourceId() {
        return R.drawable.ic_walk;
    }

    public Drawable getSensorImage() {
        final int color = AppResources.get().isDarkTheme() ? Color.WHITE : Color.BLACK;
        final Drawable drawable = ContextCompat.getDrawable(getContext(), getImageResourceId());
        return DrawableHelper.applyColorFilter(drawable, color);
    }

    public void registerSensor() {
        getSensorManager().registerListener(this, getSensor(), getSensorDelay());
    }

    public void unregisterSensor() {
        getSensorManager().unregisterListener(this, getSensor());
    }

    public BaseSensor(final Context context) {
        super(context, null);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mInflater = LayoutInflater.from(context);

        View v = LayoutInflater.from(context).inflate(R.layout.card_with_container, this, false);
        CardView cardView = (CardView) v.findViewById(R.id.card_view_root);
        cardView.setCardBackgroundColor(AppResources.get().getCardBackgroundColor());
        super.addView(v);

        final FrameLayout container = (FrameLayout) findViewById(R.id.layout_container);

        mInflater.inflate(R.layout.item_sensor, container, true);

        mIcon = (ImageView) findViewById(R.id.sensor_icon);
        mIcon.setImageDrawable(getSensorImage());

        mTitle = (TextView) findViewById(R.id.sensor_label);
        mName = (TextView) findViewById(R.id.sensor_name);
        mVendor = (TextView) findViewById(R.id.sensor_vendor);
        mPowerUsage = (TextView) findViewById(R.id.sensor_power_usage);

        mDataContainer = (LinearLayout) findViewById(R.id.sensor_data_container);
    }

    public void setup(final int titleResId) {
        getTitle().setText(titleResId);
        // set sensor name
        getName().setText(getSensor().getName());
        // set sensor vendor
        getVendor().setText(String.format("(%s)", getSensor().getVendor()));
        // set power usage
        getPowerUsage().setText(getPowerUsageString());
    }

    public void onResume() {
        registerSensor();
    }

    public void onPause() {
        unregisterSensor();
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public SensorManager getSensorManager() {
        return mSensorManager;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public TextView getTitle() {
        return mTitle;
    }

    public TextView getName() {
        return mName;
    }

    public TextView getVendor() {
        return mVendor;
    }

    public TextView getPowerUsage() {
        return mPowerUsage;
    }

    public LinearLayout getDataContainer() {
        return mDataContainer;
    }

    public String getPowerUsageString() {
        return String.format("%s: %s mA",
                getResources().getString(R.string.power_usage), getSensor().getPower());
    }

    public int getSensorDelay() {
        return SensorManager.SENSOR_DELAY_UI;
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Logger.d(this, "onAccuracyChanged: %s (%s), %s", sensor.getName(), sensor.getVendor(),
                accuracy);
    }

}
