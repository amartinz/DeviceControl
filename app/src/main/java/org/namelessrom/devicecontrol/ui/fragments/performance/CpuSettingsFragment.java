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
package org.namelessrom.devicecontrol.ui.fragments.performance;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.monitors.CpuCoreMonitor;
import org.namelessrom.devicecontrol.objects.CpuCore;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.ui.views.CpuCoreView;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CpuSettingsFragment extends AttachFragment implements DeviceConstants,
        CpuUtils.CoreListener, CpuUtils.FrequencyListener, GovernorUtils.GovernorListener {

    private CheckBox mStatusHide;
    private Spinner  mMax;
    private Spinner  mMin;
    private Spinner  mGovernor;

    private LinearLayout mCpuInfo;

    private static int mInterval = 2000;

    @Override protected int getFragmentId() { return ID_PERFORMANCE_CPU_SETTINGS; }

    @Override public void onResume() {
        super.onResume();
        if (mStatusHide != null && !mStatusHide.isChecked()) {
            CpuCoreMonitor.getInstance(getActivity()).start(this, mInterval);
        }
    }

    @Override public void onPause() {
        super.onPause();
        CpuCoreMonitor.getInstance(getActivity()).stop();
    }

    @Override public void onCores(final CpuUtils.Cores cores) {
        final List<CpuCore> coreList = cores.list;
        if (coreList != null && !coreList.isEmpty()) {
            final int count = coreList.size();
            for (int i = 0; i < count; i++) {
                generateRow(i, coreList.get(i));
            }
        }
    }

    @Override public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // add cpu governor settings entry
        menu.add(0, 0, Menu.NONE, R.string.cpu_governor_tuning)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case 0: // cpu governor
                MainActivity.loadFragment(getActivity(), ID_GOVERNOR_TUNABLE);
                return true;
        }

        return false;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_cpu_settings, root, false);

        mCpuInfo = (LinearLayout) view.findViewById(R.id.cpu_info);

        final TextView mIntervalText = (TextView) view.findViewById(R.id.ui_device_value);
        final SeekBar intervalBar = (SeekBar) view.findViewById(R.id.ui_device_seekbar);
        intervalBar.setMax(4000);
        intervalBar.setProgress(Utils.parseInt(
                PreferenceHelper.getString("pref_interval_cpu_info", "1000")) - 1000);
        intervalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                mIntervalText.setText((progress == 4000
                        ? getString(R.string.off)
                        : (((double) progress + 1000) / 1000) + "s"));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                mInterval = seekBar.getProgress() + 1000;
                if (mInterval >= 5000) {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                } else {
                    CpuCoreMonitor.getInstance(getActivity())
                            .start(CpuSettingsFragment.this, mInterval);
                }
                mIntervalText.setText((mInterval == 5000
                        ? getString(R.string.off)
                        : (((double) mInterval) / 1000) + "s"));
                updateSharedPrefs("pref_interval_cpu_info", String.valueOf(mInterval));
            }
        });

        ((TextView) view.findViewById(R.id.ui_device_title)).setText(R.string.refresh_interval);
        mInterval = intervalBar.getProgress() + 1000;
        mIntervalText.setText((mInterval == 5000
                ? getString(R.string.off)
                : (((double) mInterval) / 1000) + "s"));

        final LinearLayout speed = (LinearLayout) view.findViewById(R.id.speed);
        mStatusHide = (CheckBox) view.findViewById(R.id.cpu_info_hide);
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(final CompoundButton button, final boolean b) {
                view.findViewById(R.id.ui_interval).setVisibility(b ? View.GONE : View.VISIBLE);
                if (b) {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                    speed.setVisibility(View.GONE);
                } else {
                    speed.setVisibility(View.VISIBLE);
                    mInterval = intervalBar.getProgress() + 1000;
                    CpuCoreMonitor.getInstance(getActivity())
                            .start(CpuSettingsFragment.this, mInterval);
                }
                updateSharedPrefs("pref_hide_cpu_info", b ? "1" : "0");
            }
        });
        mStatusHide.setChecked(PreferenceHelper.getString("pref_hide_cpu_info", "1").equals("1"));

        CpuCore tmpCore;
        final int mCpuNum = CpuUtils.get().getNumOfCpus();
        for (int i = 0; i < mCpuNum; i++) {
            tmpCore = new CpuCore(getString(R.string.core) + ' ' + String.valueOf(i) + ": ",
                    "0",
                    "0",
                    "0");
            generateRow(i, tmpCore);
        }

        mMax = (Spinner) view.findViewById(R.id.pref_max);
        mMax.setEnabled(false);

        mMin = (Spinner) view.findViewById(R.id.pref_min);
        mMin.setEnabled(false);

        mGovernor = (Spinner) view.findViewById(R.id.pref_governor);
        mGovernor.setEnabled(false);

        return view;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CpuUtils.get().getCpuFreq(this);
        GovernorUtils.get().getGovernor(this);
    }

    @Override public void onFrequency(final CpuUtils.Frequency cpuFreq) {
        final Activity activity = getActivity();
        if (activity != null && cpuFreq != null) {
            final String[] mAvailableFrequencies = cpuFreq.available;
            Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Utils.tryValueOf(object1, 0).compareTo(Utils.tryValueOf(object2, 0));
                }
            });
            Collections.reverse(Arrays.asList(mAvailableFrequencies));

            final ArrayAdapter<CharSequence> freqAdapter = new ArrayAdapter<CharSequence>(
                    activity, android.R.layout.simple_spinner_item);
            freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (final String mAvailableFreq : mAvailableFrequencies) {
                freqAdapter.add(CpuUtils.toMhz(mAvailableFreq));
            }

            mMax.setAdapter(freqAdapter);
            mMax.setSelection(Arrays.asList(mAvailableFrequencies).indexOf(cpuFreq.maximum));
            mMax.post(new Runnable() {
                public void run() {
                    mMax.setOnItemSelectedListener(new MaxListener());
                }
            });
            mMax.setEnabled(true);

            mMin.setAdapter(freqAdapter);
            mMin.setSelection(Arrays.asList(mAvailableFrequencies).indexOf(cpuFreq.minimum));
            mMin.post(new Runnable() {
                public void run() {
                    mMin.setOnItemSelectedListener(new MinListener());
                }
            });
            mMin.setEnabled(true);
        }
    }

    @Override public void onGovernor(final GovernorUtils.Governor governor) {
        final Activity activity = getActivity();
        if (activity != null && governor != null) {
            final ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(
                    activity, android.R.layout.simple_spinner_item);
            governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (final String availableGovernor : governor.available) {
                governorAdapter.add(availableGovernor);
            }
            mGovernor.setAdapter(governorAdapter);
            mGovernor.setSelection(Arrays.asList(governor.available).indexOf(governor.current));
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
            final boolean updateOther = Utils.parseInt(selected) < Utils.parseInt(other);
            if (updateOther) { mMin.setSelection(pos);}

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, selected, true);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    public class MinListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = CpuUtils.fromMHz(String.valueOf(parent.getItemAtPosition(pos)));
            final String other = CpuUtils.fromMHz(String.valueOf(mMax.getSelectedItem()));
            final boolean updateOther = Utils.parseInt(selected) > Utils.parseInt(other);
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

    public View generateRow(final int core, final CpuCore cpuCore) {
        if (!isAdded() || mCpuInfo == null) { return null; }
        Logger.d(this, String.format("generateRow(%s);", cpuCore.toString()));

        View rowView = mCpuInfo.getChildAt(core);
        if (rowView == null) {
            rowView = new CpuCoreView(getActivity());
            mCpuInfo.addView(rowView);
        }

        if (rowView instanceof CpuCoreView) {
            final boolean isOffline = cpuCore.mCoreCurrent == 0;

            ((CpuCoreView) rowView).core.setText(cpuCore.mCore);
            ((CpuCoreView) rowView).freq.setText(isOffline
                    ? getString(R.string.core_offline)
                    : CpuUtils.toMhz(String.valueOf(cpuCore.mCoreCurrent))
                    + " / " + CpuUtils.toMhz(String.valueOf(cpuCore.mCoreMax))
                    + " [" + cpuCore.mCoreGov + ']');
            ((CpuCoreView) rowView).bar.setMax(cpuCore.mCoreMax);
            ((CpuCoreView) rowView).bar.setProgress(cpuCore.mCoreCurrent);
        }

        return rowView;
    }

    private void updateSharedPrefs(final String var, final String value) {
        PreferenceHelper.setString(var, value);
    }
}

