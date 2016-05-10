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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;

import java.util.List;

import at.amartinz.hardware.cpu.CpuInformation;
import at.amartinz.hardware.cpu.CpuInformationListener;
import at.amartinz.hardware.cpu.CpuReader;
import at.amartinz.hardware.device.Device;
import at.amartinz.hardware.device.ProcessorInfo;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import hugo.weaving.DebugLog;

public class DeviceInfoCpuFragment extends MaterialSupportPreferenceFragment {
    private MaterialPreferenceCategory catProcessor;
    private MaterialPreferenceCategory catCpu;

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_dev_cpu;
    }

    @Override @NonNull public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        catProcessor = (MaterialPreferenceCategory) view.findViewById(R.id.cat_processor);
        catCpu = (MaterialPreferenceCategory) view.findViewById(R.id.cat_cpu);

        return view;
    }

    @DebugLog @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupProcessor();
        setupCpu();
    }

    private void setupProcessor() {
        ProcessorInfo.feedWithInformation(new Device.ProcessorInfoListener() {
            @Override public void onProcessorInfoAvailable(@NonNull final ProcessorInfo processorInfo) {
                catProcessor.post(new Runnable() {
                    @Override public void run() {
                        final int bitResId = processorInfo.is64Bit ? R.string.bit_64 : R.string.bit_32;
                        addPreference(catProcessor, "cpu_bit", R.string.arch, getString(bitResId));

                        final String cpuAbi = getString(R.string.cpu_abi);
                        final List<String> abis = processorInfo.abisAsList();
                        final int length = abis.size();
                        for (int i = 0; i < length; i++) {
                            String abi = "cpu_abi";
                            String title = cpuAbi;
                            if (i != 0) {
                                abi = String.format("cpu_abi%s", i + 1);
                                title += String.valueOf(i + 1);
                            }
                            addPreference(catProcessor, abi, title, abis.get(i));
                        }

                        addPreference(catProcessor, "cpu_hardware", R.string.hardware, processorInfo.hardware);
                        addPreference(catProcessor, "cpu_processor", R.string.processor, processorInfo.processor);
                        addPreference(catProcessor, "cpu_features", R.string.features, processorInfo.features);
                        addPreference(catProcessor, "cpu_bogomips", R.string.bogomips, processorInfo.bogomips);
                        catProcessor.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void setupCpu() {
        CpuReader.getCpuInformation(new CpuInformationListener() {
            @Override public void onCpuInformation(@NonNull final CpuInformation cpuInformation) {
                catCpu.post(new Runnable() {
                    @Override public void run() {
                        addPreference(catCpu, "cpu_core_count", R.string.cores, Integer.toString(cpuInformation.coreCount));
                        addPreference(catCpu, "cpu_freq_avail", R.string.frequency_available,
                                CpuInformation.listFrequenciesFormatted(cpuInformation.freqAvail));
                        addPreference(catCpu, "cpu_freq_min_max", R.string.clock_speed, String.format("%s - %s",
                                cpuInformation.freqAsMhz(cpuInformation.freqMin),
                                cpuInformation.freqAsMhz(cpuInformation.freqMax)));
                    }
                });
            }
        });
    }

    private MaterialPreference addPreference(MaterialPreferenceCategory category, String key, int titleResId, String summary) {
        return addPreference(category, key, getString(titleResId), summary);
    }

    private MaterialPreference addPreference(MaterialPreferenceCategory category, String key, String title, String summary) {
        final Context context = getActivity();
        final MaterialPreference preference = new MaterialPreference(context);
        preference.init(context);
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(TextUtils.isEmpty(summary) ? getString(R.string.unknown) : summary);
        category.addPreference(preference);
        return preference;
    }

}
