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
import android.app.Fragment;
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

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.CPUStateMonitor;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 18.12.13.
 */
public class PerformanceInformationFragment extends Fragment implements DeviceConstants {

    private LinearLayout mStatesView;
    private TextView mAdditionalStates;
    private TextView mTotalStateTime;
    private TextView mHeaderAdditionalStates;
    private TextView mHeaderTotalStateTime;
    private TextView mStatesWarning;
    private ImageView mRefresh;

    private LinearLayout mDeviceInfo;

    private boolean mUpdatingData = false;
    private boolean mUpdatingDevice = false;

    private int mBatteryTemperature = 0;
    private String mBatteryExtra = " - Getting information...";

    private static final int mInterval = 2000;
    private Handler mHandler;

    private CPUStateMonitor monitor = new CPUStateMonitor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            mBatteryExtra = " - Health: "
                    + getBatteryHealth(intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0));
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
        View view = inflater.inflate(R.layout.fragment_performance,
                container, false);

        mStatesView = (LinearLayout) view.findViewById(R.id.ui_states_view);
        mAdditionalStates = (TextView) view.findViewById(R.id.ui_additional_states);
        mHeaderAdditionalStates = (TextView) view.findViewById(R.id.ui_header_additional_states);
        mHeaderTotalStateTime = (TextView) view.findViewById(R.id.ui_header_total_state_time);
        mStatesWarning = (TextView) view.findViewById(R.id.ui_states_warning);
        mTotalStateTime = (TextView) view.findViewById(R.id.ui_total_state_time);
        mRefresh = (ImageView) view.findViewById(R.id.ui_refresh);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("updatingData", mUpdatingData);
    }

    @Override
    public void onResume() {
        refreshData();
        startRepeatingTask();
        super.onResume();
    }

    @Override
    public void onPause() {
        stopRepeatingTask();
        super.onPause();
    }

    private View generateRow(ViewGroup parent, final String title, final String value,
                             final String barLeft, final String barRight, final int progress) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.row_device, parent, false);

        TextView deviceTitle = (TextView) view.findViewById(R.id.ui_device_title);
        TextView deviceValue = (TextView) view.findViewById(R.id.ui_device_value);
        TextView deviceBarLeft = (TextView) view.findViewById(R.id.ui_device_bar_left);
        TextView deviceBarRight = (TextView) view.findViewById(R.id.ui_device_bar_right);
        ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_device_bar);

        deviceTitle.setText(title);
        deviceValue.setText(value);
        deviceBarLeft.setText(barLeft);
        deviceBarRight.setText(barRight);
        bar.setProgress(progress);

        parent.addView(view);
        return view;
    }

    Runnable mDeviceUpdater = new Runnable() {
        @Override
        public void run() {
            if (!mUpdatingDevice) {
                updateStatus();
            }
            mHandler.postDelayed(mDeviceUpdater, mInterval);
        }
    };

    void startRepeatingTask() {
        mDeviceUpdater.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mDeviceUpdater);
    }

    private void updateStatus() {
        mDeviceInfo.removeAllViews();
        generateRow(mDeviceInfo, "CPU Temperature:", CpuUtils.getCpuTemperature() + " °C",
                "0°C", "100°C", CpuUtils.getCpuTemperature());
        generateRow(mDeviceInfo, "Battery Temperature:",
                ((float) mBatteryTemperature) / 10 + " °C" + mBatteryExtra,
                "0°C", "100°C", (mBatteryTemperature / 10));
    }

    public void updateView() {
        mStatesView.removeAllViews();
        List<String> extraStates = new ArrayList<String>();
        for (CPUStateMonitor.CpuState state : monitor.getStates()) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView);
            } else {
                if (state.freq == 0) {
                    extraStates.add(getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.freq / 1000 + " MHz");
                }
            }
        }

        if (monitor.getStates().size() == 0) {
            mStatesWarning.setVisibility(View.VISIBLE);
            mHeaderTotalStateTime.setVisibility(View.GONE);
            mTotalStateTime.setVisibility(View.GONE);
            mStatesView.setVisibility(View.GONE);
        }

        final long totTime = monitor.getTotalStateTime() / 100;
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

    public void refreshData() {
        if (!mUpdatingData) {
            new RefreshStateDataTask().execute((Void) null);
        }
    }

    private static String toString(long tSec) {
        long h = (long) Math.floor(tSec / (60 * 60));
        long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
        long s = tSec % 60;
        String sDur;
        sDur = h + ":";
        if (m < 10) {
            sDur += "0";
        }
        sDur += m + ":";
        if (s < 10) {
            sDur += "0";
        }
        sDur += s;

        return sDur;
    }

    private View generateStateRow(CPUStateMonitor.CpuState state, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.row_state, parent, false);

        float per = (float) state.duration * 100 / monitor.getTotalStateTime();
        String sPer = (int) per + "%";

        String sFreq;
        if (state.freq == 0) {
            sFreq = getString(R.string.deep_sleep);
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        long tSec = state.duration / 100;
        String sDur = toString(tSec);

        TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
        TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
        TextView perText = (TextView) view.findViewById(R.id.ui_percentage_text);
        ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_bar);

        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int) per);

        parent.addView(view);
        return view;
    }

    protected class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... v) {
            try {
                monitor.updateStates();
            } catch (CPUStateMonitor.CPUStateMonitorException e) {
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mUpdatingData = true;
        }

        @Override
        protected void onPostExecute(Void v) {
            updateView();
            mUpdatingData = false;
        }
    }

}
