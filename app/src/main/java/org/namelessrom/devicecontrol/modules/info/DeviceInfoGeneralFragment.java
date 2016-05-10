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
package org.namelessrom.devicecontrol.modules.info;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import at.amartinz.hardware.Emmc;
import org.namelessrom.devicecontrol.preferences.CustomPreferenceCategoryMaterial;

import at.amartinz.execution.RootShell;
import at.amartinz.hardware.device.Device;
import at.amartinz.hardware.device.KernelInfo;
import at.amartinz.hardware.device.MemoryInfo;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class DeviceInfoGeneralFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener {
    private long[] mHits = new long[3];
    private boolean mEasterEggStarted = false;

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_info_dev_general;
    }

    public DeviceInfoGeneralFragment() { }

    @Override public void onResume() {
        super.onResume();
        mEasterEggStarted = false;
    }

    @NonNull @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = super.onCreateView(inflater, container, savedInstanceState);

        final CustomPreferenceCategoryMaterial kernelCategory =
                (CustomPreferenceCategoryMaterial) v.findViewById(R.id.cat_kernel);
        KernelInfo.feedWithInformation(new Device.KernelInfoListener() {
            @Override public void onKernelInfoAvailable(@NonNull final KernelInfo kernelInfo) {
                kernelCategory.post(new Runnable() {
                    @Override public void run() {
                        addPreference(kernelCategory, "kernel_version", R.string.version,
                                String.format("%s %s", kernelInfo.version, kernelInfo.revision));
                        addPreference(kernelCategory, "kernel_extras", R.string.extras, kernelInfo.extras);
                        addPreference(kernelCategory, "kernel_gcc", R.string.toolchain, kernelInfo.toolchain);
                        addPreference(kernelCategory, "kernel_date", R.string.build_date, kernelInfo.date);
                        addPreference(kernelCategory, "kernel_host", R.string.host, kernelInfo.host);
                    }
                });
            }
        });

        final CustomPreferenceCategoryMaterial deviceCategory =
                (CustomPreferenceCategoryMaterial) v.findViewById(R.id.cat_device_information);
        MemoryInfo.feedWithInformation(MemoryInfo.TYPE_MB, new Device.MemoryInfoListener() {
            @Override public void onMemoryInfoAvailable(@NonNull final MemoryInfo memInfo) {
                final Device device = Device.get(getActivity());
                deviceCategory.post(new Runnable() {
                    @Override public void run() {
                        // TODO: save / restore / check --> ANDROID ID
                        addPreference(deviceCategory, "android_id", R.string.android_id, device.androidId);
                        addPreference(deviceCategory, "device_manufacturer", R.string.manufacturer, device.manufacturer);
                        addPreference(deviceCategory, "device_device", R.string.device, device.device);
                        addPreference(deviceCategory, "device_model", R.string.model, device.model);
                        addPreference(deviceCategory, "device_product", R.string.product, device.product);
                        addPreference(deviceCategory, "device_board", R.string.board, device.board);
                        addPreference(deviceCategory, "device_bootloader", R.string.bootloader, device.bootloader);
                        addPreference(deviceCategory, "device_radio_version", R.string.radio_version, device.radio);
                        addPreference(deviceCategory, "device_selinux", R.string.selinux, device.isSELinuxEnforcing
                                ? getString(R.string.selinux_enforcing) : getString(R.string.selinux_permissive));
                        addPreference(deviceCategory, "memory_total", R.string.memory_total, MemoryInfo.getAsMb(memInfo.total));
                    }
                });
            }
        });

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Device device = Device.get(getActivity());

        // Platform
        CustomPreferenceCategoryMaterial category = (CustomPreferenceCategoryMaterial) view.findViewById(R.id.cat_platform);
        addPreference(category, "platform_version", R.string.version, device.platformVersion).setOnPreferenceClickListener(this);
        addPreference(category, "platform_id", R.string.build_id, device.platformId);
        addPreference(category, "platform_type", R.string.type, device.platformType);
        addPreference(category, "platform_tags", R.string.tags, device.platformTags);
        addPreference(category, "platform_build_date", R.string.build_date, device.platformBuildType);

        // Runtime
        category = (CustomPreferenceCategoryMaterial) view.findViewById(R.id.cat_runtime);
        addPreference(category, "vm_library", R.string.type, device.vmLibrary);
        addPreference(category, "vm_version", R.string.version, device.vmVersion);

        // eMMC
        category = (CustomPreferenceCategoryMaterial) view.findViewById(R.id.cat_emmc);
        addPreference(category, "emmc_name", R.string.name, Emmc.get().getName());
        addPreference(category, "emmc_cid", R.string.emmc_cid, Emmc.get().getCid());
        addPreference(category, "emmc_mid", R.string.emmc_mid, Emmc.get().getMid());
        addPreference(category, "emmc_rev", R.string.emmc_rev, Emmc.get().getRev());
        addPreference(category, "emmc_date", R.string.emmc_date, Emmc.get().getDate());
        String tmp = Emmc.get().canBrick() ? getString(R.string.emmc_can_brick_true) : getString(R.string.emmc_can_brick_false);
        tmp = tmp + '\n' + getString(R.string.press_learn_more);
        addPreference(category, "emmc_can_brick", R.string.emmc_can_brick, tmp).setOnPreferenceClickListener(this);
    }

    private MaterialPreference addPreference(final CustomPreferenceCategoryMaterial category,
            final String key, final int titleResId, final String summary) {
        return addPreference(category, key, getString(titleResId), summary);
    }

    private MaterialPreference addPreference(final CustomPreferenceCategoryMaterial category,
            final String key, final String title, final String summary) {
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
                RootShell.fireAndForget("am start android/com.android.internal.app.PlatLogoActivity");
                mEasterEggStarted = true;
            }
            return true;
        } else if ("emmc_can_brick".equals(key)) {
            final Activity activity = getActivity();
            ((App) activity.getApplicationContext()).getCustomTabsHelper().launchUrl(activity, Emmc.BRICK_INFO_URL);
            return true;
        }

        return false;
    }
}
