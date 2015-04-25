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

    private static final String MEMTOTAL = "MemTotal:";
    private static final String MEMCACHED = "Cached:";
    private static final String MEMFREE = "MemFree:";

    public int type;
    public long memoryTotal;
    public long memoryCached;
    public long memoryFree; // TODO: rework as android calculates free memory differently

    public MemoryInfo() { }

    @Override public String toString() {
        return String.format("memoryTotal: %s, memoryFree: %s, memoryCached: %s",
                memoryTotal, memoryFree, memoryCached);
    }

    public long[] feedWithInformation(int type) {
        return readMemory(type);
    }

    public long[] readMemory(final int type) {
        this.type = type;

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

    public static String getAsMb(final long data) {
        return String.format("%s MB", data);
    }

    private long checkMemory(String s) {
        if (s != null && !s.isEmpty()) {
            s = s.replace("kB", "");

            if (s.startsWith(MEMTOTAL)) {
                s = s.replace(MEMTOTAL, "").trim();
                memoryTotal = parseLong(s);
                return memoryTotal;
            } else if (s.startsWith(MEMCACHED)) {
                s = s.replace(MEMCACHED, "").trim();
                memoryCached = parseLong(s);
                return memoryCached;
            } else if (s.startsWith(MEMFREE)) {
                s = s.replace(MEMFREE, "").trim();
                memoryFree = parseLong(s);
                return memoryFree;
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
