package org.namelessrom.devicecontrol.events;

public class FreezeEvent {

    private final String mPackages;

    public FreezeEvent(final String packages) {
        mPackages = packages;
    }

    public String getPackages() {
        return mPackages;
    }

}
