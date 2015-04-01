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
package org.namelessrom.devicecontrol.modules.performance;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class GpuSettingsFragment extends AttachPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private PreferenceCategory mRoot;

    private CustomListPreference mFreqMax = null;
    private CustomListPreference mFreqMin = null;
    private CustomListPreference mGpuGovernor = null;
    private CustomTogglePreference m3dScaling = null;

    @Override protected int getFragmentId() {
        return DeviceConstants.ID_PERFORMANCE_GPU_SETTINGS;
    }

    @Override public void onResume() {
        super.onResume();
        getGpu();
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.gpu);
        mRoot = (PreferenceCategory) getPreferenceScreen().findPreference("gpu");

        final PreferenceCategory category = (PreferenceCategory) findPreference("gpu_opengl");
        if (GpuUtils.isOpenGLES20Supported()) {
            // our preference and string for storing gpu / opengl information
            Preference infoPref;

            final ArrayList<String> glesInformation = GpuUtils.getOpenGLESInformation();

            String tmp;
            for (int i = 0; i < glesInformation.size(); i++) {
                tmp = glesInformation.get(i);
                if (!TextUtils.isEmpty(tmp)) {
                    infoPref = new CustomPreference(getActivity());
                    infoPref.setTitle(GpuUtils.GL_STRINGS[i]);
                    infoPref.setSummary(tmp);
                    category.addPreference(infoPref);
                }
            }
        }

        // if we are not supported or we could not add any preferences, remove it
        if (category.getPreferenceCount() <= 0) {
            getPreferenceScreen().removePreference(category);
        }
    }

    private void getGpu() {
        String tmp;

        if (Utils.fileExists(GpuUtils.get().getGpuBasePath())) {
            final GpuUtils.Gpu gpu = GpuUtils.get().getGpu();
            final String[] frequencies = gpu.available;
            final String[] gpuNames = GpuUtils.freqsToMhz(frequencies);

            final int freqsLength = frequencies == null ? 0 : frequencies.length;
            final int namesLength = gpuNames == null ? 0 : gpuNames.length;
            tmp = gpu.max;
            if (!TextUtils.isEmpty(tmp) && freqsLength != 0 && freqsLength == namesLength) {
                tmp = tmp.trim();
                for (int i = 0; i < freqsLength; i++) {
                    if (frequencies[i].equals(tmp)) {
                        tmp = gpuNames[i];
                        break;
                    }
                }

                if (mFreqMax != null) {
                    mFreqMax.setValue(GpuUtils.fromMHz(tmp));
                    mFreqMax.setSummary(tmp);
                } else {
                    mFreqMax = new CustomListPreference(getActivity());
                    mFreqMax.setKey("pref_max_gpu");
                    mFreqMax.setTitle(R.string.gpu_freq_max);
                    mFreqMax.setEntries(gpuNames);
                    mFreqMax.setEntryValues(frequencies);
                    mFreqMax.setValue(GpuUtils.fromMHz(tmp));
                    mFreqMax.setSummary(tmp);
                    mFreqMax.setOnPreferenceChangeListener(this);
                    mRoot.addPreference(mFreqMax);
                }
            }

            tmp = gpu.min;
            if (!TextUtils.isEmpty(tmp) && freqsLength != 0 && freqsLength == namesLength) {
                tmp = tmp.trim();
                for (int i = 0; i < freqsLength; i++) {
                    if (frequencies[i].equals(tmp)) {
                        tmp = gpuNames[i];
                        break;
                    }
                }

                if (mFreqMin != null) {
                    mFreqMin.setValue(GpuUtils.fromMHz(tmp));
                    mFreqMin.setSummary(tmp);
                } else {
                    mFreqMin = new CustomListPreference(getActivity());
                    mFreqMin.setKey("pref_min_gpu");
                    mFreqMin.setTitle(R.string.gpu_freq_min);
                    mFreqMin.setEntries(gpuNames);
                    mFreqMin.setEntryValues(frequencies);
                    mFreqMin.setValue(GpuUtils.fromMHz(tmp));
                    mFreqMin.setSummary(tmp);
                    mFreqMin.setOnPreferenceChangeListener(this);
                    mRoot.addPreference(mFreqMin);
                }
            }

            tmp = gpu.governor;
            if (!TextUtils.isEmpty(tmp) && GpuUtils.get().containsGov(tmp)) {
                if (mGpuGovernor != null) {
                    mGpuGovernor.setSummary(tmp);
                    mGpuGovernor.setValue(tmp);
                } else {
                    final String[] gpuGovs = GovernorUtils.get().getAvailableGpuGovernors();
                    mGpuGovernor = new CustomListPreference(getActivity());
                    mGpuGovernor.setKey("pref_gpu_gov");
                    mGpuGovernor.setTitle(R.string.gpu_governor);
                    mGpuGovernor.setEntries(gpuGovs);
                    mGpuGovernor.setEntryValues(gpuGovs);
                    mGpuGovernor.setSummary(tmp);
                    mGpuGovernor.setValue(tmp);
                    mGpuGovernor.setOnPreferenceChangeListener(this);
                    mRoot.addPreference(mGpuGovernor);
                }
            }
        }

        if (Utils.fileExists(GpuUtils.FILE_3D_SCALING)) {
            if (m3dScaling == null) {
                tmp = Utils.readOneLine(GpuUtils.FILE_3D_SCALING);
                m3dScaling = new CustomTogglePreference(getActivity());
                m3dScaling.setKey("3d_scaling");
                m3dScaling.setTitle(R.string.gpu_3d_scaling);
                m3dScaling.setSummary(R.string.gpu_3d_scaling_summary);
                m3dScaling.setChecked(tmp != null && tmp.equals("1"));
                m3dScaling.setOnPreferenceChangeListener(this);
                mRoot.addPreference(m3dScaling);
            }
        }

        isSupported(mRoot, getActivity());
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object objVal) {
        if (mFreqMax == preference) {
            final String value = String.valueOf(objVal);
            mFreqMax.setValue(value);
            mFreqMax.setSummary(GpuUtils.toMhz(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MAX, value, true);
            return true;
        } else if (mFreqMin == preference) {
            final String value = String.valueOf(objVal);
            mFreqMin.setValue(value);
            mFreqMin.setSummary(GpuUtils.toMhz(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MIN, value, true);
            return true;
        } else if (mGpuGovernor == preference) {
            final String value = String.valueOf(objVal);
            mGpuGovernor.setValue(value);
            mGpuGovernor.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_GOVERNOR, value, true);
            return true;
        } else if (m3dScaling == preference) {
            final boolean value = (Boolean) objVal;
            m3dScaling.setChecked(value);
            ActionProcessor
                    .processAction(ActionProcessor.ACTION_3D_SCALING, value ? "1" : "0", true);
            return true;
        }

        return false;
    }

}


