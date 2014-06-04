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
package org.namelessrom.devicecontrol.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import static butterknife.ButterKnife.findById;

public class DeviceStatusWidget extends LinearLayout {

    private LinearLayout mDeviceInfo;

    private int    mBatteryTemperature = 0;
    private String mBatteryExtra       = " - Getting information...";

    private boolean mIsAttached = false;

    private static final int     mInterval = 2000;
    private static final Handler mHandler  = new Handler();

    private static final Object mLockObject = new Object();

    public DeviceStatusWidget(Context context) {
        super(context);
        createViews(context);
    }

    public DeviceStatusWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        createViews(context);
    }

    public DeviceStatusWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createViews(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        Application.applicationContext.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        try {
            Application.applicationContext.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) { }
        stopRepeatingTask();
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            mBatteryExtra = " - Health: " + Utils.getBatteryHealth(
                    intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0));
        }
    };

    private void createViews(final Context context) {
        final View view = inflate(context, R.layout.widget_device_stats, this);

        mDeviceInfo = findById(view, R.id.ui_device_stats_view);

        startRepeatingTask();
    }

    private View generateRow(final ViewGroup parent, final String title, final String value,
            final String barLeft, final String barRight, final int progress) {

        if (!isAttached()) { return null; }

        final Context context = getContext();

        if (context == null) { return null; }

        final LinearLayout view = (LinearLayout) inflate(context, R.layout.row_device, null);

        ((TextView) findById(view, R.id.ui_device_title)).setText(title);
        ((TextView) findById(view, R.id.ui_device_value)).setText(value);
        ((TextView) findById(view, R.id.ui_device_bar_left)).setText(barLeft);
        ((TextView) findById(view, R.id.ui_device_bar_right)).setText(barRight);
        ((ProgressBar) findById(view, R.id.ui_device_bar)).setProgress(progress);

        parent.addView(view);
        return view;
    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {

        private int cpuTemp;

        @Override
        protected Void doInBackground(Void... voids) {
            cpuTemp = CpuUtils.getCpuTemperature();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isAttached() && getContext() != null) {
                mDeviceInfo.removeAllViews();
                if (cpuTemp != -1) {
                    generateRow(mDeviceInfo, getContext().getString(R.string.cpu_temperature),
                            cpuTemp + " °C", "0°C", "100°C", cpuTemp);
                }
                generateRow(mDeviceInfo, getContext().getString(R.string.battery_temperature),
                        ((float) mBatteryTemperature) / 10 + " °C" + mBatteryExtra,
                        "0°C", "100°C", (mBatteryTemperature / 10));

                mHandler.removeCallbacks(mDeviceUpdater);
                mHandler.postDelayed(mDeviceUpdater, mInterval);
            }
        }
    }

    private boolean isAttached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return isAttachedToWindow();
        } else {
            return mIsAttached;
        }
    }

    final Runnable mDeviceUpdater = new Runnable() {
        @Override
        public void run() {
            synchronized (mLockObject) {
                new UpdateTask().execute();
            }
        }
    };

    void startRepeatingTask() {
        stopRepeatingTask();
        mDeviceUpdater.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mDeviceUpdater);
    }

}
