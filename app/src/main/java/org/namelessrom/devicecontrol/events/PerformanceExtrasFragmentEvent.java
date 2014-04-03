package org.namelessrom.devicecontrol.events;

public class PerformanceExtrasFragmentEvent {

    private final boolean mIsForceHighEndGfx;
    private final boolean mIsMpDecisionRunning;

    public PerformanceExtrasFragmentEvent(final boolean isForceHighEndGfx,
            final boolean isMpDecisionRunning) {
        mIsForceHighEndGfx = isForceHighEndGfx;
        mIsMpDecisionRunning = isMpDecisionRunning;
    }

    public boolean isForceHighEndGfx() { return mIsForceHighEndGfx; }

    public boolean isMpDecisionRunning() { return mIsMpDecisionRunning; }

}
