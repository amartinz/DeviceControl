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
package org.namelessrom.devicecontrol.modules.cpu.monitors;

import android.app.Activity;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.objects.CpuCore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import alexander.martinz.libs.execution.Command;
import alexander.martinz.libs.execution.Shell;
import alexander.martinz.libs.execution.ShellManager;

public class CpuCoreMonitor {
    private static final int CPU_COUNT = CpuUtils.get().getNumOfCpus();

    private static CpuCoreMonitor cpuFrequencyMonitor;
    private Shell mShell;
    private Activity mActivity;

    private boolean isStarted = false;

    private final Object mLock = new Object();

    private CpuUtils.CoreListener mListener;
    private int mInterval;

    private final List<CpuCore> mCoreList = new ArrayList<>(CPU_COUNT);

    private final String CORE_STRING;

    private CpuCoreMonitor(final Activity activity) {
        getShell();
        mActivity = activity;

        CORE_STRING = mActivity.getString(R.string.core);
        for (int i = 0; i < CPU_COUNT; i++) {
            String coreString = String.format("%s %s:", CORE_STRING, String.valueOf(i));
            mCoreList.add(new CpuCore(coreString, "0", "0", "0"));
        }
    }

    public static CpuCoreMonitor getInstance(final Activity activity) {
        if (cpuFrequencyMonitor == null) {
            cpuFrequencyMonitor = new CpuCoreMonitor(activity);
        }
        return cpuFrequencyMonitor;
    }

    public CpuCoreMonitor start(final CpuUtils.CoreListener listener) {
        return start(listener, 2000);
    }

    public CpuCoreMonitor start(final CpuUtils.CoreListener listener, final int interval) {
        mListener = listener;
        mInterval = interval;
        if (!isStarted) {
            isStarted = true;
            Application.HANDLER.post(mUpdater);
            Logger.i(this, "started, interval: " + String.valueOf(mInterval));
        } else {
            Logger.i(this, "updated interval: " + String.valueOf(mInterval));
        }

        return cpuFrequencyMonitor;
    }

    public CpuCoreMonitor stop() {
        if (isStarted) {
            isStarted = false;
            mListener = null;
            Application.HANDLER.removeCallbacks(mUpdater);
            Logger.v(this, "stopped!");
        }
        return cpuFrequencyMonitor;
    }

    public void destroy() {
        stop();
        mActivity = null;
        cpuFrequencyMonitor = null;
    }

    private final Runnable mUpdater = new Runnable() {
        @Override public void run() {
            synchronized (mLock) {
                updateStates();
            }
        }
    };

    private Shell getShell() {
        if (mShell == null || mShell.isClosed() || mShell.shouldClose()) {
            final boolean shouldUseRoot = shouldUseRoot();
            Logger.v(this, "shouldUseRoot: %s", shouldUseRoot);
            if (shouldUseRoot) {
                mShell = ShellManager.get().getRootShell();
            } else {
                mShell = ShellManager.get().getNormalShell();
            }
        }
        return mShell;
    }

    private boolean shouldUseRoot() {
        for (int i = 0; i < CPU_COUNT; i++) {
            final String[] paths = new String[]{
                    CpuUtils.get().getCpuFrequencyPath(i),
                    CpuUtils.get().getMaxCpuFrequencyPath(i),
                    CpuUtils.get().getMinCpuFrequencyPath(i),
                    GovernorUtils.get().getGovernorPath(i)
            };
            for (final String path : paths) {
                if (!(new File(path).canRead())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateStates() {
        if (getShell() == null) {
            Logger.e(this, "Could not open shell!");
            return;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CPU_COUNT; i++) {
            // if cpufreq directory exists ...
            sb.append("if [ -d \"/sys/devices/system/cpu/cpu").append(String.valueOf(i)).append("/cpufreq\" ]; then\n");
            // cat /path/to/cpu/frequency
            sb.append(String.format("(cat \"%s\") 2> /dev/null;\n", CpuUtils.get().getCpuFrequencyPath(i)));
            sb.append("echo -n \" \";");
            // cat /path/to/cpu/frequency_max
            sb.append(String.format("(cat \"%s\") 2> /dev/null;\n", CpuUtils.get().getMaxCpuFrequencyPath(i)));
            sb.append("echo -n \" \";");
            // cat /path/to/cpu/governor
            sb.append(String.format("(cat \"%s\") 2> /dev/null;\n", GovernorUtils.get().getGovernorPath(i)));
            // ... else echo 0 for them
            sb.append("else echo \"0 0 0\";fi;");
            // ... and append a space on the end
            sb.append("echo -n \" \";");
        }

        // example output: 162000 1890000 interactive
        final String cmd = sb.toString();
        final Command command = new Command(cmd) {
            @Override public void onCommandCompleted(int id, int exitCode) {
                super.onCommandCompleted(id, exitCode);

                String output = getOutput();
                if (output == null) {
                    return;
                }
                output = output.replace("\n", " ");

                if (mActivity != null) {
                    final String[] parts = output.split(" ");
                    int mult = 0;
                    for (int i = 0; i < CPU_COUNT; i++) {
                        CpuCore cpuCore;
                        try {
                            cpuCore = mCoreList.get(i);
                        } catch (IndexOutOfBoundsException iobe) {
                            cpuCore = new CpuCore(String.format("%s %s:", CORE_STRING, String.valueOf(i)), "0", "0", "0");
                        }
                        try {
                            cpuCore.setCurrent(parts[i + mult])
                                    .setMax(parts[i + mult + 1])
                                    .setGovernor(parts[i + mult + 2]);
                        } catch (IndexOutOfBoundsException iob) {
                            cpuCore.setCurrent("0").setMax("0").setGovernor("0");
                        }
                        mult += 2;
                    }

                    if (mListener != null && mActivity != null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override public void run() {
                                if (mListener != null) {
                                    mListener.onCores(mCoreList);
                                }
                            }
                        });
                    }
                }

                Application.HANDLER.removeCallbacks(mUpdater);
                Application.HANDLER.postDelayed(mUpdater, mInterval);
            }
        };
        command.setOutputType(Command.OUTPUT_STRING);
        getShell().add(command);
    }

}
