package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.net.NetworkInfo;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

/**
 * Created by alex on 27.05.14.
 */
public class WirelessFileManagerFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener {

    private CustomPreference         mWirelessFileManager;
    private CustomCheckBoxPreference mBrowseRoot;

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_WIRELESS_FM);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.tools_wireless_file_manager);

        mWirelessFileManager = (CustomPreference) findPreference("wireless_file_manager");
        if (mWirelessFileManager != null) {
            if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
                mWirelessFileManager.setSummary(getString(R.string.stop_wireless_file_manager,
                        "http://" + NetworkInfo.getAnyIpAddress() + ":8080"));
            } else {
                mWirelessFileManager.setSummary(R.string.start_wireless_file_manager);
            }
        }

        mBrowseRoot = (CustomCheckBoxPreference) findPreference("wireless_file_manager_root");
        if (mBrowseRoot != null) {
            mBrowseRoot.setChecked(PreferenceHelper.getBoolean(mBrowseRoot.getKey(), false));
            mBrowseRoot.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mWirelessFileManager == preference) {
            final Intent i = new Intent(Application.applicationContext, WebServerService.class);
            if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
                i.setAction(WebServerService.ACTION_STOP);
                mWirelessFileManager.setSummary(R.string.start_wireless_file_manager);
            } else {
                i.setAction(WebServerService.ACTION_START);
                mWirelessFileManager.setSummary(getString(R.string.stop_wireless_file_manager,
                        "http://" + NetworkInfo.getAnyIpAddress() + ":8080"));
            }
            Application.applicationContext.startService(i);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mBrowseRoot == preference) {
            PreferenceHelper.setBoolean(mBrowseRoot.getKey(), (Boolean) o);
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }

}
