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
package org.namelessrom.devicecontrol.objects;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Provides information about the device's memory
 */
public class MemoryInfo {

    public static final int TYPE_B = 0;
    public static final int TYPE_KB = 1;
    public static final int TYPE_MB = 2;

    private static MemoryInfo sInstance;

    public static long memoryTotal;
    public static long memoryFree;
    public static long memoryCached;

    private MemoryInfo() { }

    public static MemoryInfo getInstance() {
        if (sInstance == null) {
            sInstance = new MemoryInfo();
        }
        return sInstance;
    }

    @Override public String toString() {
        return String.format("memoryTotal: %s, memoryFree: %s, memoryCached: %s",
                memoryTotal, memoryFree, memoryCached);
    }

    public long[] readMemory() {
        return readMemory(TYPE_B);
    }

    public long[] readMemory(final int type) {
        final String input = Utils.readFile("/proc/meminfo");
        if (!TextUtils.isEmpty(input)) {
            final String[] parts = input.split("\n");
            for (final String s : parts) checkMemory(s);
        } else {
            memoryTotal = 0;
            memoryFree = 0;
            memoryCached = 0;
        }

        // Ensure we dont get garbage
        if (memoryTotal < 0) memoryTotal = 0;
        if (memoryFree < 0) memoryFree = 0;
        if (memoryCached < 0) memoryCached = 0;

        // default is kb
        switch (type) {
            default:
            case TYPE_KB:
                break;
            case TYPE_B:
                memoryTotal = memoryTotal * 1024;
                memoryFree = memoryFree * 1024;
                memoryCached = memoryCached * 1024;
                break;
            case TYPE_MB:
                memoryTotal = memoryTotal / 1024;
                memoryFree = memoryFree / 1024;
                memoryCached = memoryCached / 1024;
                break;
        }

        return new long[]{ memoryTotal, memoryFree, memoryCached };
    }

    private long checkMemory(String s) {
        if (s != null && !s.isEmpty()) {
            s = s.replace("kB", "");

            if (s.startsWith("MemTotal:")) {
                s = s.replace("MemTotal:", "").trim();
                memoryTotal = parseLong(s);
                return memoryTotal;
            } else if (s.startsWith("MemFree:")) {
                s = s.replace("MemFree:", "").trim();
                memoryFree = parseLong(s);
                return memoryFree;
            } else if (s.startsWith("Cached:")) {
                s = s.replace("Cached:", "").trim();
                memoryCached = parseLong(s);
                return memoryCached;
            }
        }

        return -1;
    }

    private long parseLong(final String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception exc) {
            return -1;
        }
    }

}
