package org.namelessrom.devicecontrol.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.events.CpuFreqEvent;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.Arrays;
import java.util.List;

/**
 * API Service to use by other apps.
 */
public class RemoteService extends Service {

    private CpuFreqEvent mCpuFreqEvent;

    @Override
    public IBinder onBind(final Intent intent) { return mBinder; }

    @Override
    public void onCreate() {
        super.onCreate();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().unregister(this);
    }

    @Subscribe
    public void onCpuFreqEvent(final CpuFreqEvent event) {
        if (event == null) return;

        mCpuFreqEvent = event;
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {

        @Override
        public boolean isCpuFreqAvailable() throws RemoteException {
            return (mCpuFreqEvent != null);
        }

        @Override
        public void prepareCpuFreq() throws RemoteException {
            mCpuFreqEvent = null;
            CpuUtils.getCpuFreqEvent();
        }

        @Override
        public List<String> getAvailableCpuFrequencies() throws RemoteException {
            if (mCpuFreqEvent == null) return null;

            return Arrays.asList(mCpuFreqEvent.getCpuFreqAvail());
        }

    };
}
