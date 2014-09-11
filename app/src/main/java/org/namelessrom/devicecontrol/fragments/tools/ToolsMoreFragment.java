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
package org.namelessrom.devicecontrol.fragments.tools;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class ToolsMoreFragment extends AttachPreferenceFragment implements DeviceConstants,
        MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMediaScannerConnection;
    private String                 mMediaScanPath;
    private CustomPreference       mMediaScan;

    @Override protected int getFragmentId() { return ID_TOOLS_MORE; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_more);

        mMediaScan = (CustomPreference) findPreference("media_scan");
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        final String key = preference.getKey();

        if (TextUtils.isEmpty(key)) return false;

        if (mMediaScan == preference) {
            startMediaScan();
        } else if (TextUtils.equals(key, "app_manager")) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_APP_MANAGER);
        } else if (TextUtils.equals(key, "wireless_file_manager")) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_WIRELESS_FM);
        } else if (TextUtils.equals(key, "build_prop")) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_BUILD_PROP);
        } else if (TextUtils.equals(key, "sysctl_vm")) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_VM);
        } else if (TextUtils.equals(key, "low_memory_killer")) {
            MainActivity.loadFragment(getActivity(), ID_LOWMEMORYKILLER);
        } else if (TextUtils.equals(key, "log_collector")) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_LOG_COLLECTOR);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void startMediaScan() {
        if (mMediaScannerConnection != null) {
            mMediaScannerConnection.disconnect();
        }
        mMediaScannerConnection = new MediaScannerConnection(getActivity(), this);
        mMediaScannerConnection.connect();
    }

    @Override public void onMediaScannerConnected() {
        try {
            if (mMediaScanPath == null || mMediaScanPath.isEmpty()) {
                mMediaScanPath = Environment.getExternalStorageDirectory().getPath();
            }
            mMediaScannerConnection.scanFile(mMediaScanPath, "*/*");
            Application.HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (mMediaScan != null) {
                        mMediaScan.setEnabled(false);
                        mMediaScan.setSummary("Media Scanner started scanning " + mMediaScanPath);
                    }
                }
            });
        } catch (Exception ignored) { /* ignored */ }
    }

    @Override public void onScanCompleted(final String path, final Uri uri) {
        Application.HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (mMediaScan != null) {
                    mMediaScan.setEnabled(true);
                    mMediaScan.setSummary("Media Scanner finished scanning " + path);
                }
            }
        });
        if (mMediaScannerConnection.isConnected()) {
            mMediaScannerConnection.disconnect();
        }
    }

}
