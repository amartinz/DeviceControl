/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.bus;

import org.namelessrom.devicecontrol.hardware.monitors.CpuStateMonitor;

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
