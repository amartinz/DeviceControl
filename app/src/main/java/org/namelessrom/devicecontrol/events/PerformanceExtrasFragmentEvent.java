package org.namelessrom.devicecontrol.events;

public class PerformanceExtrasFragmentEvent {

    private final boolean mIsForceHighEndGfx;

    public PerformanceExtrasFragmentEvent(final boolean isForceHighEndGfx) {
        mIsForceHighEndGfx = isForceHighEndGfx;
    }

    public boolean isForceHighEndGfx() { return mIsForceHighEndGfx; }

}
