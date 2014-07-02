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

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.events.CpuFreqEvent;
import org.namelessrom.devicecontrol.events.GovernorEvent;
import org.namelessrom.devicecontrol.events.GpuEvent;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.GpuUtils;
import org.namelessrom.devicecontrol.utils.monitors.MemoryInfo;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.Arrays;
import java.util.List;

/**
 * API Service for use by other apps.
 */
public class RemoteService extends Service {

    private CpuFreqEvent  mCpuFreqEvent;
    private GovernorEvent mGovernorEvent;
    private GpuEvent      mGpuEvent;

    @Override public IBinder onBind(final Intent intent) { return mBinder; }

    @Override public void onCreate() {
        super.onCreate();
        BusProvider.getBus().register(this);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().unregister(this);
    }

    @Subscribe public void onCpuFreqEvent(final CpuFreqEvent event) {
        if (event == null) return;
        mCpuFreqEvent = event;
    }

    @Subscribe public void onGovernorEvent(final GovernorEvent event) {
        if (event == null) return;
        mGovernorEvent = event;
    }

    @Subscribe public void onGpuEvent(final GpuEvent event) {
        if (event == null) return;
        mGpuEvent = event;
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {

        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        @Override public boolean isCpuFreqAvailable()
                throws RemoteException { return (mCpuFreqEvent != null); }

        @Override public void prepareCpuFreq() throws RemoteException {
            mCpuFreqEvent = null;
            CpuUtils.getCpuFreqEvent();
        }

        @Override public List<String> getAvailableCpuFrequencies() throws RemoteException {
            if (mCpuFreqEvent == null) return null;
            return Arrays.asList(mCpuFreqEvent.getCpuFreqAvail());
        }

        @Override public String getMaxFrequency() throws RemoteException {
            if (mCpuFreqEvent == null) return null;
            return mCpuFreqEvent.getCpuFreqMax();
        }

        @Override public String getMinFrequency() throws RemoteException {
            if (mCpuFreqEvent == null) return null;
            return mCpuFreqEvent.getCpuFreqMin();
        }

        @Override public int getAvailableCores()
                throws RemoteException { return CpuUtils.getNumOfCpus(); }

        @Override public void setMaxFrequency(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, value);
        }

        @Override public void setMinFrequency(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, value);
        }

        //------------------------------------------------------------------------------------------

        @Override public void prepareGovernor() throws RemoteException {
            mGovernorEvent = null;
            CpuUtils.getGovernorEvent();
        }

        @Override public boolean isGovernorAvailable()
                throws RemoteException { return (mGovernorEvent != null); }

        @Override public List<String> getAvailableGovernors() throws RemoteException {
            if (mGovernorEvent == null) return null;
            return Arrays.asList(mGovernorEvent.getAvailableGovernors());
        }

        @Override public String getCurrentGovernor() throws RemoteException {
            if (mGovernorEvent == null) return null;
            return mGovernorEvent.getCurrentGovernor();
        }

        @Override public void setGovernor(final String value) throws RemoteException {
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_GOVERNOR, value);
        }

        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        @Override public void prepareGpu() throws RemoteException {
            mGpuEvent = null;
            GpuUtils.getOnGpuEvent();
        }

        @Override public boolean isGpuAvailable()
                throws RemoteException { return (mGpuEvent != null); }

        @Override public List<String> getAvailableGpuFrequencies() throws RemoteException {
            if (mGpuEvent == null) return null;
            return Arrays.asList(mGpuEvent.getAvailFreqs());
        }

        @Override public String getMaxGpuFrequency() throws RemoteException {
            if (mGpuEvent == null) return null;
            return mGpuEvent.getMaxFreq();
        }

        @Override public String getCurrentGpuGovernor() throws RemoteException {
            if (mGpuEvent == null) return null;
            return mGpuEvent.getGovernor();
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
