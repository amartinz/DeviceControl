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
package org.namelessrom.devicecontrol.hardware.monitors;

import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.hardware.GpuUtils;

public class GpuMonitor {
    private static GpuMonitor gpuMonitor;
    private static Shell mShell;
    private static int mInterval;

    private static boolean isStarted = false;

    private static final Object mLock = new Object();

    private static GpuMonitorListener mListener;

    public static interface GpuMonitorListener {
        public void onGpu(final GpuUtils.Gpu gpu);
    }

    private GpuMonitor() { }

    public static GpuMonitor getInstance() {
        if (gpuMonitor == null) {
            gpuMonitor = new GpuMonitor();
        }
        return gpuMonitor;
    }

    public void start(final GpuMonitorListener listener) { start(listener, 2000); }

    public void start(final GpuMonitorListener listener, final int interval) {
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

    private void updateStates() {
        if (mListener != null) {
            Application.HANDLER.post(new Runnable() {
                @Override public void run() {
                    if (mListener != null) {
                        mListener.onGpu(GpuUtils.get().getGpu());
                    }
                }
            });
        }

        Application.HANDLER.removeCallbacks(mUpdater);
        Application.HANDLER.postDelayed(mUpdater, mInterval);
    }

}
