package org.namelessrom.devicecontrol.sample;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

/**
 * A Busprovider for sharing a single Bus, making sure we are only registering and posting to the
 * same Bus
 */
public class BusProvider {

    private static final Bus sBus = new Bus(ThreadEnforcer.MAIN);

    private BusProvider() { /* ignored */ }

    public static Bus getBus() { return sBus; }

}
