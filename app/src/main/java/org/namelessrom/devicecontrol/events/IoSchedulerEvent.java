package org.namelessrom.devicecontrol.events;

public class IoSchedulerEvent {

    public static String[] mAvailableIoSchedulers;
    public static String   mCurrentIoScheduler;

    public IoSchedulerEvent(final String[] availableIoSchedulers, final String ioScheduler) {
        mAvailableIoSchedulers = availableIoSchedulers;
        mCurrentIoScheduler = ioScheduler;
    }

    public String[] getAvailableIoScheduler() {return mAvailableIoSchedulers;}

    public String getCurrentIoScheduler() { return mCurrentIoScheduler; }

}
