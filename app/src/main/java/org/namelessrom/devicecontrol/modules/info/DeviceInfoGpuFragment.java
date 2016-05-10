/*
 * Copyright (C) 2013 - 2016 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.modules.info;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;

import at.amartinz.hardware.gpu.GpuInformation;
import at.amartinz.hardware.gpu.GpuInformationListener;
import at.amartinz.hardware.gpu.GpuReader;
import at.amartinz.hardware.opengl.OpenGlInformation;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import hugo.weaving.DebugLog;

public class DeviceInfoGpuFragment extends MaterialSupportPreferenceFragment {

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_dev_gpu;
    }

    @DebugLog @Override public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final MaterialPreferenceCategory gpuCategory = (MaterialPreferenceCategory) view.findViewById(R.id.cat_gpu);
        final MaterialPreferenceCategory openGlCategory = (MaterialPreferenceCategory) view.findViewById(R.id.cat_opengl);

        final ArrayList<String> glesInformation = OpenGlInformation.getOpenGLESInformation();
        final int glesLength = glesInformation.size();

        for (int i = 0; i < glesLength; i++) {
            final String summary = glesInformation.get(i);
            if (!TextUtils.isEmpty(summary)) {
                final String title = getString(OpenGlInformation.GL_STRINGS[i]);
                final boolean postOnGpu = (i < 2);
                view.post(new Runnable() {
                    @Override public void run() {
                        addPreference((postOnGpu ? gpuCategory : openGlCategory), title, summary);
                    }
                });
            }
        }

        GpuReader.getGpuInformation(getActivity(), new GpuInformationListener() {
            @Override public void onGpuInformation(@NonNull final GpuInformation gpuInfo) {
                gpuCategory.post(new Runnable() {
                    @Override public void run() {
                        if (gpuInfo.freqAvailable != null && !gpuInfo.freqAvailable.isEmpty()) {
                            addPreference(gpuCategory, getString(R.string.frequency_available),
                                    GpuInformation.listFrequenciesFormatted(gpuInfo.freqAvailable));
                        }

                        addPreference(gpuCategory, getString(R.string.frequency_max), gpuInfo.freqAsMhz(gpuInfo.freqMax));
                        addPreference(gpuCategory, getString(R.string.frequency_min), gpuInfo.freqAsMhz(gpuInfo.freqMin));
                        addPreference(gpuCategory, getString(R.string.frequency_current), gpuInfo.freqAsMhz(gpuInfo.freqCur));
                    }
                });
            }
        });
    }

    private MaterialPreference addPreference(MaterialPreferenceCategory category, String title, String summary) {
        final MaterialPreference pref = new MaterialPreference(getActivity());
        pref.init(getActivity());
        category.addPreference(pref);
        pref.setTitle(title);
        pref.setSummary(summary);
        return pref;
    }

}
