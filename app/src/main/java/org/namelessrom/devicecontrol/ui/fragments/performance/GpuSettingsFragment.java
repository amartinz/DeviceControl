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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.ui.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.Constants;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.GL_RENDERER;
import static android.opengl.GLES20.GL_SHADING_LANGUAGE_VERSION;
import static android.opengl.GLES20.GL_VENDOR;
import static android.opengl.GLES20.GL_VERSION;
import static android.opengl.GLES20.glGetString;

public class GpuSettingsFragment extends AttachPreferenceFragment implements Constants,
        DeviceConstants, GpuUtils.GpuListener, Preference.OnPreferenceChangeListener {

    private PreferenceCategory mRoot;

    private CustomListPreference     mGpuFrequency = null;
    private CustomListPreference     mGpuGovernor  = null;
    private CustomCheckBoxPreference m3dScaling    = null;

    private static final int[] GL_INFO = new int[]{
            GL_VENDOR,                  // gpu vendor
            GL_RENDERER,                // gpu renderer
            GL_VERSION,                 // opengl version
            GL_EXTENSIONS,              // opengl extensions
            GL_SHADING_LANGUAGE_VERSION // shader language version
    };

    private static final int[] GL_STRINGS = new int[]{
            R.string.gpu_vendor,        // gpu vendor
            R.string.gpu_renderer,      // gpu renderer
            R.string.opengl_version,    // opengl version
            R.string.opengl_extensions, // opengl extensions
            R.string.shader_version     // shader language version
    };

    @Override protected int getFragmentId() { return ID_PERFORMANCE_GPU_SETTINGS; }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.gpu);
        mRoot = (PreferenceCategory) getPreferenceScreen().findPreference("gpu");

        final PreferenceCategory category = (PreferenceCategory) findPreference("gpu_opengl");
        if (GpuUtils.isOpenGLES20Supported()) {
            // our preference and string for storing gpu / opengl information
            Preference infoPref;
            String tmp;

            final int length = GL_INFO.length;
            for (int i = 0; i < length; i++) {
                tmp = glGetString(GL_INFO[i]);
                if (!TextUtils.isEmpty(tmp)) {
                    infoPref = new Preference(getActivity());
                    infoPref.setTitle(GL_STRINGS[i]);
                    infoPref.setSummary(tmp);
                    infoPref.setSelectable(false);
                    category.addPreference(infoPref);
                }
            }
        }

        // if we are not supported or we could not add any preferences, remove it
        if (category.getPreferenceCount() <= 0) {
            getPreferenceScreen().removePreference(category);
        }

        GpuUtils.get().getGpu(this);
    }

    private void refreshPreferences() {
        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            GpuUtils.get().getGpu(this);
        }
    }

    @Override public void onGpu(final GpuUtils.Gpu gpu) {
        String tmp;

        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            final String[] frequencies = gpu.available;
            final String[] gpuNames = GpuUtils.freqsToMhz(frequencies);

            final int freqsLength = frequencies.length;
            final int namesLength = gpuNames.length;
            tmp = gpu.max;
            if (!TextUtils.isEmpty(tmp) && freqsLength == namesLength) {
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

            tmp = gpu.governor;
            if (!TextUtils.isEmpty(tmp) && GpuUtils.get().containsGov(tmp)) {
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

    @Override public boolean onPreferenceChange(final Preference preference, final Object objVal) {
        boolean changed = false;

        if (mGpuFrequency == preference) {
            final String value = String.valueOf(objVal);
            mGpuFrequency.setValue(value);
            mGpuFrequency.setSummary(GpuUtils.toMhz(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MAX, value, true);
            changed = true;
        } else if (mGpuGovernor == preference) {
            final String value = String.valueOf(objVal);
            mGpuGovernor.setValue(value);
            mGpuGovernor.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_GOVERNOR, value, true);
            changed = true;
        } else if (m3dScaling == preference) {
            final boolean value = (Boolean) objVal;
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


