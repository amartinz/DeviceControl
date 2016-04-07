package org.namelessrom.devicecontrol.modules.appmanager;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.os.Parcel;
import android.os.RemoteException;

import org.namelessrom.devicecontrol.App;

import timber.log.Timber;

/**
 * Our Stub for the package stats observer.
 * Usually we just have to override onGetStatsCompleted but my android studio instance is
 * going crazy and produces apps, which crash at onTransact...
 */
public class PackageStatsObserver extends IPackageStatsObserver.Stub {

    private static final String DESCRIPTOR = "android.content.pm.IPackageStatsObserver";

    private OnPackageStatsListener packageStatsListener;

    public PackageStatsObserver(final OnPackageStatsListener listener) {
        packageStatsListener = listener;
    }

    @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
            throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION: {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            case FIRST_CALL_TRANSACTION: {
                data.enforceInterface(DESCRIPTOR);
                final PackageStats _arg0;
                if ((0 != data.readInt())) {
                    _arg0 = PackageStats.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                final boolean _arg1 = (0 != data.readInt());
                this.onGetStatsCompleted(_arg0, _arg1);
                return true;
            }
        }
        return true;
    }

    @Override public void onGetStatsCompleted(final PackageStats pStats, final boolean success) throws RemoteException {
        Timber.v("onGetStatsCompleted(): %s", success);
        App.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                packageStatsListener.onPackageStats(pStats);
            }
        });
    }

    public interface OnPackageStatsListener {
        void onPackageStats(final PackageStats packageStats);
    }

}
