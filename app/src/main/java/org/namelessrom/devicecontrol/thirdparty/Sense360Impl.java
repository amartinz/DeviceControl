package org.namelessrom.devicecontrol.thirdparty;

import android.content.Context;

import com.sense360.android.quinoa.lib.Sense360;

import timber.log.Timber;

public class Sense360Impl {
    public static void init(Context applicationContext) {
        try {
            if (!Sense360.isUserOptedOut(applicationContext)) {
                Timber.v("Starting Sense360");
                Sense360.start(applicationContext);
            } else {
                Timber.v("Not starting Sense360, user is opted out!");
            }
        } catch (NoSuchMethodError | Exception exc) {
            Timber.e(exc, "Could not start/stop Sense360");
        }
    }

    public static void optIn(Context applicationContext) {
        Timber.v("Opting in for Sense360");
        Sense360.userOptIn(applicationContext);
        Sense360.start(applicationContext);
    }

    public static void optOut(Context applicationContext) {
        Timber.v("Opting out from Sense360");
        Sense360.userOptOut(applicationContext);
    }
}
