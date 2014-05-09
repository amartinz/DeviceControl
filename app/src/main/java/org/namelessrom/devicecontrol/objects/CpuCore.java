package org.namelessrom.devicecontrol.objects;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class CpuCore {

    public final String mCore;
    public final int    mCoreMax;
    public final int    mCoreCurrent;
    public final String mCoreGov;

    public CpuCore(final String core, final int coreCurrent,
            final int coreMax, final String coreGov) {
        mCore = ((core != null && !core.isEmpty()) ? core : "0");
        mCoreMax = coreMax;
        mCoreCurrent = coreCurrent;
        mCoreGov = ((coreGov != null && !coreGov.isEmpty()) ? coreGov : "0");
        logDebug("mCore: [" + mCore + ']');
        logDebug("mCoreMax: [" + mCoreMax + ']');
        logDebug("mCoreCurrent: [" + mCoreCurrent + ']');
        logDebug("mCoreGov: [" + mCoreGov + ']');
    }

}
