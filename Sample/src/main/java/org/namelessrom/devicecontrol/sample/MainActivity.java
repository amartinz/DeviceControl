package org.namelessrom.devicecontrol.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.namelessrom.devicecontrol.api.IRemoteService;

import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "ApiSample";

    // a handler for posting on the same thread as the activity
    private final Handler mHandler = new Handler();
    private IRemoteService mIRemoteService;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);

        // binding to our remote device control service
        bindService(new Intent(IRemoteService.class.getName()), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unbinding service to prevent leaks
        unbindService(mConnection);
        Log.e(TAG, "Service disconnected!");
    }

    /**
     * Change the text of the textview, running on the same thread as the activity
     *
     * @param text The text to display
     */
    private void setText(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTextView.setText(text);
            }
        });
    }

    /**
     * Our ServiceConnection to DeviceControl's RemoteService
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "Service connected!");
            mIRemoteService = IRemoteService.Stub.asInterface(service);

            final StringBuilder sb = new StringBuilder();
            try {
                final List<String> frequencies = mIRemoteService.getAvailableCpuFrequencies();
                for (final String s : frequencies) {
                    sb.append(s).append('\n');
                }
            } catch (RemoteException e) {
                sb.append("Error: ").append(e);
            }

            setText(sb.toString());
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            mIRemoteService = null;
        }
    };

}
