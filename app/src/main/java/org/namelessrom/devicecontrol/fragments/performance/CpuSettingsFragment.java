/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.CpuCoreEvent;
import org.namelessrom.devicecontrol.events.CpuFreqEvent;
import org.namelessrom.devicecontrol.events.GovernorEvent;
import org.namelessrom.devicecontrol.events.IoSchedulerEvent;
import org.namelessrom.devicecontrol.monitors.CpuCoreMonitor;
import org.namelessrom.devicecontrol.objects.CpuCore;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.widgets.AttachFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CpuSettingsFragment extends AttachFragment implements PerformanceConstants {

    public static final int ID = 210;

    private CheckBox mStatusHide;
    private Spinner  mMax;
    private Spinner  mMin;
    private Spinner  mGovernor;
    private Spinner  mIo;

    LinearLayout mCpuInfo;
    private LayoutInflater mInflater;

    private static int mInterval = 2000;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        if (mStatusHide != null && !mStatusHide.isChecked()) {
            CpuCoreMonitor.getInstance(getActivity()).start(mInterval);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
        CpuCoreMonitor.getInstance(getActivity()).stop();
    }

    @Subscribe
    public void onCoresUpdated(final CpuCoreEvent event) {
        final List<CpuCore> coreList = event.getStates();
        if (coreList != null && !coreList.isEmpty()) {
            mCpuInfo.removeAllViews();
            for (final CpuCore c : coreList) {
                generateRow(mCpuInfo, c);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        mInflater = inflater;
        final View view = mInflater.inflate(R.layout.fragment_cpu_settings, root, false);

        mCpuInfo = (LinearLayout) view.findViewById(R.id.cpu_info);

        final TextView mIntervalText = (TextView) view.findViewById(R.id.ui_device_value);
        final SeekBar intervalBar = (SeekBar) view.findViewById(R.id.ui_device_bar);
        intervalBar.setMax(4000);
        intervalBar.setProgress(Integer.parseInt(
                PreferenceHelper.getString(PREF_INTERVAL_CPU_INFO, "1000")) - 1000);
        intervalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mIntervalText.setText((progress == 4000
                        ? getString(R.string.off)
                        : (((double) progress + 1000) / 1000) + "s"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mInterval = seekBar.getProgress() + 1000;
                if (mInterval >= 5000) {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                } else {
                    CpuCoreMonitor.getInstance(getActivity()).start(mInterval);
                }
                mIntervalText.setText((mInterval == 5000
                        ? getString(R.string.off)
                        : (((double) mInterval) / 1000) + "s"));
                updateSharedPrefs(PREF_INTERVAL_CPU_INFO, String.valueOf(mInterval));
            }
        });

        ((TextView) view.findViewById(R.id.ui_device_title))
                .setText(R.string.cpu_info_settings_interval);
        mInterval = intervalBar.getProgress() + 1000;
        mIntervalText.setText((mInterval == 5000
                ? getString(R.string.off)
                : (((double) mInterval) / 1000) + "s"));

        mStatusHide = (CheckBox) view.findViewById(R.id.cpu_info_hide);
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                view.findViewById(R.id.ui_interval).setVisibility(b ? View.GONE : View.VISIBLE);
                if (b) {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                    view.findViewById(R.id.speed).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.speed).setVisibility(View.VISIBLE);
                    mInterval = intervalBar.getProgress() + 1000;
                    CpuCoreMonitor.getInstance(getActivity()).start(mInterval);
                }
                updateSharedPrefs(PREF_HIDE_CPU_INFO, b ? "1" : "0");
            }
        });
        mStatusHide.setChecked(PreferenceHelper.getString(PREF_HIDE_CPU_INFO, "1").equals("1"));

        CpuCore tmpCore;
        final int mCpuNum = CpuUtils.getNumOfCpus();
        for (int i = 0; i < mCpuNum; i++) {
            tmpCore = new CpuCore(getString(R.string.core) + " " + String.valueOf(i) + ": ",
                    "0",
                    "0",
                    "0");
            generateRow(mCpuInfo, tmpCore);
        }

        mMax = (Spinner) view.findViewById(R.id.pref_max);
        mMax.setEnabled(false);

        mMin = (Spinner) view.findViewById(R.id.pref_min);
        mMin.setEnabled(false);

        mGovernor = (Spinner) view.findViewById(R.id.pref_governor);
        mGovernor.setEnabled(false);

        final Button govButton = (Button) view.findViewById(R.id.governor_tuning);
        govButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(new CpuGovernorFragment());
            }
        });

        mIo = (Spinner) view.findViewById(R.id.pref_io);
        mIo.setEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }

        final Activity activity = getActivity();

        CpuUtils.getCpuFreqEvent(activity);
        CpuUtils.getGovernorEvent(activity);
        CpuUtils.getIoSchedulerEvent(activity);
    }

    @Subscribe
    public void onCpuFreq(final CpuFreqEvent event) {
        final Activity activity = getActivity();
        if (activity != null && event != null) {
            final String mCurMaxSpeed = event.getCpuFreqMax();
            final String mCurMinSpeed = event.getCpuFreqMin();

            String[] mAvailableFrequencies = event.getCpuFreqAvail();
            Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
                }
            });
            Collections.reverse(Arrays.asList(mAvailableFrequencies));

            final ArrayAdapter<CharSequence> freqAdapter = new ArrayAdapter<CharSequence>(
                    activity, android.R.layout.simple_spinner_item);
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (final String mAvailableFreq : mAvailableFrequencies) {
                freqAdapter.add(CpuUtils.toMHz(mAvailableFreq));
            }

            mMax.setAdapter(freqAdapter);
            mMax.setSelection(Arrays.asList(mAvailableFrequencies).indexOf(mCurMaxSpeed));
            mMax.post(new Runnable() {
                public void run() {
                    mMax.setOnItemSelectedListener(new MaxListener());
                }
            });
            mMax.setEnabled(true);

            mMin.setAdapter(freqAdapter);
            mMin.setSelection(Arrays.asList(mAvailableFrequencies).indexOf(mCurMinSpeed));
            mMin.post(new Runnable() {
                public void run() {
                    mMin.setOnItemSelectedListener(new MinListener());
                }
            });
            mMin.setEnabled(true);
        }
    }

    @Subscribe
    public void onGovernor(final GovernorEvent event) {
        final Activity activity = getActivity();
        if (activity != null && event != null) {
            final String[] availableGovernors = event.getAvailableGovernors();
            final String currentGovernor = event.getCurrentGovernor();
            final ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(
                    activity, android.R.layout.simple_spinner_item);
            governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (final String availableGovernor : availableGovernors) {
                governorAdapter.add(availableGovernor);
            }
            mGovernor.setAdapter(governorAdapter);
            mGovernor.setSelection(Arrays.asList(availableGovernors).indexOf(currentGovernor));
            mGovernor.post(new Runnable() {
                public void run() {
                    mGovernor.setOnItemSelectedListener(new GovListener());
                }
            });
            mGovernor.setEnabled(true);
        }
    }

    @Subscribe
    public void onIoScheduler(final IoSchedulerEvent event) {
        final Activity activity = getActivity();
        if (activity != null && event != null) {
            final String[] mAvailableIo = event.getAvailableIoScheduler();
            final String mCurrentIo = event.getCurrentIoScheduler();
            final ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(
                    activity, android.R.layout.simple_spinner_item
            );
            ioAdapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
            );
            for (final String availableIo : mAvailableIo) {
                ioAdapter.add(availableIo);
            }
            mIo.setAdapter(ioAdapter);
            mIo.setSelection(Arrays.asList(mAvailableIo).indexOf(mCurrentIo));
            mIo.post(new Runnable() {
                public void run() {
                    mIo.setOnItemSelectedListener(new IOListener());
                }
            });
            mIo.setEnabled(true);
        }
    }

    public class MaxListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = CpuUtils.fromMHz(String.valueOf(parent.getItemAtPosition(pos)));
            final String other = CpuUtils.fromMHz(String.valueOf(mMin.getSelectedItem()));
            final boolean updateOther = Integer.parseInt(selected) < Integer.parseInt(other);
            if (updateOther) { mMin.setSelection(pos);}

            final int cpuNum = CpuUtils.getNumOfCpus();
            for (int i = 0; i < cpuNum; i++) {
                CpuUtils.setValue(i, selected, CpuUtils.ACTION_FREQ_MAX);
            }
            updateSharedPrefs(PREF_MAX_CPU, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class MinListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = CpuUtils.fromMHz(String.valueOf(parent.getItemAtPosition(pos)));
            final String other = CpuUtils.fromMHz(String.valueOf(mMax.getSelectedItem()));
            final boolean updateOther = Integer.parseInt(selected) > Integer.parseInt(other);
            if (updateOther) { mMax.setSelection(pos);}

            final int cpuNum = CpuUtils.getNumOfCpus();
            for (int i = 0; i < cpuNum; i++) {
                CpuUtils.setValue(i, selected, CpuUtils.ACTION_FREQ_MIN);
            }
            updateSharedPrefs(PREF_MIN_CPU, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = String.valueOf(parent.getItemAtPosition(pos));
            final int cpuNum = CpuUtils.getNumOfCpus();
            for (int i = 0; i < cpuNum; i++) {
                CpuUtils.setValue(i, selected, CpuUtils.ACTION_GOV);
            }
            updateSharedPrefs(PREF_GOV, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = String.valueOf(parent.getItemAtPosition(pos));
            for (String aIO_SCHEDULER_PATH : IO_SCHEDULER_PATH) {
                if (Utils.fileExists(aIO_SCHEDULER_PATH)) {
                    Utils.writeValue(aIO_SCHEDULER_PATH, selected);
                }
            }
            updateSharedPrefs(PREF_IO, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public View generateRow(final ViewGroup parent, final CpuCore cpuCore) {

        if (!isAdded()) { return null; }

        final View rowView = mInflater.inflate(R.layout.row_device, parent, false);

        final TextView cpuInfoCore = (TextView) rowView.findViewById(R.id.ui_device_title);
        final TextView cpuInfoFreq = (TextView) rowView.findViewById(R.id.ui_device_value);
        final ProgressBar cpuBar = (ProgressBar) rowView.findViewById(R.id.ui_device_bar);

        final boolean isOffline = cpuCore.mCoreCurrent.equals("0");

        cpuInfoCore.setText(cpuCore.mCore);
        cpuInfoFreq.setText(isOffline
                ? getString(R.string.core_offline)
                : CpuUtils.toMHz(cpuCore.mCoreCurrent)
                        + " / " + CpuUtils.toMHz(cpuCore.mCoreMax)
                        + " [" + cpuCore.mCoreGov + "]");
        cpuBar.setMax(Integer.parseInt(cpuCore.mCoreMax));
        cpuBar.setProgress(Integer.parseInt(cpuCore.mCoreCurrent));

        parent.addView(rowView);
        return rowView;
    }

    private void updateSharedPrefs(final String var, final String value) {
        PreferenceHelper.setString(var, value);
    }
}

