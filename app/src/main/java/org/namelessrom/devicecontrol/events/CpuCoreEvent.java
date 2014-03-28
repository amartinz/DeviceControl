package org.namelessrom.devicecontrol.events;

import org.namelessrom.devicecontrol.utils.classes.CpuCore;

import java.util.List;

public class CpuCoreEvent {

    public final List<CpuCore> mCoreList;

    public CpuCoreEvent(List<CpuCore> coreList) {
        mCoreList = coreList;
    }

    public List<CpuCore> getStates() {
        return mCoreList;
    }

}
