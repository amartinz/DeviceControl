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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.classes.CpuCore;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class PerformanceCpuSettings extends Fragment
        implements SeekBar.OnSeekBarChangeListener, PerformanceConstants {

    private SeekBar mMaxSlider;
    private SeekBar mMinSlider;
    private Spinner mGovernor;
    private Spinner mIo;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private String[] mAvailableFrequencies;
    private String mMaxFreqSetting;
    private String mMinFreqSetting;
    private SharedPreferences mPreferences;

    private TextView mIntervalText;

    private int mCpuNum = 1;
    private CpuInfoListAdapter mCpuInfoListAdapter;
    private List<CpuCore> mCpuInfoListData;
    private LayoutInflater mInflater;

    private static int mInterval = 2000;
    private Handler mHandler;

    public class CpuInfoListAdapter extends ArrayAdapter<CpuCore> {

        public CpuInfoListAdapter(Context context, List<CpuCore> values) {
            super(context, R.layout.row_device, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View rowView = mInflater.inflate(R.layout.row_device, parent, false);

            final TextView cpuInfoCore = (TextView) rowView.findViewById(R.id.ui_device_title);
            final TextView cpuInfoFreq = (TextView) rowView.findViewById(R.id.ui_device_value);
            final ProgressBar cpuBar = (ProgressBar) rowView.findViewById(R.id.ui_device_bar);

            final CpuCore cpuCore = mCpuInfoListData.get(position);
            final boolean isOffline = cpuCore.mCoreCurrent.equals("0");

            cpuInfoCore.setText(cpuCore.mCore);
            cpuInfoFreq.setText(isOffline
                    ? getString(R.string.core_offline)
                    : CpuUtils.toMHz(cpuCore.mCoreCurrent)
                    + " / " + CpuUtils.toMHz(cpuCore.mCoreMax)
                    + " [" + cpuCore.mCoreGov + "]");
            cpuBar.setMax(Integer.parseInt(cpuCore.mCoreMax));
            cpuBar.setProgress(Integer.parseInt(cpuCore.mCoreCurrent));

            return rowView;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = mInflater.inflate(R.layout.fragment_cpu_settings, root, false);

        mIntervalText = (TextView) view.findViewById(R.id.ui_device_value);
        final SeekBar intervalBar = (SeekBar) view.findViewById(R.id.ui_device_bar);
        intervalBar.setMax(5000);
        intervalBar.setProgress(2000);
        ((TextView) view.findViewById(R.id.ui_device_title)).setText(
                getString(R.string.cpu_info_settings_interval) + ": ");
        intervalBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mIntervalText.setText((i == 5000
                        ? getString(R.string.off)
                        : (((double) i) / 1000) + "s"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int progress = seekBar.getProgress();
                mInterval = (progress <= 500 ? 500 : progress);
                seekBar.setProgress(mInterval);
                if (progress >= 5000) {
                    stopRepeatingTask();
                } else {
                    startRepeatingTask();
                }
            }
        });
        mIntervalText.setText((i == 5000
                ? getString(R.string.off)
                : (((double) i) / 1000) + "s"));

        mCpuNum = CpuUtils.getNumOfCpus();

        mCpuInfoListData = new ArrayList<CpuCore>(mCpuNum);
        CpuCore tmpCore;
        for (int i = 0; i < mCpuNum; i++) {
            tmpCore = new CpuCore(getString(R.string.core) + " " + String.valueOf(i) + ": ",
                    "0",
                    CpuUtils.getValue(i, CpuUtils.ACTION_FREQ_MAX),
                    CpuUtils.getValue(i, CpuUtils.ACTION_GOV));
            mCpuInfoListData.add(tmpCore);
        }

        mCpuInfoListAdapter = new CpuInfoListAdapter(getActivity(), mCpuInfoListData);

        ListView mCpuInfoList = (ListView) view.findViewById(R.id.cpu_info_list);
        mCpuInfoList.setAdapter(mCpuInfoListAdapter);

        mAvailableFrequencies = new String[0];

        String availableFrequenciesLine = Utils.readOneLine(STEPS_PATH);
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
        mSetOnBoot.setVisibility(View.GONE); // TODO: add set on boot option
        /*
        mSetOnBoot.setChecked(mPreferences.getBoolean(CPU_SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(CPU_SOB, checked);
                if (checked) {
                    editor.putString(PREF_MIN_CPU, CpuUtils.getCurrentMinSpeed());
                    editor.putString(PREF_MAX_CPU, CpuUtils.getCurrentMaxSpeed());
                    editor.putString(PREF_GOV, CpuUtils.getCurrentGovernor());
                    editor.putString(PREF_IO, CpuUtils.getIOScheduler());
                }
                editor.commit();
            }
        });
        */

        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            if (seekBar.getId() == R.id.max_slider) {
                setMaxSpeed(progress);
            } else if (seekBar.getId() == R.id.min_slider) {
                setMinSpeed(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final int cpuCount = CpuUtils.getNumOfCpus();
        for (int i = 0; i < cpuCount; i++) {
            CpuUtils.setValue(i, mMinFreqSetting, CpuUtils.ACTION_FREQ_MIN);
            CpuUtils.setValue(i, mMaxFreqSetting, CpuUtils.ACTION_FREQ_MAX);
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
            mPreferences.edit().remove(GOV_SETTINGS).remove(GOV_NAME).apply();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            final
            String selected = parent.getItemAtPosition(pos).toString();
            for (String aIO_SCHEDULER_PATH : IO_SCHEDULER_PATH) {
                if (new File(aIO_SCHEDULER_PATH).exists()) {
                    Utils.writeValue(aIO_SCHEDULER_PATH, selected);
                }
            }
            updateSharedPrefs(PREF_IO, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
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

    public void setMaxSpeed(int progress) {
        String current = "";
        current = mAvailableFrequencies[progress];
        int minSliderProgress = mMinSlider.getProgress();
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
        String current = "";
        current = mAvailableFrequencies[progress];
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(CpuUtils.toMHz(current));
            mMaxFreqSetting = current;
        }
        mMinSpeedText.setText(CpuUtils.toMHz(current));
        mMinFreqSetting = current;
        updateSharedPrefs(PREF_MIN_CPU, current);
    }

    Runnable mDeviceUpdater = new Runnable() {
        @Override
        public void run() {
            updateStatus();
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
        List<String> freqs = new ArrayList<String>();
        for (int i = 0; i < mCpuNum; i++) {
            String cpuFreq = CPU_PATH + String.valueOf(i) + CPU_FREQ_TAIL;
            String curFreq = "0";
            if (Utils.fileExists(cpuFreq)) {
                curFreq = Utils.readOneLine(cpuFreq);
            }
            freqs.add(curFreq);
        }
        String[] freqArray = freqs.toArray(new String[freqs.size()]);
        mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, freqArray));
    }


    protected Handler mCurCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            final String[] freqArray = (String[]) msg.obj;
            final int freqCount = (freqArray != null ? freqArray.length : 0);
            CpuCore tmpCore;
            for (int i = 0; i < freqCount; i++) {
                try {
                    final int freqHz = Integer.parseInt(freqArray[i]);

                    if (freqHz == 0) {
                        tmpCore = new CpuCore(getString(R.string.core) + " " + String.valueOf(i) + ": ",
                                "0",
                                CpuUtils.getValue(i, CpuUtils.ACTION_FREQ_MAX),
                                CpuUtils.getValue(i, CpuUtils.ACTION_GOV));
                        mCpuInfoListData.set(i, tmpCore);
                    } else {
                        tmpCore = new CpuCore(getString(R.string.core) + " " + String.valueOf(i) + ": ",
                                (freqHz + ""),
                                CpuUtils.getValue(i, CpuUtils.ACTION_FREQ_MAX),
                                CpuUtils.getValue(i, CpuUtils.ACTION_GOV));
                        mCpuInfoListData.set(i, tmpCore);
                    }
                } catch (Exception e) {
                    // Do nothing
                }
            }
            mCpuInfoListAdapter.notifyDataSetChanged();
        }
    };

    private void updateSharedPrefs(String var, String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(var, value).commit();
    }
}

