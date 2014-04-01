package org.namelessrom.devicecontrol.events;

public class GpuEvent {

    private final String[] mAvailFreqs;
    private final String   mMaxFreq;
    private final String   mGov;

    public GpuEvent(final String[] availFreqs, final String maxFreq, final String gov) {
        mAvailFreqs = availFreqs;
        mMaxFreq = maxFreq;
        mGov = gov;
    }

    public String[] getAvailFreqs() { return mAvailFreqs; }

    public String getMaxFreq() { return mMaxFreq; }

    public String getGovernor() { return mGov; }

}
