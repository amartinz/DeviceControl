package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class ToolsMoreFragment extends AttachPreferenceFragment implements DeviceConstants,
        MediaScannerConnection.MediaScannerConnectionClient, Preference.OnPreferenceClickListener {

    private MediaScannerConnection mMediaScannerConnection;
    private ListPreference         mMediaScan;
    private String                 mMediaScanPath;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_MORE);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_more);

        mMediaScan = (ListPreference) findPreference("media_scan");
        if (mMediaScan != null) {
            final String[] paths = getMediaScanPaths();
            mMediaScan.setEntries(paths);
            mMediaScan.setEntryValues(paths);
            mMediaScan.setOnPreferenceClickListener(this);
        }
    }

    private String[] getMediaScanPaths() {
        final List<String> fileList = new ArrayList<String>();

        fileList.add(Environment.getExternalStorageDirectory().getPath());

        // we can add more files this way if needed
        /*
        File tmp = new File("/storage/sdcard1");
        if (tmp.exists()) {
            fileList.add(tmp.getPath());
        }
        tmp = new File("...");
        ...
        */

        return fileList.toArray(new String[fileList.size()]);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        if (key == null || key.isEmpty()) return false;

        if (key.equals("freezer")) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_FREEZER));
        } else if (key.equals("editors")) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_EDITORS));
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
            mMediaScannerConnection.scanFile(mMediaScanPath, null);
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

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (mMediaScan == preference) {
            mMediaScanPath = mMediaScan.getValue();
            startMediaScan();
            return true;
        }

        return false;
    }
}
