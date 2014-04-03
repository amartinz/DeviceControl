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

package org.namelessrom.devicecontrol.monitors;

import android.app.Activity;
import android.os.SystemClock;
import android.util.SparseArray;

import org.namelessrom.devicecontrol.events.CpuStateEvent;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.CpuUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CpuStateMonitor implements DeviceConstants {

    private final List<CpuState>    mStates  = new ArrayList<CpuState>();
    private final SparseArray<Long> mOffsets = new SparseArray<Long>();

    private static CpuStateMonitor mCpuStateMonitor;
    private static Activity        mActivity;

    private CpuStateMonitor(final Activity activity) {
        mActivity = activity;
    }

    public static CpuStateMonitor getInstance(final Activity activity) {
        if (mCpuStateMonitor == null) {
            mCpuStateMonitor = new CpuStateMonitor(activity);
        }

        return mCpuStateMonitor;
    }

    public class CpuState implements Comparable<CpuState> {
        public CpuState(int a, long b) {
            freq = a;
            duration = b;
        }

        public int  freq     = 0;
        public long duration = 0;

        public int compareTo(final CpuState state) {
            final Integer a = freq;
            final Integer b = state.freq;
            return a.compareTo(b);
        }
    }

    private long getTotalStateTime() {
        long sum = 0;
        long offset = 0;

        for (final CpuState state : mStates) {
            sum += state.duration;
        }

        final int size = mOffsets.size();
        for (int i = 0; i < size; i++) {
            offset += mOffsets.valueAt(i);
        }
        return sum - offset;
    }

    public void updateStates() throws IOException {
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            is = new FileInputStream(CpuUtils.FREQ_TIME_IN_STATE_PATH);
            ir = new InputStreamReader(is);
            br = new BufferedReader(ir);
            mStates.clear();
            readInStates(br);
        } finally {
            if (br != null) br.close();
            if (ir != null) ir.close();
            if (is != null) is.close();
        }

        final long sleepTime = (SystemClock.elapsedRealtime() - SystemClock.uptimeMillis()) / 10;
        mStates.add(new CpuState(0, sleepTime));

        Collections.sort(mStates, Collections.reverseOrder());

        final long totalStateTime = getTotalStateTime();
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BusProvider.getBus().post(new CpuStateEvent(mStates, totalStateTime));
            }
        });
    }

    private void readInStates(final BufferedReader br) throws IOException {
        String line;
        String[] nums;
        while ((line = br.readLine()) != null) {
            nums = line.split(" ");
            mStates.add(new CpuState(Integer.parseInt(nums[0]), Long.parseLong(nums[1])));
        }
    }
}
