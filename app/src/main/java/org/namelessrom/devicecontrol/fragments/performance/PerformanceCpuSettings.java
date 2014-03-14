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

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.CpuCoreMonitor;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.classes.CpuCore;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class PerformanceCpuSettings extends Fragment
        implements SeekBar.OnSeekBarChangeListener, PerformanceConstants {

    private SeekBar  mMaxSlider;
    private SeekBar  mMinSlider;
    private Spinner  mGovernor;
    private Spinner  mIo;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private String[] mAvailableFrequencies;
    private String   mMaxFreqSetting;
    private String   mMinFreqSetting;

    private TextView mIntervalText;

    LinearLayout mCpuInfo;
    private LayoutInflater mInflater;

    private static int     mInterval  = 2000;
    private static boolean isUpdating = false;
    private Handler mHandler;

    final Object mLockObject = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        mInflater = inflater;
        final View view = mInflater.inflate(R.layout.fragment_cpu_settings, root, false);

        mCpuInfo = (LinearLayout) view.findViewById(R.id.cpu_info);

        mIntervalText = (TextView) view.findViewById(R.id.ui_device_value);
        final SeekBar intervalBar = (SeekBar) view.findViewById(R.id.ui_device_bar);
        intervalBar.setMax(4000);
        intervalBar.setProgress(Integer.parseInt(
                PreferenceHelper.getString(PREF_INTERVAL_CPU_INFO, "1000")) - 1000);
        intervalBar.setOnSeekBarChangeListener(this);
        ((TextView) view.findViewById(R.id.ui_device_title)).setText(
                getString(R.string.cpu_info_settings_interval) + ": ");
        mInterval = intervalBar.getProgress() + 1000;
        mIntervalText.setText((mInterval == 5000
                ? getString(R.string.off)
                : (((double) mInterval) / 1000) + "s"));

        final CheckBox mStatusHide = (CheckBox) view.findViewById(R.id.cpu_info_hide);
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                view.findViewById(R.id.ui_interval).setVisibility(b ? View.GONE : View.VISIBLE);
                if (b) {
                    stopRepeatingTask();
                    view.findViewById(R.id.speed).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.speed).setVisibility(View.VISIBLE);
                    mInterval = intervalBar.getProgress() + 1000;
                    startRepeatingTask();
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
                    CpuUtils.getValue(i, CpuUtils.ACTION_FREQ_MAX),
                    CpuUtils.getValue(i, CpuUtils.ACTION_GOV));
            generateRow(mCpuInfo, tmpCore);
        }

        mAvailableFrequencies = new String[0];

        String availableFrequenciesLine = Utils.readOneLine(FREQ_AVAILABLE_PATH);
        if (availableFrequenciesLine != null) {
            mAvailableFrequencies = availableFrequenciesLine.split(" ");
            Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Integer.valueOf(object1).compareTo(Integer.valueOf(object2));
                }
            });
        }

        final int mFrequenciesNum = mAvailableFrequencies.length - 1;
        final String[] mAvailableGovernors = CpuUtils.getAvailableGovernors().split(" ");
        final String[] mAvailableIo = CpuUtils.getAvailableIOSchedulers();

        final String mCurrentGovernor = CpuUtils.getValue(0, CpuUtils.ACTION_GOV);
        final String mCurrentIo = CpuUtils.getIOScheduler();
        final String mCurMaxSpeed = CpuUtils.getValue(0, CpuUtils.ACTION_FREQ_MAX);
        final String mCurMinSpeed = CpuUtils.getValue(0, CpuUtils.ACTION_FREQ_MIN);

        mMaxSlider = (SeekBar) view.findViewById(R.id.max_slider);
        mMaxSlider.setMax(mFrequenciesNum);
        mMaxSpeedText = (TextView) view.findViewById(R.id.max_speed_text);
        mMaxSpeedText.setText(CpuUtils.toMHz(mCurMaxSpeed));
        mMaxSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(mCurMaxSpeed));
        mMaxFreqSetting = mCurMaxSpeed;
        mMaxSlider.setOnSeekBarChangeListener(this);

        mMinSlider = (SeekBar) view.findViewById(R.id.min_slider);
        mMinSlider.setMax(mFrequenciesNum);
        mMinSpeedText = (TextView) view.findViewById(R.id.min_speed_text);
        mMinSpeedText.setText(CpuUtils.toMHz(mCurMinSpeed));
        mMinSlider.setProgress(Arrays.asList(mAvailableFrequencies).indexOf(mCurMinSpeed));
        mMinFreqSetting = mCurMinSpeed;
        mMinSlider.setOnSeekBarChangeListener(this);

        mGovernor = (Spinner) view.findViewById(R.id.pref_governor);
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(
                getActivity(), android.R.layout.simple_spinner_item);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String mAvailableGovernor : mAvailableGovernors) {
            governorAdapter.add(mAvailableGovernor);
        }
        mGovernor.setAdapter(governorAdapter);
        mGovernor.setSelection(Arrays.asList(mAvailableGovernors).indexOf(mCurrentGovernor));
        mGovernor.post(new Runnable() {
            public void run() {
                mGovernor.setOnItemSelectedListener(new GovListener());
            }
        });

        mIo = (Spinner) view.findViewById(R.id.pref_io);
        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(
                getActivity(), android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String aMAvailableIo : mAvailableIo) {
            ioAdapter.add(aMAvailableIo);
        }
        mIo.setAdapter(ioAdapter);
        mIo.setSelection(Arrays.asList(mAvailableIo).indexOf(mCurrentIo));
        mIo.post(new Runnable() {
            public void run() {
                mIo.setOnItemSelectedListener(new IOListener());
            }
        });

        Switch mSetOnBoot = (Switch) view.findViewById(R.id.cpu_sob);
        mSetOnBoot.setChecked(PreferenceHelper.getBoolean(CPU_SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                PreferenceHelper.setBoolean(CPU_SOB, checked);
                if (checked) {
                    PreferenceHelper.setString(PREF_MIN_CPU,
                            CpuUtils.getValue(0, CpuUtils.ACTION_FREQ_MIN));
                    PreferenceHelper.setString(PREF_MAX_CPU,
                            CpuUtils.getValue(0, CpuUtils.ACTION_FREQ_MAX));
                    PreferenceHelper.setString(PREF_GOV,
                            CpuUtils.getValue(0, CpuUtils.ACTION_GOV));
                    PreferenceHelper.setString(PREF_IO,
                            CpuUtils.getIOScheduler());
                }
            }
        });

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            final int seekBarId = seekBar.getId();
            if (seekBarId == R.id.max_slider) {
                setMaxSpeed(progress);
            } else if (seekBarId == R.id.min_slider) {
                setMinSpeed(progress);
            } else if (seekBarId == R.id.ui_device_bar) {
                mIntervalText.setText((progress == 4000
                        ? getString(R.string.off)
                        : (((double) progress + 1000) / 1000) + "s"));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final int seekBarId = seekBar.getId();
        if (seekBarId == R.id.max_slider || seekBarId == R.id.min_slider) {
            final int cpuCount = CpuUtils.getNumOfCpus();
            for (int i = 0; i < cpuCount; i++) {
                CpuUtils.setValue(i, mMinFreqSetting, CpuUtils.ACTION_FREQ_MIN);
                CpuUtils.setValue(i, mMaxFreqSetting, CpuUtils.ACTION_FREQ_MAX);
                updateSharedPrefs(PREF_MIN_CPU, mMinFreqSetting);
                updateSharedPrefs(PREF_MAX_CPU, mMaxFreqSetting);
            }
        } else if (seekBarId == R.id.ui_device_bar) {
            mInterval = seekBar.getProgress() + 1000;
            if (mInterval >= 5000) {
                stopRepeatingTask();
            } else {
                startRepeatingTask();
            }
            mIntervalText.setText((mInterval == 5000
                    ? getString(R.string.off)
                    : (((double) mInterval) / 1000) + "s"));
            updateSharedPrefs(PREF_INTERVAL_CPU_INFO, String.valueOf(mInterval));
        }
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = parent.getItemAtPosition(pos).toString();
            final int cpuNum = CpuUtils.getNumOfCpus();
            for (int i = 0; i < cpuNum; i++) {
                CpuUtils.setValue(i, selected, CpuUtils.ACTION_GOV);
            }
            updateSharedPrefs(PREF_GOV, selected);
            PreferenceHelper.remove(GOV_SETTINGS);
            PreferenceHelper.remove(GOV_NAME);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final String selected = parent.getItemAtPosition(pos).toString();
            for (String aIO_SCHEDULER_PATH : IO_SCHEDULER_PATH) {
                if (Utils.fileExists(aIO_SCHEDULER_PATH)) {
                    Utils.writeValue(aIO_SCHEDULER_PATH, selected);
                }
            }
            updateSharedPrefs(PREF_IO, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public void setMaxSpeed(int progress) {
        final String current = mAvailableFrequencies[progress];
        final int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeedText.setText(CpuUtils.toMHz(current));
            mMinFreqSetting = current;
        }
        mMaxSpeedText.setText(CpuUtils.toMHz(current));
        mMaxFreqSetting = current;
        updateSharedPrefs(PREF_MAX_CPU, current);
    }

    public void setMinSpeed(int progress) {
        final String current = mAvailableFrequencies[progress];
        final int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(CpuUtils.toMHz(current));
            mMaxFreqSetting = current;
        }
        mMinSpeedText.setText(CpuUtils.toMHz(current));
        mMinFreqSetting = current;
        updateSharedPrefs(PREF_MIN_CPU, current);
    }

    @Override
    public void onResume() {
        startRepeatingTask();
        super.onResume();
    }

    @Override
    public void onPause() {
        stopRepeatingTask();
        super.onPause();
    }

    final Runnable mDeviceUpdater = new Runnable() {
        @Override
        public void run() {
            synchronized (mLockObject) {
                new UpdateTask().execute();
            }
        }
    };

    private void startRepeatingTask() {
        stopRepeatingTask();
        mDeviceUpdater.run();
    }

    private void stopRepeatingTask() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mDeviceUpdater);
        }
    }

    private class UpdateTask extends AsyncTask<Void, Void, Void> {

        private List<CpuCore> coreList;

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isUpdating) {
                isUpdating = true;
                try {
                    if (coreList == null || coreList.isEmpty()) {
                        coreList = CpuCoreMonitor.getInstance(getActivity()).updateStates();
                    }
                } catch (Exception ignored) { }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (coreList != null && !coreList.isEmpty()) {
                mCpuInfo.removeAllViews();
                for (CpuCore c : coreList) {
                    generateRow(mCpuInfo, c);
                }
                coreList.clear();
                coreList = null;
                isUpdating = false;
            }
            mHandler.postDelayed(mDeviceUpdater, mInterval);
        }
    }

    public View generateRow(final ViewGroup parent, final CpuCore cpuCore) {

        if (!isAdded()) {
            return null;
        }

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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            startRepeatingTask();
        } else {
            stopRepeatingTask();
        }
        logDebug(getClass().getSimpleName() + " isVisible:" + (isVisibleToUser ? "true" : "false"));
    }
}

