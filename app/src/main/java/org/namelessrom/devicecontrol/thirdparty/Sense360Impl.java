package org.namelessrom.devicecontrol.thirdparty;

import android.content.Context;

import com.sense360.android.quinoa.lib.Sense360;

import timber.log.Timber;

/**
 * Created by amartinz on 19.05.16.
 */

public class Sense360Impl {
    public static void init(Context applicationContext) {
        try {
            if (!Sense360.isUserOptedOut(applicationContext)) {
                Timber.v("Starting Sense360");
                Sense360.start(applicationContext);
            } else {
                Timber.v("Stopping Sense360");
                Sense360.stop(applicationContext);
            }
        } catch (NoSuchMethodError | Exception exc) {
            Timber.e(exc, "Could not start/stop Sense360");
        }
    }
}
