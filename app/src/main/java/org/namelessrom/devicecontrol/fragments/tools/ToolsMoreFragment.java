package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

public class ToolsMoreFragment extends AttachPreferenceFragment implements DeviceConstants,
        MediaScannerConnection.MediaScannerConnectionClient {

    private MediaScannerConnection mMediaScannerConnection;
    private CustomPreference       mMediaScan;
    private String                 mMediaScanPath;

    private CustomPreference mBuildProp;
    private CustomPreference mSysctlVm;
    private CustomPreference mAppManager;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_MORE);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_more);

        mMediaScan = (CustomPreference) findPreference("media_scan");

        mBuildProp = (CustomPreference) findPreference("build_prop");
        mSysctlVm = (CustomPreference) findPreference("sysctl_vm");

        mAppManager = (CustomPreference) findPreference("app_manager");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if (key == null || key.isEmpty()) return false;

        if (mMediaScan == preference) {
            startMediaScan();
        } else if (mAppManager == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_APP_MANAGER));
        } else if (mBuildProp == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_BUILD_PROP));
        } else if (mSysctlVm == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_VM));
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void startMediaScan() {
        if (mMediaScannerConnection != null) {
            mMediaScannerConnection.disconnect();
        }
        mMediaScannerConnection = new MediaScannerConnection(Application.applicationContext, this);
        mMediaScannerConnection.connect();
    }

    @Override
    public void onMediaScannerConnected() {
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

    @Override
    public void onScanCompleted(final String path, final Uri uri) {
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
