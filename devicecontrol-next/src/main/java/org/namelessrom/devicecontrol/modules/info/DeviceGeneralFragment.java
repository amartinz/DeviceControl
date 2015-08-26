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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

import alexander.martinz.libs.execution.Command;
import alexander.martinz.libs.execution.RootShell;
import alexander.martinz.libs.execution.ShellManager;
import alexander.martinz.libs.hardware.device.Device;
import alexander.martinz.libs.hardware.device.EmmcInfo;
import alexander.martinz.libs.hardware.device.KernelInfo;
import alexander.martinz.libs.hardware.device.MemoryInfo;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class DeviceGeneralFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener {
    private long[] mHits = new long[3];
    private boolean mEasterEggStarted = false;

    private MaterialPreferenceCategory catPlatform;
    private MaterialPreferenceCategory catRuntime;
    private MaterialPreferenceCategory catDevice;
    private MaterialPreferenceCategory catMemory;
    private MaterialPreferenceCategory catKernel;
    private MaterialPreferenceCategory catEmmc;


    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_general;
    }

    public DeviceGeneralFragment() { }

    @Override public void onResume() {
        super.onResume();
        mEasterEggStarted = false;

        final Device device = Device.get(getActivity());

        setupPlatform(device);
        setupRuntime(device);
        setupDevice(device);

        setupMemory();
        setupKernel();
        setupEmmc();
    }

    @Override @NonNull public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        catPlatform = (MaterialPreferenceCategory) view.findViewById(R.id.cat_platform);
        catRuntime = (MaterialPreferenceCategory) view.findViewById(R.id.cat_runtime);
        catDevice = (MaterialPreferenceCategory) view.findViewById(R.id.cat_device_information);

        catMemory = (MaterialPreferenceCategory) view.findViewById(R.id.cat_memory);
        catKernel = (MaterialPreferenceCategory) view.findViewById(R.id.cat_kernel);
        catEmmc = (MaterialPreferenceCategory) view.findViewById(R.id.cat_emmc);

        return view;
    }

    private void setupPlatform(@NonNull final Device device) {
        addPreference(catPlatform, "platform_version", R.string.version, device.platformVersion)
                .setOnPreferenceClickListener(this);
        addPreference(catPlatform, "platform_id", R.string.build_id, device.platformId);
        addPreference(catPlatform, "platform_type", R.string.type, device.platformType);
        addPreference(catPlatform, "platform_tags", R.string.tags, device.platformTags);
        addPreference(catPlatform, "platform_build_date", R.string.build_date, device.platformBuildType);
        catPlatform.setVisibility(View.VISIBLE);
    }

    private void setupRuntime(@NonNull final Device device) {
        addPreference(catRuntime, "vm_library", R.string.type, device.vmLibrary);
        addPreference(catRuntime, "vm_version", R.string.version, device.vmVersion);
        catRuntime.setVisibility(View.VISIBLE);
    }

    private void setupDevice(@NonNull final Device device) {
        // TODO: save / restore / check --> ANDROID ID
        addPreference(catDevice, "android_id", R.string.android_id, device.androidId);
        addPreference(catDevice, "device_manufacturer", R.string.manufacturer, device.manufacturer);
        addPreference(catDevice, "device_device", R.string.device, device.device);
        addPreference(catDevice, "device_model", R.string.model, device.model);
        addPreference(catDevice, "device_product", R.string.product, device.product);
        addPreference(catDevice, "device_board", R.string.board, device.board);
        addPreference(catDevice, "device_bootloader", R.string.bootloader, device.bootloader);
        addPreference(catDevice, "device_radio_version", R.string.radio_version, device.radio);
        addPreference(catDevice, "device_selinux", R.string.selinux, device.isSELinuxEnforcing
                ? getString(R.string.selinux_enforcing) : getString(R.string.selinux_permissive));
        catDevice.setVisibility(View.VISIBLE);
    }

    private void setupMemory() {
        MemoryInfo.feedWithInformation(getActivity(), MemoryInfo.TYPE_MB, new Device.MemoryInfoListener() {
            @Override public void onMemoryInfoAvailable(@NonNull final MemoryInfo memoryInfo) {
                catMemory.post(new Runnable() {
                    @Override public void run() {
                        addPreference(catMemory, "memory_total", R.string.total, MemoryInfo.getAsMb(memoryInfo.total));
                        addPreference(catMemory, "memory_cached", R.string.cached, MemoryInfo.getAsMb(memoryInfo.cached));
                        addPreference(catMemory, "memory_free", R.string.free, MemoryInfo.getAsMb(memoryInfo.free));
                        catMemory.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void setupKernel() {
        KernelInfo.feedWithInformation(getActivity(), new Device.KernelInfoListener() {
            @Override public void onKernelInfoAvailable(@NonNull final KernelInfo kernelInfo) {
                catKernel.post(new Runnable() {
                    @Override public void run() {
                        addPreference(catKernel, "kernel_version", R.string.version,
                                String.format("%s %s", kernelInfo.version, kernelInfo.revision));
                        addPreference(catKernel, "kernel_extras", R.string.extras, kernelInfo.extras);
                        addPreference(catKernel, "kernel_gcc", R.string.toolchain, kernelInfo.toolchain);
                        addPreference(catKernel, "kernel_date", R.string.build_date, kernelInfo.date);
                        addPreference(catKernel, "kernel_host", R.string.host, kernelInfo.host);
                        catKernel.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }

    private void setupEmmc() {
        EmmcInfo.feedWithInformation(getActivity(), new Device.EmmcInfoListener() {
            @Override public void onEmmcInfoAvailable(@NonNull final EmmcInfo emmcInfo) {
                catEmmc.post(new Runnable() {
                    @Override public void run() {
                        addPreference(catEmmc, "emmc_name", R.string.name, emmcInfo.name);
                        addPreference(catEmmc, "emmc_cid", R.string.emmc_cid, emmcInfo.cid);
                        addPreference(catEmmc, "emmc_mid", R.string.emmc_mid, emmcInfo.mid);
                        addPreference(catEmmc, "emmc_rev", R.string.emmc_rev, emmcInfo.rev);
                        addPreference(catEmmc, "emmc_date", R.string.emmc_date, emmcInfo.date);

                        String tmp = emmcInfo.canBrick()
                                ? getString(R.string.emmc_can_brick_true) : getString(R.string.emmc_can_brick_false);
                        tmp = String.format("%s\n%s", tmp, getString(R.string.press_learn_more));
                        addPreference(catEmmc, "emmc_can_brick", R.string.emmc_can_brick, tmp)
                                .setOnPreferenceClickListener(DeviceGeneralFragment.this);
                        catEmmc.setVisibility(View.VISIBLE);
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
