package org.namelessrom.devicecontrol.events;

import org.namelessrom.devicecontrol.utils.monitors.CpuStateMonitor;

import java.util.List;

public class CpuStateEvent {

    private final List<CpuStateMonitor.CpuState> mStateList;
    private final long                           mTotalStateTime;

    public CpuStateEvent(final List<CpuStateMonitor.CpuState> stateList,
            final long totalStateTime) {
        mStateList = stateList;
        mTotalStateTime = totalStateTime;
    }

    public List<CpuStateMonitor.CpuState> getStates() { return mStateList; }

    public long getTotalStateTime() { return mTotalStateTime; }

}
