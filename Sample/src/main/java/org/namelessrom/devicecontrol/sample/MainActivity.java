package org.namelessrom.devicecontrol.sample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.api.IRemoteService;
import org.namelessrom.devicecontrol.sample.events.ServiceConnectedEvent;
import org.namelessrom.devicecontrol.sample.events.ServiceDisconnectedEvent;

import java.security.SecureRandom;
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
        try {
            bindService(new Intent(IRemoteService.class.getName()), mConnection, BIND_AUTO_CREATE);
        } catch (SecurityException exception) {
            if (mTextView != null) {
                mTextView.setText("No permission to use DeviceControl API!");
            }
        } catch (Exception e) {
            if (mTextView != null) {
                mTextView.setText("An error occured: " + e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            if (mTextView != null) {
                mTextView.setText("Refreshing!\n\n");
            }
            new DeviceTask().execute();
            return true;
        }

        if (mIRemoteService == null) return false;
        try {
            final SecureRandom secureRandom = new SecureRandom();
            final List<String> list = mIRemoteService.getAvailableCpuFrequencies();
            String freq;

            switch (id) {
                case R.id.menu_set_max:
                    freq = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setMaxFrequency(freq);
                    break;
                case R.id.menu_set_min:
                    freq = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setMinFrequency(freq);
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) { return false; }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregistering the bus
        BusProvider.getBus().unregister(this);
        // unbinding service to prevent leaks
        try {
            unbindService(mConnection);
            Log.e(TAG, "Service disconnected!");
        } catch (SecurityException exception) {
            if (mTextView != null) {
                mTextView.setText("No permission to use DeviceControl API!");
            }
        }
    }

    private void appendText(final String text) {
        if (mTextView != null) {
            mTextView.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(mTextView.getText() + text);
                }
            });
        }
    }

    /**
     * Called, when a ServiceConnectedEvent gets posted to the bus
     *
     * @param event The posted event
     */
    @Subscribe
    public void onServiceConnectedEvent(final ServiceConnectedEvent event) {
        if (event == null) return;

        if (mTextView != null) {
            mTextView.setText("Service Connected!\n\n");
        }
        new DeviceTask().execute();
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
            appendText("\nService disconnected!");
        }
    }

    private class DeviceTask extends AsyncTask<Void, Void, Void> {
        private StringBuilder sb;

        @Override
        protected void onPreExecute() {
            // Prepare what we need
            sb = new StringBuilder();
            try {
                // we need to tell the service to prepare the things we need...
                mIRemoteService.prepareCpuFreq();
                mIRemoteService.prepareGovernor();
            } catch (RemoteException e) {
                sb.append("Error: ").append(e);
            }
            appendText(sb.toString());
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // ... and wait until it is available, else we may end up with some NULL's
                //     oh, also check if mIRemoteService is null as in case we wait for it and the
                //     service dies, we die with it.
                while (mIRemoteService != null && !mIRemoteService.isCpuFreqAvailable()) {
                    /* wait */
                }
                sb = new StringBuilder();
                sb.append("Available CPU-Cores:\n")
                        .append(mIRemoteService.getAvailableCores())
                        .append("\n\n");

                // is null if we didn't prepare CpuFreq before and wait for it
                List<String> list = mIRemoteService.getAvailableCpuFrequencies();
                sb.append("Available CPU-Frequencies:\n");
                for (final String s : list) {
                    sb.append(s).append('\n');
                }
                sb.append('\n');
                sb.append("Current Maximum Frequency: ")
                        .append(mIRemoteService.getMaxFrequency())
                        .append('\n');
                sb.append("Current Minimum Frequency: ")
                        .append(mIRemoteService.getMinFrequency())
                        .append('\n');
                sb.append('\n');
                appendText(sb.toString());

                while (mIRemoteService != null && !mIRemoteService.isGovernorAvailable()) {
                    /* wait */
                }
                sb = new StringBuilder();

                list = mIRemoteService.getAvailableGovernors();
                sb.append("Available Governors:\n");
                for (final String s : list) {
                    sb.append(s).append('\n');
                }
                sb.append('\n');
                sb.append("Current Governor: ")
                        .append(mIRemoteService.getCurrentGovernor())
                        .append('\n');
                sb.append('\n');
                appendText(sb.toString());
            } catch (RemoteException e) {
                appendText("Error: " + e);
            }

            return null;
        }

    }

    /**
     * Our ServiceConnection to DeviceControl's RemoteService
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(final ComponentName className, final IBinder service) {
            Log.e(TAG, "Service connected!");
            mIRemoteService = IRemoteService.Stub.asInterface(service);
            BusProvider.getBus().post(new ServiceConnectedEvent());
        }

        public void onServiceDisconnected(final ComponentName className) {
            Log.e(TAG, "Service has unexpectedly disconnected");
            mIRemoteService = null;
            BusProvider.getBus().post(new ServiceDisconnectedEvent());
        }
    };

}
