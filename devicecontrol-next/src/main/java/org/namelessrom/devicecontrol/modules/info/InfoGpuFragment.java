/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

import alexander.martinz.libs.hardware.gpu.GpuInformation;
import alexander.martinz.libs.hardware.gpu.GpuInformationListener;
import alexander.martinz.libs.hardware.gpu.GpuReader;
import alexander.martinz.libs.hardware.opengl.OpenGlInformation;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class InfoGpuFragment extends MaterialSupportPreferenceFragment {

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_gpu;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ArrayList<String> glesInformation = OpenGlInformation.getOpenGLESInformation();
        final int glesLength = glesInformation.size();

        final MaterialPreferenceCategory gpuCategory = (MaterialPreferenceCategory) view.findViewById(R.id.cat_gpu);
        final MaterialPreferenceCategory openGlCategory = (MaterialPreferenceCategory) view.findViewById(R.id.cat_opengl);

        for (int i = 0; i < glesLength; i++) {
            String summary = glesInformation.get(i);
            if (!TextUtils.isEmpty(summary)) {
                final String title = getString(OpenGlInformation.GL_STRINGS[i]);
                addPreference(((i < 2) ? gpuCategory : openGlCategory), title, summary);
            }
        }

        GpuReader.getGpuInformation(getActivity(), new GpuInformationListener() {
            @Override public void onGpuInformation(@NonNull GpuInformation gpuInformation) {
                final String freqAvail = Utils.listAsString(gpuInformation.freqAvailable);
                if (!TextUtils.isEmpty(freqAvail)) {
                    addPreference(gpuCategory, getString(R.string.frequency_available), freqAvail);
                }

                addPreference(gpuCategory, getString(R.string.frequency_max),
                        gpuInformation.freqAsMhzReadable(gpuInformation.freqMax));
                addPreference(gpuCategory, getString(R.string.frequency_min),
                        gpuInformation.freqAsMhzReadable(gpuInformation.freqMin));
                addPreference(gpuCategory, getString(R.string.frequency_current),
                        gpuInformation.freqAsMhzReadable(gpuInformation.freqCur));
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
