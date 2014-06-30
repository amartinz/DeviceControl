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
import java.util.ArrayList;
import java.util.List;

/**
 * An example activity for interacting with the DeviceControl API
 */
public class MainActivity extends Activity {

    private static final String TAG = "ApiSample";

    private static final int REQUEST_FILE_PICKER = 100;

    private IRemoteService mIRemoteService;
    private TextView       mTextView;

    @Override protected void onCreate(Bundle savedInstanceState) {
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

    @Override public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        // if we press refresh, just refresh and return...
        if (id == R.id.menu_refresh) {
            if (mTextView != null) {
                mTextView.setText("Refreshing!\n\n");
            }
            new DeviceTask().execute();
            return true;
        } else if (id == R.id.menu_pick_file) {
            final Intent i = new Intent();
            i.setAction("org.namelessrom.devicecontrol.api.FILE_PICKER");
            try {
                startActivityForResult(i, REQUEST_FILE_PICKER);
            } catch (Exception e) {
                final String error = String.format("Error picking file: %s", e.getMessage());
                Log.e(TAG, error);
                appendText(error);
            }
            return true;
        }

        // ... else check if we have a service connection, return false if not.
        if (mIRemoteService == null) return false;

        // as we just do some random stuff, create a securerandom and do the random stuff ;P
        try {
            final SecureRandom secureRandom = new SecureRandom();
            final List<String> list;
            if (id == R.id.menu_set_cpu_max || id == R.id.menu_set_cpu_min) {
                list = mIRemoteService.getAvailableCpuFrequencies();
            } else if (id == R.id.menu_set_cpu_gov) {
                list = mIRemoteService.getAvailableGovernors();
            } else if (id == R.id.menu_set_gpu_max) {
                list = mIRemoteService.getAvailableGpuFrequencies();
            } else {
                // with the gpu governor we have to guess...
                list = new ArrayList<String>(4);
                list.add("ondemand");
                list.add("performance");
                list.add("interactive");
                list.add("simple");
            }
            String value;

            switch (id) {
                case R.id.menu_set_cpu_max:
                    value = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setMaxFrequency(value);
                    break;
                case R.id.menu_set_cpu_min:
                    value = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setMinFrequency(value);
                    break;
                case R.id.menu_set_cpu_gov:
                    value = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setGovernor(value);
                    break;
                case R.id.menu_set_gpu_max:
                    value = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setMaxGpuFrequency(value);
                    break;
                case R.id.menu_set_gpu_gov:
                    value = list.get(secureRandom.nextInt(list.size() - 1));
                    mIRemoteService.setGpuGovernor(value);
                    break;
                default:
                    value = "nothing";
                    break;
            }

            Log.e(TAG, "Applied: " + value);
        } catch (Exception ignored) { return false; }

        return false;
    }

    @Override protected void onActivityResult(final int req, final int res, final Intent data) {
        if (req == REQUEST_FILE_PICKER && res == Activity.RESULT_OK && data != null) {
            final String name = data.getStringExtra("name");
            final String path = data.getStringExtra("path");
            final String msg = String.format("Picked a file!\nName: %s\nPath: %s\n\n", name, path);
            appendText(msg);
        } else {
            super.onActivityResult(req, res, data);
        }
    }

    @Override protected void onDestroy() {
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

    /**
     * Appends text to the textview.
     * This method is fully thread safe and can be called from anywhere.
     *
     * @param text The text to append
     */
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
    @Subscribe public void onServiceConnectedEvent(final ServiceConnectedEvent event) {
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
    @Subscribe public void onServiceDisconnectedEvent(final ServiceDisconnectedEvent event) {
        if (event == null) return;

        if (mTextView != null) {
            appendText("\nService disconnected!");
        }
    }

    /**
     * Our Asynctask to utilize the DeviceControl API
     */
    private class DeviceTask extends AsyncTask<Void, Void, Void> {
        private StringBuilder sb;

        @Override protected void onPreExecute() {
            // check if service is null and cancel
            if (mIRemoteService == null) {
                cancel(true);
            }

            // prepare what we need
            sb = new StringBuilder();
            try {
                // we need to tell the service to prepare the things we need...
                mIRemoteService.prepareCpuFreq();
                mIRemoteService.prepareGovernor();
                mIRemoteService.prepareGpu();
            } catch (RemoteException e) {
                sb.append("Error: ").append(e);
            }
            appendText(sb.toString());
        }

        @Override protected Void doInBackground(Void... params) {
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
                sb.append("Current Maximum CPU-Frequency: ")
                        .append(mIRemoteService.getMaxFrequency())
                        .append('\n');
                sb.append("Current Minimum CPU-Frequency: ")
                        .append(mIRemoteService.getMinFrequency())
                        .append('\n');
                sb.append('\n');
                appendText(sb.toString());

                while (mIRemoteService != null && !mIRemoteService.isGovernorAvailable()) {
                    /* wait */
                }
                sb = new StringBuilder();

                list = mIRemoteService.getAvailableGovernors();
                sb.append("Available CPU-Governors:\n");
                for (final String s : list) {
                    sb.append(s).append('\n');
                }
                sb.append('\n');
                sb.append("Current CPU-Governor: ")
                        .append(mIRemoteService.getCurrentGovernor())
                        .append('\n');
                sb.append('\n');
                appendText(sb.toString());

                while (mIRemoteService != null && !mIRemoteService.isGpuAvailable()) {
                    /* wait */
                }
                sb = new StringBuilder();

                list = mIRemoteService.getAvailableGpuFrequencies();
                sb.append("Available GPU-Frequencies:\n");
                for (final String s : list) {
                    sb.append(s).append('\n');
                }
                sb.append('\n');
                sb.append("Current Maximum GPU-Frequency: ")
                        .append(mIRemoteService.getMaxGpuFrequency())
                        .append('\n');
                sb.append('\n');
                sb.append("Current GPU-Governor: ")
                        .append(mIRemoteService.getCurrentGpuGovernor())
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
