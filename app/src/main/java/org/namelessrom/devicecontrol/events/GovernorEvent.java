package org.namelessrom.devicecontrol.events;

public class GovernorEvent {

    public static String[] mAvailableGovernors;
    public static String   mCurrentGovernor;

    public GovernorEvent(final String[] availableGovernors, final String governor) {
        mAvailableGovernors = availableGovernors;
        mCurrentGovernor = governor;
    }

    public String[] getAvailableGovernors() { return mAvailableGovernors; }

    public String getCurrentGovernor() { return mCurrentGovernor; }

}
