package org.namelessrom.devicecontrol.utils.monitors;

import org.namelessrom.devicecontrol.utils.Utils;

import hugo.weaving.DebugLog;

/**
 * Provides information about the device's memory
 */
public class MemoryInfo {

    private static MemoryInfo sInstance;

    private static long memoryTotal;
    private static long memoryFree;
    private static long memoryCached;

    private MemoryInfo() { }

    public static MemoryInfo getInstance() {
        if (sInstance == null) {
            sInstance = new MemoryInfo();
        }
        return sInstance;
    }

    @DebugLog public long[] readMemory() {
        final String input = Utils.readFile("/proc/meminfo");
        if (input != null && !input.isEmpty()) {
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

        return new long[]{memoryTotal, memoryFree, memoryCached};
    }

    @DebugLog private long checkMemory(String s) {
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
