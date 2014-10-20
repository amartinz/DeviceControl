package org.namelessrom.devicecontrol.hardware;

/**
 * Created by alex on 20.10.14.
 */
public class KsmUtils {
    public static final String KSM_PATH           = "/sys/kernel/mm/ksm/";
    public static final String KSM_SLEEP          = KSM_PATH + "sleep_millisecs";
    public static final String KSM_PAGES_VOLATILE = KSM_PATH + "pages_volatile";
    public static final String KSM_PAGES_UNSHARED = KSM_PATH + "pages_unshared";
    public static final String KSM_PAGES_TO_SCAN  = KSM_PATH + "pages_to_scan";
    public static final String KSM_PAGES_SHARING  = KSM_PATH + "pages_sharing";
    public static final String KSM_PAGES_SHARED   = KSM_PATH + "pages_shared";
    public static final String KSM_FULL_SCANS     = KSM_PATH + "full_scans";
}
