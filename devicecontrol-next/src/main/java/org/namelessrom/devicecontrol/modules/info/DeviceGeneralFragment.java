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
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

import java.util.List;

import alexander.martinz.libs.execution.Command;
import alexander.martinz.libs.execution.RootShell;
import alexander.martinz.libs.execution.ShellManager;
import alexander.martinz.libs.hardware.device.Device;
import alexander.martinz.libs.hardware.device.EmmcInfo;
import alexander.martinz.libs.hardware.device.KernelInfo;
import alexander.martinz.libs.hardware.device.MemoryInfo;
import alexander.martinz.libs.hardware.device.ProcessorInfo;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class DeviceGeneralFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener {
    private long[] mHits = new long[3];
    private boolean mEasterEggStarted = false;

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_device_general;
    }

    public DeviceGeneralFragment() { }

    @Override public void onResume() {
        super.onResume();
        mEasterEggStarted = false;
    }

    @Override public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Device device = Device.get(getActivity());

        // Platform
        MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_platform);
        addPreference(category, "platform_version", R.string.version, device.platformVersion).setOnPreferenceClickListener(this);
        addPreference(category, "platform_id", R.string.build_id, device.platformId);
        addPreference(category, "platform_type", R.string.type, device.platformType);
        addPreference(category, "platform_tags", R.string.tags, device.platformTags);
        addPreference(category, "platform_build_date", R.string.build_date, device.platformBuildType);
        category.setVisibility(View.VISIBLE);

        // Runtime
        category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_runtime);
        addPreference(category, "vm_library", R.string.type, device.vmLibrary);
        addPreference(category, "vm_version", R.string.version, device.vmVersion);
        category.setVisibility(View.VISIBLE);

        // Device
        category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_device_information);
        // TODO: save / restore / check --> ANDROID ID
        addPreference(category, "android_id", R.string.android_id, device.androidId);
        addPreference(category, "device_manufacturer", R.string.manufacturer, device.manufacturer);
        addPreference(category, "device_device", R.string.device, device.device);
        addPreference(category, "device_model", R.string.model, device.model);
        addPreference(category, "device_product", R.string.product, device.product);
        addPreference(category, "device_board", R.string.board, device.board);
        addPreference(category, "device_bootloader", R.string.bootloader, device.bootloader);
        addPreference(category, "device_radio_version", R.string.radio_version, device.radio);
        addPreference(category, "device_selinux", R.string.selinux, device.isSELinuxEnforcing
                ? getString(R.string.selinux_enforcing) : getString(R.string.selinux_permissive));
        category.setVisibility(View.VISIBLE);

        // Memory
        MemoryInfo.feedWithInformation(getActivity(), MemoryInfo.TYPE_MB, new Device.MemoryInfoListener() {
            @Override public void onMemoryInfoAvailable(@NonNull final MemoryInfo memoryInfo) {
                view.post(new Runnable() {
                    @Override public void run() {
                        MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_memory);
                        addPreference(category, "memory_total", R.string.total, MemoryInfo.getAsMb(memoryInfo.total));
                        addPreference(category, "memory_cached", R.string.cached, MemoryInfo.getAsMb(memoryInfo.cached));
                        addPreference(category, "memory_free", R.string.free, MemoryInfo.getAsMb(memoryInfo.free));
                        category.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        // Processor
        ProcessorInfo.feedWithInformation(getActivity(), new Device.ProcessorInfoListener() {
            @Override public void onProcessorInfoAvailable(@NonNull final ProcessorInfo processorInfo) {
                view.post(new Runnable() {
                    @Override public void run() {
                        MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_processor);
                        final int bitResId = processorInfo.is64Bit ? R.string.bit_64 : R.string.bit_32;
                        addPreference(category, "cpu_bit", R.string.arch, getString(bitResId));

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
                            addPreference(category, abi, title, abis.get(i));
                        }

                        addPreference(category, "cpu_hardware", R.string.hardware, processorInfo.hardware);
                        addPreference(category, "cpu_processor", R.string.processor, processorInfo.processor);
                        addPreference(category, "cpu_features", R.string.features, processorInfo.features);
                        addPreference(category, "cpu_bogomips", R.string.bogomips, processorInfo.bogomips);
                        category.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        // Kernel
        KernelInfo.feedWithInformation(getActivity(), new Device.KernelInfoListener() {
            @Override public void onKernelInfoAvailable(@NonNull final KernelInfo kernelInfo) {
                view.post(new Runnable() {
                    @Override public void run() {
                        MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_kernel);
                        addPreference(category, "kernel_version", R.string.version,
                                String.format("%s %s", kernelInfo.version, kernelInfo.revision));
                        addPreference(category, "kernel_extras", R.string.extras, kernelInfo.extras);
                        addPreference(category, "kernel_gcc", R.string.toolchain, kernelInfo.toolchain);
                        addPreference(category, "kernel_date", R.string.build_date, kernelInfo.date);
                        addPreference(category, "kernel_host", R.string.host, kernelInfo.host);
                        category.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        // eMMC
        EmmcInfo.feedWithInformation(getActivity(), new Device.EmmcInfoListener() {
            @Override public void onEmmcInfoAvailable(@NonNull final EmmcInfo emmcInfo) {
                view.post(new Runnable() {
                    @Override public void run() {
                        MaterialPreferenceCategory category = (MaterialPreferenceCategory) view.findViewById(R.id.cat_emmc);
                        addPreference(category, "emmc_name", R.string.name, emmcInfo.name);
                        addPreference(category, "emmc_cid", R.string.emmc_cid, emmcInfo.cid);
                        addPreference(category, "emmc_mid", R.string.emmc_mid, emmcInfo.mid);
                        addPreference(category, "emmc_rev", R.string.emmc_rev, emmcInfo.rev);
                        addPreference(category, "emmc_date", R.string.emmc_date, emmcInfo.date);

                        String tmp = emmcInfo.canBrick()
                                ? getString(R.string.emmc_can_brick_true) : getString(R.string.emmc_can_brick_false);
                        tmp = String.format("%s\n%s", tmp, getString(R.string.press_learn_more));
                        addPreference(category, "emmc_can_brick", R.string.emmc_can_brick, tmp)
                                .setOnPreferenceClickListener(DeviceGeneralFragment.this);
                        category.setVisibility(View.VISIBLE);
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

    @Override public boolean onPreferenceClicked(MaterialPreference materialPreference) {
        final String key = materialPreference.getKey();
        if (!mEasterEggStarted && "platform_version".equals(key)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                RootShell shell = ShellManager.get(getActivity()).getRootShell();
                if (shell != null) {
                    shell.add(new Command("am start android/com.android.internal.app.PlatLogoActivity"));
                }
                mEasterEggStarted = true;
            }
            return true;
        } else if ("emmc_can_brick".equals(key)) {
            AppHelper.launchUrlViaTabs(getActivity(), EmmcInfo.BRICK_INFO_URL);
            return true;
        }

        return false;
    }
}
