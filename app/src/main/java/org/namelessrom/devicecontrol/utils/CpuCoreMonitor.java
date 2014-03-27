/*
 *  Copyright (C) 2014 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.utils;

import android.app.Activity;
import android.os.Handler;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.CpuCoreEvent;
import org.namelessrom.devicecontrol.utils.classes.CpuCore;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;

import java.util.ArrayList;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class CpuCoreMonitor implements DeviceConstants {

    private static final int CPU_COUNT = CpuUtils.getNumOfCpus();

    private static CpuCoreMonitor cpuFrequencyMonitor;
    private static Shell          mShell;
    private static Activity       mActivity;
    private static Handler        mHandler;
    private static int            mInterval;

    private static boolean isStarted = false;

    private static final Object mLock = new Object();

    private CpuCoreMonitor(final Activity activity) {
        openShell();
        mActivity = activity;
        mHandler = new Handler();
    }

    public static CpuCoreMonitor getInstance(final Activity activity) {
        if (cpuFrequencyMonitor == null) {
            cpuFrequencyMonitor = new CpuCoreMonitor(activity);
        }
        return cpuFrequencyMonitor;
    }

    public void start() { start(2000); }

    public void start(final int interval) {
        mInterval = interval;
        if (!isStarted) {
            mHandler.post(mUpdater);
            isStarted = true;
            logDebug("Started CpuCoreMonitor! Interval: " + String.valueOf(mInterval));
        } else {
            logDebug("Updated interval: " + String.valueOf(mInterval));
        }
    }

    public void stop() {
        isStarted = false;
        mHandler.removeCallbacks(mUpdater);
        if (mShell != null) {
            mShell.close();
            mShell = null;
        }
        logDebug("Stopped CpuCoreMonitor!");
    }

    private final Runnable mUpdater = new Runnable() {
        @Override
        public void run() {
            synchronized (mLock) {
                updateStates();
            }
        }
    };

    private void openShell() {
        if (mShell == null) {
            try {
                mShell = RootTools.getShell(true);
            } catch (Exception exc) {
                logDebug("CpuCoreMonitor: " + exc.getMessage());
            }
        }
    }

    private void updateStates() {
        final StringBuilder sb = new StringBuilder();
        // command=$(
        sb.append("command=$(");
        for (int i = 0; i < CPU_COUNT; i++) {
            // if cpufreq directory exists ...
            sb.append("if [ -d \"/sys/devices/system/cpu/cpu").append(String.valueOf(i))
                    .append("/cpufreq\" ]; then ");
            // busybox cat /path/to/cpu/frequency
            sb.append("busybox cat ").append(CpuUtils.getCpuFrequencyPath(i))
                    .append(" 2> /dev/null;");
            // busybox cat /path/to/cpu/frequency_max
            sb.append("busybox cat ").append(CpuUtils.getMaxCpuFrequencyPath(i))
                    .append(" 2> /dev/null;");
            // busybox cat /path/to/cpu/governor
            sb.append("busybox cat ").append(CpuUtils.getGovernorPath(i))
                    .append(" 2> /dev/null;");
            // ... else echo 0 for them
            sb.append("else busybox echo \"0 0 0\" 2> /dev/null; fi;");
        }
        // replace new lines with space
        sb.append(");").append("echo $command | tr -d \"\\n\"");
        // example output: 0 162000 1890000 interactive
        final String cmd = sb.toString();
        logDebug("cmd: " + cmd);

        final StringBuilder outputCollector = new StringBuilder();
        final CommandCapture commandCapture = new CommandCapture(0, false, cmd) {
            @Override
            public void commandOutput(int id, String line) {
                outputCollector.append(line);
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                final String output = outputCollector.toString();
                logDebug("output: " + output);

                if (mActivity != null) {
                    final String[] parts = output.split(" ");
                    final List<CpuCore> mCoreList = new ArrayList<CpuCore>(CPU_COUNT);
                    int mult = 0;
                    CpuCore tmp;
                    for (int i = 0; i < CPU_COUNT; i++) {
                        try {
                            tmp = new CpuCore(
                                    mActivity.getString(
                                            R.string.core) + " " + String.valueOf(i) + ":",
                                    parts[i + mult + 0],
                                    parts[i + mult + 1],
                                    parts[i + mult + 2]
                            );
                        } catch (IndexOutOfBoundsException iob) {
                            tmp = new CpuCore(mActivity.getString(
                                    R.string.core) + " " + String.valueOf(i) + ":",
                                    "0",
                                    "0",
                                    "0"
                            );
                        }
                        mCoreList.add(tmp);
                        mult += 2;
                    }

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(new CpuCoreEvent(mCoreList));
                        }
                    });
                }

                mHandler.removeCallbacks(mUpdater);
                mHandler.postDelayed(mUpdater, mInterval);
            }
        };

        openShell();
        mShell.add(commandCapture);
    }

}
