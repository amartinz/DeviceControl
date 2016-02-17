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
package org.namelessrom.devicecontrol.hardware;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import timber.log.Timber;

public class GpuUtils {
    private static String gpuBasePath = null;
    private static String gpuGovPath = null;
    private static String gpuGovsAvailablePath = null;
    private static String gpuFreqsAvailPath = null;
    private static String gpuFreqMaxPath = null;
    private static String gpuFreqMinPath = null;

    public static final String FILE_3D_SCALING = "/sys/devices/gr3d/enable_3d_scaling";

    public static class Gpu {
        public final String[] available;
        public final String max;
        public final String min;
        public final String governor;

        public Gpu(final String[] availFreqs, final String maxFreq, final String minFreq, final String gov) {
            available = availFreqs;
            max = maxFreq;
            min = minFreq;
            governor = gov;
        }
    }

    private static GpuUtils sInstance;

    private GpuUtils() { }

    public static GpuUtils get() {
        if (sInstance == null) {
            sInstance = new GpuUtils();
        }
        return sInstance;
    }

    @Nullable public String getGpuBasePath() {
        if (gpuBasePath == null) {
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_base);
            for (final String s : paths) {
                if (Utils.fileExists(s)) {
                    gpuBasePath = s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuBasePath)) {
                return "";
            }
        }
        return gpuBasePath;
    }

    @Nullable public String getGpuGovPath() {
        if (gpuGovPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_gov_path);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuGovPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuGovPath)) {
                return "";
            }
        }
        return gpuGovPath;
    }

    @Nullable public String getGpuGovsAvailablePath() {
        if (gpuGovsAvailablePath == null) {
            final String base = getGpuBasePath();
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_govs_avail_path);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuGovsAvailablePath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuGovsAvailablePath)) {
                return null;
            }
        }
        return gpuGovsAvailablePath;
    }

    @Nullable public String getGpuFreqsAvailPath() {
        if (gpuFreqsAvailPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_freqs_avail);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqsAvailPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqsAvailPath)) {
                return null;
            }
        }
        return gpuFreqsAvailPath;
    }

    @Nullable public String getGpuFreqMaxPath() {
        if (gpuFreqMaxPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_freqs_max);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMaxPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMaxPath)) {
                return null;
            }
        }
        return gpuFreqMaxPath;
    }

    @Nullable public String getGpuFreqMinPath() {
        if (gpuFreqMinPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = App.get().getStringArray(R.array.hardware_gpu_freqs_min);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMinPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMinPath)) {
                return null;
            }
        }
        return gpuFreqMinPath;
    }

    @Nullable public String[] getAvailableFrequencies(final boolean sorted) {
        final String freqsRaw = Utils.readOneLine(getGpuFreqsAvailPath());
        if (freqsRaw != null && !freqsRaw.isEmpty()) {
            final String[] freqs = freqsRaw.split(" ");
            if (!sorted) {
                return freqs;
            }
            Arrays.sort(freqs, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Utils.tryValueOf(object1, 0).compareTo(Utils.tryValueOf(object2, 0));
                }
            });
            Collections.reverse(Arrays.asList(freqs));
            return freqs;
        }
        return null;
    }

    @Nullable public String getMaxFreq() {
        return Utils.readOneLine(getGpuFreqMaxPath());
    }

    @Nullable public String getMinFreq() {
        return Utils.readOneLine(getGpuFreqMinPath());
    }

    @Nullable public String getGovernor() {
        return Utils.readOneLine(getGpuGovPath());
    }

    public Gpu getGpu() {
        return new GpuUtils.Gpu(
                GpuUtils.get().getAvailableFrequencies(true),
                GpuUtils.get().getMaxFreq(),
                GpuUtils.get().getMinFreq(),
                GpuUtils.get().getGovernor());
    }

    public boolean containsGov(final String gov) {
        final String[] governors = GovernorUtils.get().getAvailableGpuGovernors();
        if (governors == null) { return false; }
        for (final String s : governors) {
            if (gov.toLowerCase().equals(s.toLowerCase())) { return true; }
        }
        return false;
    }

    @NonNull public String restore(BootupConfig config) {
        final ArrayList<BootupItem> items = config.getItemsByCategory(BootupConfig.CATEGORY_GPU);
        if (items.size() == 0) {
            return "";
        }

        final StringBuilder sbCmd = new StringBuilder();
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            sbCmd.append(Utils.getWriteCommand(item.filename, item.value));
        }

        return sbCmd.toString();
    }

    @NonNull public static String toMhz(final String mhz) {
        int mhzInt;
        try {
            mhzInt = Utils.parseInt(mhz);
        } catch (Exception exc) {
            Timber.e(exc.getMessage());
            mhzInt = 0;
        }
        return (String.valueOf(mhzInt / 1000000) + " MHz");
    }

    @NonNull public static String fromMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            try {
                return String.valueOf(Utils.parseInt(mhzString.replace(" MHz", "")) * 1000000);
            } catch (Exception exc) {
                Timber.e(exc.getMessage());
            }
        }
        return "0";
    }

    @Nullable public static String[] freqsToMhz(final String[] frequencies) {
        if (frequencies == null) { return null; }
        final String[] names = new String[frequencies.length];

        for (int i = 0; i < frequencies.length; i++) {
            names[i] = toMhz(frequencies[i]);
        }

        return names;
    }

}
