package org.namelessrom.devicecontrol.events;

public class DeviceFragmentEvent {

    private final boolean mIsForceNavBar;

    public DeviceFragmentEvent(final boolean isForceNavBar) { mIsForceNavBar = isForceNavBar; }

    public boolean isForceNavBar() { return mIsForceNavBar; }

}
