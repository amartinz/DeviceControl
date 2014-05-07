package org.namelessrom.devicecontrol.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.api.IRemoteService;
import org.namelessrom.devicecontrol.sample.events.ServiceConnectedEvent;
import org.namelessrom.devicecontrol.sample.events.ServiceDisconnectedEvent;

import java.util.List;

/**
 * An example activity for interacting with the DeviceControl API
 */
public class MainActivity extends Activity {

    private static final String TAG = "ApiSample";

    private IRemoteService mIRemoteService;
    private TextView       mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // registering the bus
        BusProvider.getBus().register(this);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);

        // binding to our remote device control service
        bindService(new Intent(IRemoteService.class.getName()), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregistering the bus
        BusProvider.getBus().unregister(this);
        // unbinding service to prevent leaks
        unbindService(mConnection);
        Log.e(TAG, "Service disconnected!");
    }

    /**
     * Called, when a ServiceConnectedEvent gets posted to the bus
     *
     * @param event The posted event
     */
    @Subscribe
    public void onServiceConnectedEvent(final ServiceConnectedEvent event) {
        if (event == null) return;

        final StringBuilder sb = new StringBuilder();
        sb.append("Service Connected!\n\n");
        try {
            final List<String> frequencies = mIRemoteService.getAvailableCpuFrequencies();
            for (final String s : frequencies) {
                sb.append(s).append('\n');
            }
        } catch (RemoteException e) {
            sb.append("Error: ").append(e);
        }

        if (mTextView != null) {
            mTextView.setText(sb.toString());
        }
    }

    /**
     * Called, when a ServiceDisconnectedEvent gets posted to the bus
     *
     * @param event The posted event
     */
    @Subscribe
    public void onServiceDisconnectedEvent(final ServiceDisconnectedEvent event) {
        if (event == null) return;

        if (mTextView != null) {
            mTextView.setText(mTextView.getText() + "\n\n" + "Service disconnected!");
        }
    }

    /**
     * Our ServiceConnection to DeviceControl's RemoteService
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "Service connected!");
            mIRemoteService = IRemoteService.Stub.asInterface(service);
            BusProvider.getBus().post(new ServiceConnectedEvent());
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            mIRemoteService = null;
            BusProvider.getBus().post(new ServiceDisconnectedEvent());
        }
    };

}
