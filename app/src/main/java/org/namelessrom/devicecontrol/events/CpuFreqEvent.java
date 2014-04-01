package org.namelessrom.devicecontrol.events;

public class CpuFreqEvent {

    private final String[] mCpuFreqAvail;
    private final String   mCpuFreqMax;
    private final String   mCpuFreqMin;

    public CpuFreqEvent(final String[] avail, final String max, final String min) {
        mCpuFreqAvail = avail;
        mCpuFreqMax = max;
        mCpuFreqMin = min;
    }

    public String[] getCpuFreqAvail() { return mCpuFreqAvail; }

    public String getCpuFreqMax() { return mCpuFreqMax; }

    public String getCpuFreqMin() { return mCpuFreqMin; }

}
