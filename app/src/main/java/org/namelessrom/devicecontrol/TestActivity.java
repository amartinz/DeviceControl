package org.namelessrom.devicecontrol;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.utils.AppHelper;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TestActivity extends Activity {

    @InjectView(R.id.test_textview) TextView testTextView;

    private final Handler mHandler = new Handler();
    private WebServerService webServerService;

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.inject(this);
        mHandler.post(mLogRunnable);
    }

    @Override protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mLogRunnable);
        try {
            unbindService(mConnection);
        } catch (Exception ignored) { }
    }

    @Override protected void onResume() {
        super.onResume();
        final Intent intent = new Intent(TestActivity.this, WebServerService.class);
        intent.setAction(WebServerService.ACTION_START);
        if (!AppHelper.isServiceRunning(WebServerService.class.getName())) {
            startService(intent);
        }
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mHandler.postDelayed(mLogRunnable, 1000);
    }

    private final Runnable mLogRunnable = new Runnable() {
        @Override public void run() {

            if (webServerService != null) {
                final String s = webServerService.getWebServerLog();
                if (!s.isEmpty()) {
                    testTextView.setText(testTextView.getText() + s);
                }
            }

            mHandler.removeCallbacks(mLogRunnable);
            mHandler.postDelayed(mLogRunnable, 1000);
        }
    };

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            webServerService = ((WebServerService.WebServerBinder) binder).getService();
        }

        public void onServiceDisconnected(ComponentName className) {
            webServerService = null;
        }
    };
}
