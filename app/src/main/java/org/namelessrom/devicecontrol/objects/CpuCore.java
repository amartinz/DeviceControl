package org.namelessrom.devicecontrol.objects;

import static org.namelessrom.devicecontrol.Application.logDebug;

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
        mCore = ((core != null && !core.isEmpty()) ? core : "0");
        mCoreMax = ((coreMax != null && !coreMax.isEmpty()) ? coreMax : "0");
        mCoreCurrent = ((coreCurrent != null && !coreCurrent.isEmpty()) ? coreCurrent : "0");
        mCoreGov = ((coreGov != null && !coreGov.isEmpty()) ? coreGov : "0");
        logDebug("mCore: [" + mCore + ']');
        logDebug("mCoreMax: [" + mCoreMax + ']');
        logDebug("mCoreCurrent: [" + mCoreCurrent + ']');
        logDebug("mCoreGov: [" + mCoreGov + ']');
    }

}
