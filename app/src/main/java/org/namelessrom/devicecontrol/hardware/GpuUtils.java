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

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

        public Gpu(final String[] availFreqs, final String maxFreq, final String minFreq,
                final String gov) {
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

    public String getGpuBasePath() {
        if (gpuBasePath == null) {
            final String[] paths = Application.get().getStringArray(R.array.gpu_base);
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

    public String getGpuGovPath() {
        if (gpuGovPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_gov_path);
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

    public String getGpuGovsAvailablePath() {
        if (gpuGovsAvailablePath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_govs_avail_path);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuGovsAvailablePath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuGovsAvailablePath)) {
                return "";
            }
        }
        return gpuGovsAvailablePath;
    }

    public String getGpuFreqsAvailPath() {
        if (gpuFreqsAvailPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_avail);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqsAvailPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqsAvailPath)) {
                return "";
            }
        }
        return gpuFreqsAvailPath;
    }

    public String getGpuFreqMaxPath() {
        if (gpuFreqMaxPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_max);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMaxPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMaxPath)) {
                return "";
            }
        }
        return gpuFreqMaxPath;
    }

    public String getGpuFreqMinPath() {
        if (gpuFreqMinPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_min);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMinPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMinPath)) {
                return "";
            }
        }
        return gpuFreqMinPath;
    }

    public String[] getAvailableFrequencies(final boolean sorted) {
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

    public String getMaxFreq() {
        return Utils.readOneLine(getGpuFreqMaxPath());
    }

    public String getMinFreq() {
        return Utils.readOneLine(getGpuFreqMinPath());
    }

    public String getGovernor() {
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
        for (final String s : GovernorUtils.get().getAvailableGpuGovernors()) {
            if (gov.toLowerCase().equals(s.toLowerCase())) return true;
        }
        return false;
    }

    public String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = DatabaseHandler.getInstance().getAllItems(
                DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_GPU);
        for (final DataItem item : items) {
            sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
        }

        return sbCmd.toString();
    }

    public static String toMhz(final String mhz) {
        int mhzInt;
        try {
            mhzInt = Utils.parseInt(mhz);
        } catch (Exception exc) {
            Logger.e(GpuUtils.get(), exc.getMessage());
            mhzInt = 0;
        }
        return (String.valueOf(mhzInt / 1000000) + " MHz");
    }

    public static String fromMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            try {
                return String.valueOf(Utils.parseInt(mhzString.replace(" MHz", "")) * 1000000);
            } catch (Exception exc) {
                Logger.e(GpuUtils.get(), exc.getMessage());
            }
        }
        return "0";
    }

    public static String[] freqsToMhz(final String[] frequencies) {
        final int length = frequencies.length;
        final String[] names = new String[length];

        for (int i = 0; i < length; i++) {
            names[i] = toMhz(frequencies[i]);
        }

        return names;
    }

    public static boolean isOpenGLES20Supported() {
        final ActivityManager am = (ActivityManager)
                Application.get().getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info != null && info.reqGlEsVersion >= 0x20000);
    }

}
