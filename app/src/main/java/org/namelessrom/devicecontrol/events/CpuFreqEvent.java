package org.namelessrom.devicecontrol.events;

public class CpuFreqEvent {

    public static String[] mCpuFreqAvail;
    public static String   mCpuFreqMax;
    public static String   mCpuFreqMin;

    public CpuFreqEvent(final String[] avail, final String max, final String min) {
        mCpuFreqAvail = avail;
        mCpuFreqMax = max;
        mCpuFreqMin = min;
    }

    public String[] getCpuFreqAvail() { return mCpuFreqAvail; }

    public String getCpuFreqMax() { return mCpuFreqMax; }

    public String getCpuFreqMin() { return mCpuFreqMin; }

}
