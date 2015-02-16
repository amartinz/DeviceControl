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
package org.namelessrom.devicecontrol.device;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Device;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.Emmc;
import org.namelessrom.devicecontrol.objects.CpuInfo;
import org.namelessrom.devicecontrol.objects.KernelInfo;
import org.namelessrom.devicecontrol.objects.MemoryInfo;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class DeviceInformationGeneralFragment extends PreferenceFragment implements DeviceConstants {

    private static final String KEY_PLATFORM_VERSION = "platform_version";
    private static final String KEY_ANDROID_ID = "android_id";

    private long[] mHits = new long[3];
    private boolean mEasterEggStarted = false;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override public void onResume() {
        super.onResume();
        mEasterEggStarted = false;
    }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.device_information_general);

        final Device device = Device.get();

        // Platform
        PreferenceCategory category = (PreferenceCategory) findPreference("platform");

        addPreference(category, KEY_PLATFORM_VERSION, R.string.version, device.platformVersion)
                .setSelectable(true); // selectable because of the easter egg
        addPreference(category, "platform_id", R.string.build_id, device.platformId);
        addPreference(category, "platform_type", R.string.type, device.platformType);
        addPreference(category, "platform_tags", R.string.tags, device.platformTags);
        addPreference(category, "platform_build_date", R.string.build_date,
                device.platformBuildType);

        // Runtime
        category = (PreferenceCategory) findPreference("runtime");

        addPreference(category, "vm_library", R.string.type, device.vmLibrary);
        addPreference(category, "vm_version", R.string.version, device.vmVersion);

        // Device
        category = (PreferenceCategory) findPreference("device_information");

        // TODO: save / restore / check --> ANDROID ID
        addPreference(category, KEY_ANDROID_ID, R.string.android_id, device.androidId);
        addPreference(category, "device_manufacturer", R.string.manufacturer, device.manufacturer);
        addPreference(category, "device_model", R.string.model, device.model);
        addPreference(category, "device_product", R.string.product, device.product);
        addPreference(category, "device_board", R.string.board, device.board);
        addPreference(category, "device_bootloader", R.string.bootloader, device.bootloader);
        addPreference(category, "device_radio_version", R.string.radio_version, device.radio);

        // eMMC
        category = (PreferenceCategory) findPreference("emmc");
        addPreference(category, "emmc_name", R.string.name, Emmc.get().getName());
        addPreference(category, "emmc_cid", R.string.emmc_cid, Emmc.get().getCid());
        addPreference(category, "emmc_mid", R.string.emmc_mid, Emmc.get().getMid());
        addPreference(category, "emmc_rev", R.string.emmc_rev, Emmc.get().getRev());
        addPreference(category, "emmc_date", R.string.emmc_date, Emmc.get().getDate());
        String tmp = Emmc.get().canBrick()
                ? getString(R.string.emmc_can_brick_true)
                : getString(R.string.emmc_can_brick_false);
        tmp = String.format("%s\n%s", tmp, getString(R.string.press_learn_more));
        addPreference(category, "emmc_can_brick", R.string.emmc_can_brick, tmp).setSelectable(true);

        // Processor
        category = (PreferenceCategory) findPreference("processor");

        final String cpuAbi = getString(R.string.cpu_abi);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final int bitResId = Build.SUPPORTED_64_BIT_ABIS.length == 0
                    ? R.string.bit_32 : R.string.bit_64;
            addPreference(category, "cpu_bit", R.string.arch, getString(bitResId));
            for (int i = 0; i < Build.SUPPORTED_ABIS.length; i++) {
                addPreference(category, String.format("cpu_abi%s", i + 1),
                        cpuAbi + String.valueOf(i + 1), Build.SUPPORTED_ABIS[i]);
            }
        } else {
            addPreference(category, "cpu_abi", cpuAbi, Build.CPU_ABI);
            addPreference(category, "cpu_abi2", cpuAbi + "2", Build.CPU_ABI2);
        }
        new CpuInfoTask(category).execute();

        // Kernel
        category = (PreferenceCategory) findPreference("kernel");
        new KernelInfoTask(category).execute();

        // Memory
        category = (PreferenceCategory) findPreference("memory");
        new MemoryInfoTask(category).execute();

        if (category.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(category);
        }
    }

    private CustomPreference addPreference(final PreferenceCategory category, final String key,
            final int titleResId, final String summary) {
        return addPreference(category, key, getString(titleResId), summary);
    }

    private CustomPreference addPreference(final PreferenceCategory category, final String key,
            final String title, final String summary) {
        final CustomPreference preference = new CustomPreference(getActivity());
        preference.setKey(key);
        preference.setTitle(title);
        preference.setSummary(TextUtils.isEmpty(summary) ? getString(R.string.unknown) : summary);
        category.addPreference(preference);
        return preference;
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        final String key = preference.getKey();
        if (!mEasterEggStarted && TextUtils.equals(KEY_PLATFORM_VERSION, key)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                Utils.runRootCommand("am start android/com.android.internal.app.PlatLogoActivity");
                mEasterEggStarted = true;
            }
            return true;
        } else if (TextUtils.equals("emmc_can_brick", key)) {
            AppHelper.viewInBrowser(Emmc.BRICK_INFO_URL);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class CpuInfoTask extends AsyncTask<Void, Void, Boolean> {
        private final CpuInfo cpuInfo;
        private final PreferenceCategory category;

        public CpuInfoTask(final PreferenceCategory category) {
            this.cpuInfo = new CpuInfo();
            this.category = category;
        }

        @Override protected Boolean doInBackground(Void... voids) {
            return cpuInfo.feedWithInformation();
        }

        @Override protected void onPostExecute(final Boolean success) {
            if (success && category != null) {
                Logger.i(this, cpuInfo.toString());
                addPreference(category, "cpu_hardware", R.string.hardware, cpuInfo.hardware);
                addPreference(category, "cpu_processor", R.string.processor, cpuInfo.processor);
                addPreference(category, "cpu_features", R.string.features, cpuInfo.features);
                addPreference(category, "cpu_bogomips", R.string.bogomips, cpuInfo.bogomips);
            }
        }
    }

    private class KernelInfoTask extends AsyncTask<Void, Void, Boolean> {
        private final KernelInfo kernelInfo;
        private final PreferenceCategory category;

        public KernelInfoTask(final PreferenceCategory category) {
            this.kernelInfo = new KernelInfo();
            this.category = category;
        }

        @Override protected Boolean doInBackground(Void... voids) {
            return kernelInfo.feedWithInformation();
        }

        @Override protected void onPostExecute(final Boolean success) {
            if (success && category != null) {
                Logger.i(this, kernelInfo.toString());
                addPreference(category, "kernel_version", R.string.version,
                        String.format("%s %s", kernelInfo.version, kernelInfo.revision));
                addPreference(category, "kernel_extras", R.string.extras, kernelInfo.extras);
                addPreference(category, "kernel_gcc", R.string.toolchain, kernelInfo.gcc);
                addPreference(category, "kernel_date", R.string.build_date, kernelInfo.date);
                addPreference(category, "kernel_host", R.string.host, kernelInfo.host);
            }
        }
    }

    private class MemoryInfoTask extends AsyncTask<Void, Void, long[]> {
        private final PreferenceCategory category;

        public MemoryInfoTask(final PreferenceCategory category) {
            this.category = category;
        }

        @Override protected long[] doInBackground(Void... voids) {
            // TODO: configurable?
            return MemoryInfo.getInstance().readMemory(MemoryInfo.TYPE_MB);
        }

        @Override protected void onPostExecute(final long[] result) {
            if (result.length == 3 && category != null) {
                Logger.i(this, MemoryInfo.getInstance().toString());
                addPreference(category, "memory_total", R.string.total, get(result[0]));
                addPreference(category, "memory_free", R.string.free, get(result[1]));
                addPreference(category, "memory_cached", R.string.cached, get(result[2]));
            }
        }

        private String get(final long data) {
            return String.format("%s MB", data);
        }
    }

}
