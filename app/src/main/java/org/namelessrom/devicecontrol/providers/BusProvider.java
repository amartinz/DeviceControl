package org.namelessrom.devicecontrol.providers;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BusProvider {

    private static final Bus sBus = new Bus(ThreadEnforcer.MAIN);

    private BusProvider() { /* ignored */ }

    public static Bus getBus() {
        return sBus;
    }

}
