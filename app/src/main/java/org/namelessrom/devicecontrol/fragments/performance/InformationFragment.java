/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.CpuStateEvent;
import org.namelessrom.devicecontrol.monitors.CpuStateMonitor;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.widgets.AttachFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InformationFragment extends AttachFragment implements DeviceConstants {

    public static final int ID = 200;

    private LinearLayout mStatesView;
    private TextView     mAdditionalStates;
    private TextView     mTotalStateTime;
    private TextView     mHeaderAdditionalStates;
    private TextView     mHeaderTotalStateTime;
    private TextView     mStatesWarning;

    private LinearLayout   mDeviceInfo;
    private LayoutInflater mInflater;

    private boolean mUpdatingData = false;

    private int    mBatteryTemperature = 0;
    private String mBatteryExtra       = " - Getting information...";

    private static final int mInterval = 2000;
    private Handler mHandler;

    private static final Object mLockObject = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID);
        activity.registerReceiver(
                mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        startRepeatingTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
        stopRepeatingTask();
    }

    @Override
    public void onDetach() {
        try {
            mActivity.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) {
            // not registered
        }
        super.onDetach();
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            mBatteryExtra = " - Health: " + getBatteryHealth(
                    intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            );
        }
    };

    private String getBatteryHealth(int healthInt) {
        String health;

        switch (healthInt) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = "cold";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = "good";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = "dead";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = "overvoltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = "overheat";
                break;
            default:
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = "unknown";
                break;
        }

        return health;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mInflater = inflater;
        final View view = mInflater.inflate(R.layout.fragment_performance, container, false);

        mStatesView = (LinearLayout) view.findViewById(R.id.ui_states_view);
        mAdditionalStates = (TextView) view.findViewById(R.id.ui_additional_states);
        mHeaderAdditionalStates = (TextView) view.findViewById(R.id.ui_header_additional_states);
        mHeaderTotalStateTime = (TextView) view.findViewById(R.id.ui_header_total_state_time);
        mStatesWarning = (TextView) view.findViewById(R.id.ui_states_warning);
        mTotalStateTime = (TextView) view.findViewById(R.id.ui_total_state_time);
        final ImageView mRefresh = (ImageView) view.findViewById(R.id.ui_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });

        mDeviceInfo = (LinearLayout) view.findViewById(R.id.ui_device_stats_view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshData();

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    private void refreshData() {
        new CpuStateUpdater().execute();
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
        if (mHandler != null) {
            mHandler.removeCallbacks(mDeviceUpdater);
        }
    }

    @Subscribe
    public void onCpuStateEvent(final CpuStateEvent event) {
        if (event == null) { return; }

        final List<CpuStateMonitor.CpuState> states = event.getStates();
        final long totalStateTime = event.getTotalStateTime();

        mStatesView.removeAllViews();
        final List<String> extraStates = new ArrayList<String>();
        for (CpuStateMonitor.CpuState state : states) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView, totalStateTime);
            } else {
                if (state.freq == 0) {
                    extraStates.add(getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.freq / 1000 + " MHz");
                }
            }
        }

        if (states.size() == 0) {
            mStatesWarning.setVisibility(View.VISIBLE);
            mHeaderTotalStateTime.setVisibility(View.GONE);
            mTotalStateTime.setVisibility(View.GONE);
            mStatesView.setVisibility(View.GONE);
        }

        final long totTime = totalStateTime / 100;
        mTotalStateTime.setText(toString(totTime));

        if (extraStates.size() > 0) {
            int n = 0;
            String str = "";

            for (String s : extraStates) {
                if (n++ > 0) {
                    str += ", ";
                }
                str += s;
            }

            mAdditionalStates.setVisibility(View.VISIBLE);
            mHeaderAdditionalStates.setVisibility(View.VISIBLE);
            mAdditionalStates.setText(str);
        } else {
            mAdditionalStates.setVisibility(View.GONE);
            mHeaderAdditionalStates.setVisibility(View.GONE);
        }
    }

    private static String toString(final long tSec) {
        final long h = (long) Math.floor(tSec / (60 * 60));
        final long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
        final long s = tSec % 60;

        final StringBuilder sDur = new StringBuilder();
        sDur.append(h).append(":");
        if (m < 10) {
            sDur.append("0");
        }
        sDur.append(m).append(":");
        if (s < 10) {
            sDur.append("0");
        }
        sDur.append(s);

        return sDur.toString();
    }

    private View generateRow(final ViewGroup parent, final String title, final String value,
            final String barLeft, final String barRight, final int progress) {

        final LinearLayout view = (LinearLayout) mInflater.inflate(
                R.layout.row_device, parent, false
        );

        final TextView deviceTitle = (TextView) view.findViewById(R.id.ui_device_title);
        final TextView deviceValue = (TextView) view.findViewById(R.id.ui_device_value);
        final TextView deviceBarLeft = (TextView) view.findViewById(R.id.ui_device_bar_left);
        final TextView deviceBarRight = (TextView) view.findViewById(R.id.ui_device_bar_right);
        final ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_device_bar);

        deviceTitle.setText(title);
        deviceValue.setText(value);
        deviceBarLeft.setText(barLeft);
        deviceBarRight.setText(barRight);
        bar.setProgress(progress);

        parent.addView(view);
        return view;
    }

    private View generateStateRow(final CpuStateMonitor.CpuState state, final ViewGroup parent,
            final long totalStateTime) {

        if (!isAdded()) { return null; }

        final LinearLayout view =
                (LinearLayout) mInflater.inflate(R.layout.row_state, parent, false);

        float per = (float) state.duration * 100 / totalStateTime;
        final String sPer = (int) per + "%";

        String sFreq;
        if (state.freq == 0) {
            sFreq = getString(R.string.deep_sleep);
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        long tSec = state.duration / 100;
        final String sDur = toString(tSec);

        final TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
        final TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
        final TextView perText = (TextView) view.findViewById(R.id.ui_percentage_text);
        final ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_bar);

        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int) per);

        parent.addView(view);
        return view;
    }

    private class CpuStateUpdater extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!mUpdatingData) {
                mUpdatingData = true;
                try {
                    CpuStateMonitor.getInstance(getActivity()).updateStates();
                } catch (IOException ignored) { }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mUpdatingData = false;
        }
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
            if (isAdded()) {
                mDeviceInfo.removeAllViews();
                if (cpuTemp != -1) {
                    generateRow(mDeviceInfo, getString(R.string.cpu_temperature), cpuTemp + " °C",
                            "0°C", "100°C", cpuTemp);
                }
                generateRow(mDeviceInfo, getString(R.string.battery_temperature),
                        ((float) mBatteryTemperature) / 10 + " °C" + mBatteryExtra,
                        "0°C", "100°C", (mBatteryTemperature / 10));

                mHandler.postDelayed(mDeviceUpdater, mInterval);
            }
        }
    }

}
