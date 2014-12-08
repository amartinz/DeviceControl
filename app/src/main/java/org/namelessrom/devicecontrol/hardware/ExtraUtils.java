package org.namelessrom.devicecontrol.hardware;

public class ExtraUtils {
    public static final String ENTROPY_AVAIL = "/proc/sys/kernel/random/entropy_avail";

    private static ExtraUtils sInstance;

    private ExtraUtils() { }

    public static ExtraUtils get() {
        if (sInstance == null) {
            sInstance = new ExtraUtils();
        }
        return sInstance;
    }

}
