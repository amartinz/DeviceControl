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
package org.namelessrom.devicecontrol.cpu.monitors;

import android.app.Activity;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.cpu.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.objects.CpuCore;

import java.util.ArrayList;
import java.util.List;

public class CpuCoreMonitor {

    private static final int CPU_COUNT = CpuUtils.get().getNumOfCpus();

    private static CpuCoreMonitor cpuFrequencyMonitor;
    private static Shell mShell;
    private static Activity mActivity;
    private static int mInterval;

    private static boolean isStarted = false;

    private static final Object mLock = new Object();

    private static CpuUtils.CoreListener mListener;

    private CpuCoreMonitor(final Activity activity) {
        openShell();
        mActivity = activity;
    }

    public static CpuCoreMonitor getInstance(final Activity activity) {
        if (cpuFrequencyMonitor == null) {
            cpuFrequencyMonitor = new CpuCoreMonitor(activity);
        }
        return cpuFrequencyMonitor;
    }

    public void start(final CpuUtils.CoreListener listener) { start(listener, 2000); }

    public void start(final CpuUtils.CoreListener listener, final int interval) {
        mListener = listener;
        mInterval = interval;
        if (!isStarted) {
            Application.HANDLER.post(mUpdater);
            isStarted = true;
            Logger.i(this, "started, interval: " + String.valueOf(mInterval));
        } else {
            Logger.i(this, "updated interval: " + String.valueOf(mInterval));
        }
    }

    public void stop() {
        mListener = null;
        isStarted = false;
        Application.HANDLER.removeCallbacks(mUpdater);
        Logger.v(this, "stopped!");
    }

    private final Runnable mUpdater = new Runnable() {
        @Override public void run() {
            synchronized (mLock) {
                updateStates();
            }
        }
    };

    private void openShell() {
        if (mShell == null || mShell.isClosed()) {
            try {
                mShell = RootTools.getShell(true);
            } catch (Exception exc) {
                Logger.e(this, "CpuCoreMonitor: " + exc.getMessage());
            }
        }
    }

    private void updateStates() {
        final String END = " 2> /dev/null;";
        final StringBuilder sb = new StringBuilder();
        // command=$(
        sb.append("command=$(");
        for (int i = 0; i < CPU_COUNT; i++) {
            // if cpufreq directory exists ...
            sb.append("if [ -d \"/sys/devices/system/cpu/cpu").append(String.valueOf(i))
                    .append("/cpufreq\" ]; then ");
            // busybox cat /path/to/cpu/frequency
            sb.append("busybox cat ").append(CpuUtils.get().getCpuFrequencyPath(i)).append(END);
            // busybox cat /path/to/cpu/frequency_max
            sb.append("busybox cat ").append(CpuUtils.get().getMaxCpuFrequencyPath(i)).append(END);
            // busybox cat /path/to/cpu/governor
            sb.append("busybox cat ").append(GovernorUtils.get().getGovernorPath(i)).append(END);
            // ... else echo 0 for them
            sb.append("else busybox echo \"0 0 0\"").append(END).append(" fi;");
        }
        // replace new lines with space
        sb.append(");").append("echo $command | busybox tr -d \"\\n\"");
        // example output: 0 162000 1890000 interactive
        final String cmd = sb.toString();
        Logger.v(this, "cmd: " + cmd);

        final StringBuilder outputCollector = new StringBuilder();
        final CommandCapture commandCapture = new CommandCapture(0, cmd) {
            @Override
            public void commandOutput(int id, String line) {
                outputCollector.append(line);
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                final String output = outputCollector.toString();
                Logger.v(this, "output: " + output);

                if (mActivity != null) {
                    final String[] parts = output.split(" ");
                    final List<CpuCore> mCoreList = new ArrayList<>(CPU_COUNT);
                    int mult = 0;
                    CpuCore tmp;
                    String max, current;
                    for (int i = 0; i < CPU_COUNT; i++) {
                        try {
                            max = parts[i + mult];
                            current = parts[i + mult + 1];
                            tmp = new CpuCore(
                                    mActivity.getString(
                                            R.string.core) + ' ' + String.valueOf(i) + ':',
                                    max,
                                    current,
                                    parts[i + mult + 2]
                            );
                        } catch (IndexOutOfBoundsException iob) {
                            tmp = new CpuCore(mActivity.getString(
                                    R.string.core) + ' ' + String.valueOf(i) + ':',
                                    "0",
                                    "0",
                                    "0"
                            );
                        }
                        mCoreList.add(tmp);
                        mult += 2;
                    }

                    if (mListener != null) {
                        Application.HANDLER.post(new Runnable() {
                            @Override public void run() {
                                if (mListener != null) {
                                    mListener.onCores(new CpuUtils.Cores(mCoreList));
                                }
                            }
                        });
                    }
                }

                Application.HANDLER.removeCallbacks(mUpdater);
                Application.HANDLER.postDelayed(mUpdater, mInterval);
            }
        };

        openShell();
        if (mShell != null && !mShell.isClosed() && isStarted) {
            mShell.add(commandCapture);
        }
    }

}
