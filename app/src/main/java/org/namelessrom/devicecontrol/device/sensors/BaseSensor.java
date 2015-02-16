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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public abstract class BaseSensor extends LinearLayout implements SensorEventListener {
    private LayoutInflater mInflater;

    private ImageView mIcon;
    private TextView mTitle;
    private TextView mSummary;
    private LinearLayout mDataContainer;

    public int getImageResourceId() {
        return R.mipmap.ic_launcher_devicecontrol; // TODO: get generic sensor icon
    }

    public Drawable getSensorImage() {
        final int color = Application.get().isDarkTheme() ? Color.WHITE : Color.BLACK;
        final Drawable drawable = getResources().getDrawable(getImageResourceId());
        return DrawableHelper.applyColorFilter(drawable, color);
    }

    public int getTitleResourceId() {
        return R.string.empty;
    }

    public int getSummaryResourceId() {
        return R.string.empty;
    }

    public abstract void registerSensor();

    public abstract void unregisterSensor();

    public abstract Sensor getSensor();

    public BaseSensor(final Context context) {
        super(context, null);
        mInflater = LayoutInflater.from(context);

        final int resId;
        if (Application.get().isDarkTheme()) {
            resId = R.layout.card_install_dark;
        } else {
            resId = R.layout.card_install_light;
        }

        mInflater.inflate(resId, this, true);
        final FrameLayout container = (FrameLayout) findViewById(R.id.layout_container);

        mInflater.inflate(R.layout.item_sensor, container, true);

        mIcon = (ImageView) findViewById(R.id.sensor_icon);
        mIcon.setImageDrawable(getSensorImage());

        mTitle = (TextView) findViewById(R.id.sensor_label);
        mTitle.setText(getTitleResourceId());

        mSummary = (TextView) findViewById(R.id.sensor_summary);
        mSummary.setText(getSummaryResourceId());

        mDataContainer = (LinearLayout) findViewById(R.id.sensor_data_container);
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

    public ImageView getIcon() {
        return mIcon;
    }

    public TextView getTitle() {
        return mTitle;
    }

    public TextView getSummary() {
        return mSummary;
    }

    public LinearLayout getDataContainer() {
        return mDataContainer;
    }

}
