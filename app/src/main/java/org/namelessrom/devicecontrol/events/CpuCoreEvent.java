package org.namelessrom.devicecontrol.events;

import org.namelessrom.devicecontrol.objects.CpuCore;

import java.util.List;

public class CpuCoreEvent {

    private final List<CpuCore> mCoreList;

    public CpuCoreEvent(final List<CpuCore> coreList) { mCoreList = coreList; }

    public List<CpuCore> getStates() { return mCoreList; }

}
