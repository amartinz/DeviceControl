package org.namelessrom.devicecontrol.utils.classes;

import android.util.Log;

/**
 * Created by alex on 28.02.14.
 */
public class CpuCore {

    public final String mCore;
    public final String mCoreMax;
    public final String mCoreCurrent;
    public final String mCoreGov;

    public CpuCore(final String core, final String coreCurrent,
                   final String coreMax, final String coreGov) {
        mCore = core;
        mCoreMax = coreMax;
        mCoreCurrent = coreCurrent;
        mCoreGov = coreGov;
    }

}
