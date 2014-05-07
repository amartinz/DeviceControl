package org.namelessrom.devicecontrol.api;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 07.05.14.
 */
public class RemoteService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private final IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        @Override
        public List<String> getAvailableCpuFrequencies() throws RemoteException {
            final List<String> list = new ArrayList<String>();

            list.add("100");
            list.add("200");
            list.add("300");
            list.add("400");
            list.add("500");
            list.add("600");

            return list;
        }
    };
}
