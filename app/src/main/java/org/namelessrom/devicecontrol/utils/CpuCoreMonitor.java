/*
 * Performance Control - An Android CPU Control application
 * Copyright (C) Brandon Valosek, 2011 <bvalosek@gmail.com>
 * Copyright (C) Modified by 2012 James Roberts
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.utils;

import android.content.Context;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.classes.CpuCore;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;

import java.util.ArrayList;
import java.util.List;

public class CpuCoreMonitor implements DeviceConstants {

    private static final int CPU_COUNT = CpuUtils.getNumOfCpus();

    private static Context        mContext;
    private static CpuCoreMonitor cpuFrequencyMonitor;

    private List<CpuCore> mStates = new ArrayList<CpuCore>();

    private CpuCoreMonitor(Context context) {
        mContext = context;
    }

    public static CpuCoreMonitor getInstance(Context context) {
        if (cpuFrequencyMonitor == null) {
            cpuFrequencyMonitor = new CpuCoreMonitor(context);
        }
        return cpuFrequencyMonitor;
    }

    public class CpuCoreMonitorException extends Exception {
        public CpuCoreMonitorException(String s) {
            super(s);
        }
    }

    public List<CpuCore> getCores() {
        return mStates;
    }

    public List<CpuCore> updateStates() throws CpuCoreMonitorException {
        mStates.clear();

        CpuCore tmpCore;
        for (int i = 0; i < CPU_COUNT; i++) {
            tmpCore = new CpuCore(
                    mContext.getString(R.string.core) + " " + String.valueOf(i) + ": ",
                    CpuUtils.getCpuFrequency(i),
                    CpuUtils.getValue(i, CpuUtils.ACTION_FREQ_MAX),
                    CpuUtils.getValue(i, CpuUtils.ACTION_GOV)
            );
            mStates.add(tmpCore);
        }

        return mStates;
    }

}
