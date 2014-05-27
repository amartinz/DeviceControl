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
import org.namelessrom.devicecontrol.events.CpuCoreEvent;
import org.namelessrom.devicecontrol.events.CpuFreqEvent;
import org.namelessrom.devicecontrol.events.GovernorEvent;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.objects.CpuCore;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.monitors.CpuCoreMonitor;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachFragment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class CpuSettingsFragment extends AttachFragment
        implements DeviceConstants, PerformanceConstants {

    private CheckBox mStatusHide;
    private Spinner  mMax;
    private Spinner  mMin;
    private Spinner  mGovernor;

    LinearLayout mCpuInfo;
    private LayoutInflater mInflater;

    private static int mInterval = 2000;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID_PERFORMANCE_CPU_SETTINGS);
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

        mCpuInfo = findById(view, R.id.cpu_info);

        final TextView mIntervalText = findById(view, R.id.ui_device_value);
        final SeekBar intervalBar = findById(view, R.id.ui_device_bar);
        intervalBar.setMax(4000);
        intervalBar.setProgress(Integer.parseInt(
                PreferenceHelper.getString("pref_interval_cpu_info", "1000")) - 1000);
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
                updateSharedPrefs("pref_interval_cpu_info", String.valueOf(mInterval));
            }
        });

        ((TextView) findById(view, R.id.ui_device_title))
                .setText(R.string.refresh_interval);
        mInterval = intervalBar.getProgress() + 1000;
        mIntervalText.setText((mInterval == 5000
                ? getString(R.string.off)
                : (((double) mInterval) / 1000) + "s"));

        mStatusHide = findById(view, R.id.cpu_info_hide);
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, final boolean b) {
                findById(view, R.id.ui_interval).setVisibility(b ? View.GONE : View.VISIBLE);
                if (b) {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                    findById(view, R.id.speed).setVisibility(View.GONE);
                } else {
                    findById(view, R.id.speed).setVisibility(View.VISIBLE);
                    mInterval = intervalBar.getProgress() + 1000;
                    CpuCoreMonitor.getInstance(getActivity()).start(mInterval);
                }
                updateSharedPrefs("pref_hide_cpu_info", b ? "1" : "0");
            }
        });
        mStatusHide.setChecked(PreferenceHelper.getString("pref_hide_cpu_info", "1").equals("1"));

        CpuCore tmpCore;
        final int mCpuNum = CpuUtils.getNumOfCpus();
        for (int i = 0; i < mCpuNum; i++) {
            tmpCore = new CpuCore(getString(R.string.core) + ' ' + String.valueOf(i) + ": ",
                    0,
                    0,
                    "0");
            generateRow(mCpuInfo, tmpCore);
        }

        mMax = findById(view, R.id.pref_max);
        mMax.setEnabled(false);

        mMin = findById(view, R.id.pref_min);
        mMin.setEnabled(false);

        mGovernor = findById(view, R.id.pref_governor);
        mGovernor.setEnabled(false);

        final Button govButton = findById(view, R.id.governor_tuning);
        govButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(new SubFragmentEvent(ID_GOVERNOR_TUNABLE));
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CpuUtils.getCpuFreqEvent();
        CpuUtils.getGovernorEvent();
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

    public class MaxListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = CpuUtils.fromMHz(String.valueOf(parent.getItemAtPosition(pos)));
            final String other = CpuUtils.fromMHz(String.valueOf(mMin.getSelectedItem()));
            final boolean updateOther = Integer.parseInt(selected) < Integer.parseInt(other);
            if (updateOther) { mMin.setSelection(pos);}

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, selected, true);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class MinListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = CpuUtils.fromMHz(String.valueOf(parent.getItemAtPosition(pos)));
            final String other = CpuUtils.fromMHz(String.valueOf(mMax.getSelectedItem()));
            final boolean updateOther = Integer.parseInt(selected) > Integer.parseInt(other);
            if (updateOther) { mMax.setSelection(pos);}

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, selected, true);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = String.valueOf(parent.getItemAtPosition(pos));
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_GOVERNOR, selected, true);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    // TODO: generate once, then update
    public View generateRow(final ViewGroup parent, final CpuCore cpuCore) {
        if (!isAdded()) { return null; }

        final View rowView = mInflater.inflate(R.layout.row_device, parent, false);

        final TextView cpuInfoCore = findById(rowView, R.id.ui_device_title);
        final TextView cpuInfoFreq = findById(rowView, R.id.ui_device_value);
        final ProgressBar cpuBar = findById(rowView, R.id.ui_device_bar);

        final boolean isOffline = cpuCore.mCoreCurrent == 0;

        cpuInfoCore.setText(cpuCore.mCore);
        cpuInfoFreq.setText(isOffline
                ? getString(R.string.core_offline)
                : CpuUtils.toMHz(String.valueOf(cpuCore.mCoreCurrent))
                        + " / " + CpuUtils.toMHz(String.valueOf(cpuCore.mCoreMax))
                        + " [" + cpuCore.mCoreGov + ']');
        cpuBar.setMax(cpuCore.mCoreMax);
        cpuBar.setProgress(cpuCore.mCoreCurrent);

        assert (rowView != null);

        parent.addView(rowView);
        return rowView;
    }

    private void updateSharedPrefs(final String var, final String value) {
        PreferenceHelper.setString(var, value);
    }
}

