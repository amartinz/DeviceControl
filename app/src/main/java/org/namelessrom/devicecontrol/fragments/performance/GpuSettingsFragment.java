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
package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.GpuEvent;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.GpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;

public class GpuSettingsFragment extends AttachPreferenceFragment
        implements DeviceConstants, PerformanceConstants, Preference.OnPreferenceChangeListener {

    private PreferenceCategory mRoot;
    private CustomListPreference     mGpuFrequency = null;
    private CustomListPreference     mGpuGovernor  = null;
    private CustomCheckBoxPreference m3dScaling    = null;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_PERFORMANCE_GPU_SETTINGS);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gpu);

        mRoot = (PreferenceCategory) getPreferenceScreen().findPreference("gpu");

        GpuUtils.getOnGpuEvent();
    }

    private void refreshPreferences() {
        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            GpuUtils.getOnGpuEvent();
        }
    }

    @Subscribe
    public void onGpuEvent(final GpuEvent event) {
        if (event == null) { return; }

        String tmp;

        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            final String[] frequencies = event.getAvailFreqs();
            final String[] gpuNames = GpuUtils.freqsToMhz(frequencies);

            final int freqsLength = frequencies.length;
            final int namesLength = gpuNames.length;
            tmp = event.getMaxFreq();
            if (tmp != null && !tmp.isEmpty() && freqsLength == namesLength) {
                tmp = tmp.trim();
                for (int i = 0; i < freqsLength; i++) {
                    if (frequencies[i].equals(tmp)) {
                        tmp = gpuNames[i];
                        break;
                    }
                }

                if (mGpuFrequency != null) {
                    mGpuFrequency.setValue(GpuUtils.fromMHz(tmp));
                    mGpuFrequency.setSummary(tmp);
                } else {
                    mGpuFrequency = new CustomListPreference(getActivity());
                    mGpuFrequency.setKey("pref_max_gpu");
                    mGpuFrequency.setTitle(R.string.gpu_freq_max);
                    mGpuFrequency.setEntries(gpuNames);
                    mGpuFrequency.setEntryValues(frequencies);
                    mGpuFrequency.setValue(GpuUtils.fromMHz(tmp));
                    mGpuFrequency.setSummary(tmp);
                    mGpuFrequency.setOnPreferenceChangeListener(this);
                    mRoot.addPreference(mGpuFrequency);
                }
            }

            tmp = event.getGovernor();
            if (tmp != null && !tmp.isEmpty() && GpuUtils.containsGov(tmp)) {
                if (mGpuGovernor != null) {
                    mGpuGovernor.setSummary(tmp);
                    mGpuGovernor.setValue(tmp);
                } else {
                    mGpuGovernor = new CustomListPreference(getActivity());
                    mGpuGovernor.setKey("pref_gpu_gov");
                    mGpuGovernor.setTitle(R.string.gpu_governor);
                    mGpuGovernor.setEntries(GPU_GOVS);
                    mGpuGovernor.setEntryValues(GPU_GOVS);
                    mGpuGovernor.setSummary(tmp);
                    mGpuGovernor.setValue(tmp);
                    mGpuGovernor.setOnPreferenceChangeListener(this);
                    mRoot.addPreference(mGpuGovernor);
                }
            }
        }

        if (Utils.fileExists(FILE_3D_SCALING)) {
            if (m3dScaling == null) {
                tmp = Utils.readOneLine(FILE_3D_SCALING);
                m3dScaling = new CustomCheckBoxPreference(getActivity());
                m3dScaling.setKey(PREF_3D_SCALING);
                m3dScaling.setTitle(R.string.gpu_3d_scaling);
                m3dScaling.setSummary(R.string.gpu_3d_scaling_summary);
                m3dScaling.setChecked(tmp != null && tmp.equals("1"));
                m3dScaling.setOnPreferenceChangeListener(this);
                mRoot.addPreference(m3dScaling);
            }
        }

        isSupported(mRoot, getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        if (mGpuFrequency == preference) {
            final String value = String.valueOf(newValue);
            mGpuFrequency.setValue(value);
            mGpuFrequency.setSummary(GpuUtils.toMhz(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MAX, value, true);
            changed = true;
        } else if (mGpuGovernor == preference) {
            final String value = String.valueOf(newValue);
            mGpuGovernor.setValue(value);
            mGpuGovernor.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_GOVERNOR, value, true);
            changed = true;
        } else if (m3dScaling == preference) {
            final boolean value = (Boolean) newValue;
            m3dScaling.setChecked(value);
            ActionProcessor
                    .processAction(ActionProcessor.ACTION_3D_SCALING, value ? "1" : "0", true);
        }

        if (changed) {
            refreshPreferences();
        }

        return changed;
    }

}


