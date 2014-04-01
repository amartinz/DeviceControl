package org.namelessrom.devicecontrol.events;

public class GovernorEvent {

    private final String[] mAvailableGovernors;
    private final String   mCurrentGovernor;

    public GovernorEvent(final String[] availableGovernors, final String governor) {
        mAvailableGovernors = availableGovernors;
        mCurrentGovernor = governor;
    }

    public String[] getAvailableGovernors() { return mAvailableGovernors; }

    public String getCurrentGovernor() { return mCurrentGovernor; }

}
