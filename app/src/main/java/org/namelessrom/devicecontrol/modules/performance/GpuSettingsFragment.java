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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.preferences.CustomPreferenceCategoryMaterial;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachMaterialPreferenceFragment;

import alexander.martinz.libs.materialpreferences.MaterialListPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;

public class GpuSettingsFragment extends AttachMaterialPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    private CustomPreferenceCategoryMaterial mCatGpu;

    private MaterialListPreference mFreqMax = null;
    private MaterialListPreference mFreqMin = null;
    private MaterialListPreference mGpuGovernor = null;
    private MaterialSwitchPreference m3dScaling = null;

    @Override protected int getFragmentId() {
        return DeviceConstants.ID_CTRL_GRAPHICS;
    }

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_gpu;
    }

    @Override public void onResume() {
        super.onResume();
        getGpu();
    }

    @NonNull @Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedState) {
        final View view = super.onCreateView(inflater, root, savedState);
        mCatGpu = (CustomPreferenceCategoryMaterial) view.findViewById(R.id.cat_gpu);
        return view;
    }

    private void getGpu() {
        final Activity activity = getActivity();
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
                if (mFreqMax != null) {
                    mFreqMax.setValue(tmp);
                } else {
                    mFreqMax = new MaterialListPreference(activity);
                    mFreqMax.init(activity);
                    mFreqMax.setKey("pref_max_gpu");
                    mFreqMax.setTitle(getString(R.string.frequency_max));
                    mFreqMax.setAdapter(mFreqMax.createAdapter(gpuNames, frequencies));
                    mFreqMax.setValue(tmp);
                    mCatGpu.addPreference(mFreqMax);
                    mFreqMax.setOnPreferenceChangeListener(this);
                }
            }

            tmp = gpu.min;
            if (!TextUtils.isEmpty(tmp) && freqsLength != 0 && freqsLength == namesLength) {
                tmp = tmp.trim();
                if (mFreqMin != null) {
                    mFreqMin.setValue(tmp);
                } else {
                    mFreqMin = new MaterialListPreference(activity);
                    mFreqMin.init(activity);
                    mFreqMin.setKey("pref_min_gpu");
                    mFreqMin.setTitle(getString(R.string.frequency_min));
                    mFreqMin.setAdapter(mFreqMin.createAdapter(gpuNames, frequencies));
                    mCatGpu.addPreference(mFreqMin);
                    mFreqMin.setValue(tmp);
                    mFreqMin.setOnPreferenceChangeListener(this);
                }
            }

            tmp = gpu.governor;
            if (!TextUtils.isEmpty(tmp) && GpuUtils.get().containsGov(tmp)) {
                if (mGpuGovernor != null) {
                    mGpuGovernor.setValue(tmp);
                } else {
                    final String[] gpuGovs = GovernorUtils.get().getAvailableGpuGovernors();
                    mGpuGovernor = new MaterialListPreference(activity);
                    mGpuGovernor.init(activity);
                    mGpuGovernor.setKey("pref_gpu_gov");
                    mGpuGovernor.setTitle(getString(R.string.governor));
                    mGpuGovernor.setAdapter(mGpuGovernor.createAdapter(gpuGovs, gpuGovs));
                    mCatGpu.addPreference(mGpuGovernor);
                    mGpuGovernor.setValue(tmp);
                    mGpuGovernor.setOnPreferenceChangeListener(this);
                }
            }
        }

        if (Utils.fileExists(GpuUtils.FILE_3D_SCALING)) {
            if (m3dScaling == null) {
                tmp = Utils.readOneLine(GpuUtils.FILE_3D_SCALING);
                m3dScaling = new MaterialSwitchPreference(activity);
                m3dScaling.init(activity);
                m3dScaling.setKey("3d_scaling");
                m3dScaling.setTitle(getString(R.string.gpu_3d_scaling));
                m3dScaling.setSummary(getString(R.string.gpu_3d_scaling_summary));
                mCatGpu.addPreference(m3dScaling);
                m3dScaling.setChecked(Utils.isEnabled(tmp, false));
                m3dScaling.setOnPreferenceChangeListener(this);
            }
        }

        final View child = mCatGpu.getCardView().getChildAt(0);
        if (child instanceof LinearLayout && ((LinearLayout) child).getChildCount() <= 1) {
            MaterialPreference noSupportPref = new MaterialPreference(activity);
            noSupportPref.init(activity);
            mCatGpu.addPreference(noSupportPref);
            noSupportPref.setTitle(getString(R.string.no_tweaks_available));
            noSupportPref.setSummary(getString(R.string.no_tweaks_message));
        }
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object objVal) {
        if (mFreqMax == preference) {
            final String value = String.valueOf(objVal);
            mFreqMax.setValue(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MAX, value, true);
            return true;
        } else if (mFreqMin == preference) {
            final String value = String.valueOf(objVal);
            mFreqMin.setValue(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MIN, value, true);
            return true;
        } else if (mGpuGovernor == preference) {
            final String value = String.valueOf(objVal);
            mGpuGovernor.setValue(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_GOVERNOR, value, true);
            return true;
        } else if (m3dScaling == preference) {
            final boolean value = (Boolean) objVal;
            m3dScaling.setChecked(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_3D_SCALING, value ? "1" : "0", true);
            return true;
        }

        return false;
    }

}


