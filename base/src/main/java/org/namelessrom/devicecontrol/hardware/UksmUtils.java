package org.namelessrom.devicecontrol.hardware;

import android.support.annotation.Nullable;

import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Created by proDOOMman on 11.11.14.
 */
public class UksmUtils {
    public static final String UKSM_PATH = "/sys/kernel/mm/uksm/";
    public static final String UKSM_RUN = UKSM_PATH + "run";                  // rw
    public static final String UKSM_SLEEP = UKSM_PATH + "sleep_millisecs";      // rw
    public static final String UKSM_SLEEP_TIMES = UKSM_PATH + "sleep_times";          // ro
    public static final String UKSM_CPU_GOV = UKSM_PATH + "cpu_governor";         // rw
    public static final String UKSM_CPU_MAX = UKSM_PATH + "max_cpu_percentage";   // rw
    public static final String UKSM_FULL_SCANS = UKSM_PATH + "full_scans";           // ro
    public static final String UKSM_HASH_STRENGTH = UKSM_PATH + "hash_strength";        // ro
    public static final String UKSM_PAGES_SCANNED = UKSM_PATH + "pages_scanned";        // ro
    public static final String UKSM_PAGES_SHARED = UKSM_PATH + "pages_shared";         // ro
    public static final String UKSM_PAGES_SHARING = UKSM_PATH + "pages_sharing";        // ro

    private static UksmUtils sInstance;

    private UksmUtils() {}

    public static UksmUtils get() {
        if (sInstance == null) {
            sInstance = new UksmUtils();
        }
        return sInstance;
    }

    /**
     * Gets available UKSM cpu governors from file
     *
     * @return available UKSM cpu governors
     */
    @Nullable public String[] getAvailableCpuGovernors() {
        String[] schedulers = null;
        final String[] aux = Utils.readStringArray(UKSM_CPU_GOV);
        if (aux != null) {
            schedulers = new String[aux.length];
            for (int i = 0; i < aux.length; i++) {
                if (aux[i].charAt(0) == '[') {
                    schedulers[i] = aux[i].substring(1, aux[i].length() - 1);
                } else {
                    schedulers[i] = aux[i];
                }
            }
        }
        return schedulers;
    }

    /**
     * Gets available schedulers from file
     *
     * @return available schedulers
     */
    public String getCurrentCpuGovernor() {
        final String[] aux = Utils.readStringArray(UKSM_CPU_GOV);
        if (aux != null) {
            for (final String anAux : aux) {
                if (anAux.charAt(0) == '[') {
                    return anAux.substring(1, anAux.length() - 1);
                }
            }
        }
        return "full";
    }
}
