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
package org.namelessrom.devicecontrol;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

/**
 * Dummy Activity, used as Launcher.
 * The AddTaskActivity still stays fully functional even if we deactivate this class component.
 * <p/>
 * If we deactivate this activity, the launcher icon disappears.
 */
public class DummyLauncher extends BaseActivity {

    private RelativeLayout mProgressLayout;
    private TextView mProgressStatus;

    private LinearLayout mLauncher;
    private TextView mStatus;
    private Button mAction;

    private final Handler mHandler = new Handler();

    @Override protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_launcher);

        mProgressLayout = (RelativeLayout) findViewById(R.id.launcher_progress);
        mProgressStatus = (TextView) findViewById(R.id.launcher_progress_status);

        mLauncher = (LinearLayout) findViewById(R.id.launcher_layout);
        mStatus = (TextView) findViewById(R.id.launcher_status);
        mAction = (Button) findViewById(R.id.btn_left);
        mAction.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                if (!Device.get().hasRoot) {
                    final String url = String.format("https://www.google.com/#q=how+to+root+%s",
                            Device.get().model);
                    AppHelper.viewInBrowser(url);
                } else if (!Device.get().hasBusyBox) {
                    RootTools.offerBusyBox();
                }
            }
        });
        findViewById(R.id.btn_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                finish();
            }
        });

        if (PreferenceHelper.getBoolean(DeviceConstants.SKIP_CHECKS, false)) {
            startActivity();
        } else {
            new CheckTools().execute();
        }
    }

    private void updateStatus(final String text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                if (mProgressStatus == null) return;
                mProgressStatus.setText(text);
            }
        });
    }

    private void startActivity() {
        startActivity(new Intent(DummyLauncher.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }

    private class CheckTools extends AsyncTask<Void, Void, Device> {

        @Override protected Device doInBackground(Void... params) {
            final Device device = Device.get();

            updateStatus(getString(R.string.checking_requirements));
            device.update();

            Logger.i(this, "hasRoot -> %s", device.hasRoot);
            Logger.i(this, "suVersion -> %s", device.suVersion);
            Logger.i(this, "hasBusyBox -> %s", device.hasBusyBox);

            return device;
        }

        @Override protected void onPostExecute(final Device device) {
            if (device.hasRoot && device.hasBusyBox) {
                startActivity();
                return;
            }

            mProgressLayout.setVisibility(View.GONE);
            mLauncher.setVisibility(View.VISIBLE);

            if (device.hasRoot) {
                final String status = getString(R.string.app_warning_busybox,
                        getString(R.string.app_name)) + "\n\n" +
                        getString(R.string.app_warning_busybox_note);
                mStatus.setText(status);
                mAction.setText(R.string.get_busybox);
            } else {
                mStatus.setText(getString(R.string.app_warning_root, getString(R.string.app_name)));
                mAction.setText(R.string.more_information);
            }
        }
    }

}
