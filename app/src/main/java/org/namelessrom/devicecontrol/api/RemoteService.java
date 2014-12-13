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
package org.namelessrom.devicecontrol.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.objects.MemoryInfo;

import java.util.Arrays;
import java.util.List;

/**
 * API Service for use by other apps.
 */
public class RemoteService extends Service implements CpuUtils.FrequencyListener,
        GovernorUtils.GovernorListener {

    private CpuUtils.Frequency mCpuFreq;
    private GovernorUtils.Governor mGovernor;
    private GpuUtils.Gpu mGpu;

    @Override public IBinder onBind(final Intent intent) { return mBinder; }

    @Override public void onFrequency(@NonNull final CpuUtils.Frequency cpuFreq) {
        mCpuFreq = cpuFreq;
    }

    @Override public void onGovernor(@NonNull final GovernorUtils.Governor governor) {
        mGovernor = governor;
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {

        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        @Override public boolean isCpuFreqAvailable() throws RemoteException {
            return (mCpuFreq != null);
        }

        @Override public void prepareCpuFreq() throws RemoteException {
            mCpuFreq = null;
            CpuUtils.get().getCpuFreq(RemoteService.this);
        }

        @Override public List<String> getAvailableCpuFrequencies() throws RemoteException {
            if (mCpuFreq == null) return null;
            return Arrays.asList(mCpuFreq.available);
        }

        @Override public String getMaxFrequency() throws RemoteException {
            if (mCpuFreq == null) return null;
            return mCpuFreq.maximum;
        }

        @Override public String getMinFrequency() throws RemoteException {
            if (mCpuFreq == null) return null;
            return mCpuFreq.minimum;
        }

        @Override public int getAvailableCores() throws RemoteException {
            return CpuUtils.get().getNumOfCpus();
        }

        @Override public void setMaxFrequency(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, value);
        }

        @Override public void setMinFrequency(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, value);
        }

        //------------------------------------------------------------------------------------------

        @Override public void prepareGovernor() throws RemoteException {
            mGovernor = null;
            GovernorUtils.get().getGovernor(RemoteService.this);
        }

        @Override public boolean isGovernorAvailable() throws RemoteException {
            return (mGovernor != null);
        }

        @Override public List<String> getAvailableGovernors() throws RemoteException {
            if (mGovernor == null) return null;
            return Arrays.asList(mGovernor.available);
        }

        @Override public String getCurrentGovernor() throws RemoteException {
            if (mGovernor == null) return null;
            return mGovernor.current;
        }

        @Override public void setGovernor(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_GOVERNOR, value);
        }

        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        @Override public void prepareGpu() throws RemoteException {
            mGpu = GpuUtils.get().getGpu();
        }

        @Override public boolean isGpuAvailable() throws RemoteException { return (mGpu != null); }

        @Override public List<String> getAvailableGpuFrequencies() throws RemoteException {
            if (mGpu == null) return null;
            return Arrays.asList(mGpu.available);
        }

        @Override public String getMaxGpuFrequency() throws RemoteException {
            if (mGpu == null) return null;
            return mGpu.max;
        }

        @Override public String getCurrentGpuGovernor() throws RemoteException {
            if (mGpu == null) return null;
            return mGpu.governor;
        }

        @Override public void setMaxGpuFrequency(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_FREQUENCY_MAX, value);
        }

        @Override public void setGpuGovernor(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_GPU_GOVERNOR, value);
        }

        //------------------------------------------------------------------------------------------
        // Memory
        //------------------------------------------------------------------------------------------
        @Override public long[] readMemory() { return MemoryInfo.getInstance().readMemory(); }

        @Override public long[] readMemoryByType(final int type) {
            return MemoryInfo.getInstance().readMemory(type);
        }
    };
}
