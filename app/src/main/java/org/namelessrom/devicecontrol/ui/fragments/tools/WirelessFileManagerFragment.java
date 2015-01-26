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
package org.namelessrom.devicecontrol.ui.fragments.tools;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.net.NetworkInfo;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.ui.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class WirelessFileManagerFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private WebServerService webServerService;

    private CustomPreference mWirelessFileManager;
    private CustomTogglePreference mBrowseRoot;
    private CustomEditTextPreference mPort;

    private CustomTogglePreference mUseAuth;
    private CustomEditTextPreference mUsername;
    private CustomEditTextPreference mPassword;

    @Override protected int getFragmentId() { return ID_TOOLS_WIRELESS_FM; }

    @Override public void onPause() {
        super.onPause();
        unbindService();
    }

    @Override public void onResume() {
        super.onResume();
        bindService();
    }

    private void bindService() {
        final Intent i = new Intent(getActivity(), WebServerService.class);
        if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
            getActivity().bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindService() {
        try {
            getActivity().unbindService(mConnection);
            webServerService = null;
        } catch (Exception ignored) { }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_wireless_file_manager);

        String tmp;

        mWirelessFileManager = (CustomPreference) findPreference("wireless_file_manager");
        if (mWirelessFileManager != null) {
            if (!AppHelper.isServiceRunning(WebServerService.class.getName())) {
                mWirelessFileManager.setSummary(R.string.start_wfm);
            }
            mWirelessFileManager.setOnPreferenceClickListener(this);
        }

        mBrowseRoot = (CustomTogglePreference) findPreference("wfm_root");
        if (mBrowseRoot != null) {
            mBrowseRoot.setChecked(PreferenceHelper.getBoolean(mBrowseRoot.getKey(), false));
            mBrowseRoot.setOnPreferenceChangeListener(this);
        }

        mPort = (CustomEditTextPreference) findPreference("wfm_port");
        if (mPort != null) {
            tmp = PreferenceHelper.getString(mPort.getKey(), "8080");
            mPort.setSummary(tmp);
            mPort.setText(tmp);
            mPort.setOnPreferenceChangeListener(this);
        }

        mUseAuth = (CustomTogglePreference) findPreference("wfm_auth");
        if (mUseAuth != null) {
            mUseAuth.setChecked(PreferenceHelper.getBoolean(mUseAuth.getKey(), true));
            mUseAuth.setOnPreferenceChangeListener(this);
        }

        mUsername = (CustomEditTextPreference) findPreference("wfm_username");
        if (mUsername != null) {
            tmp = PreferenceHelper.getString(mUsername.getKey(), "root");
            mUsername.setSummary(tmp);
            mUsername.setText(tmp);
            mUsername.setOnPreferenceChangeListener(this);
        }

        mPassword = (CustomEditTextPreference) findPreference("wfm_password");
        if (mPassword != null) {
            tmp = PreferenceHelper.getString(mPassword.getKey(), "toor");
            mPassword.setSummary("******");
            mPassword.setText(tmp);
            mPassword.setOnPreferenceChangeListener(this);
        }
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mBrowseRoot == preference) {
            PreferenceHelper.setBoolean(mBrowseRoot.getKey(), (Boolean) o);
            return true;
        } else if (mPort == preference) {
            final String value = String.valueOf(o);
            PreferenceHelper.setString(mPort.getKey(), value);
            mPort.setText(value);
            mPort.setSummary(value);
            return true;
        } else if (mUseAuth == preference) {
            PreferenceHelper.setBoolean(mUseAuth.getKey(), (Boolean) o);
            return true;
        } else if (mUsername == preference) {
            final String value = String.valueOf(o);
            mUsername.setText(value);
            mUsername.setSummary(value);
            PreferenceHelper.setString(mUsername.getKey(), value);
            return true;
        } else if (mPassword == preference) {
            final String value = String.valueOf(o);
            mPassword.setText(value);
            mPassword.setSummary("******");
            PreferenceHelper.setString(mPassword.getKey(), value);
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceClick(final Preference preference) {
        if (mWirelessFileManager == preference) {
            int callBind;
            final Intent i = new Intent(getActivity(), WebServerService.class);
            if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
                i.setAction(WebServerService.ACTION_STOP);
                mWirelessFileManager.setSummary(R.string.start_wfm);
                callBind = 1;
            } else {
                i.setAction(WebServerService.ACTION_START);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callBind = 2;
            }
            getActivity().startService(i);
            switch (callBind) {
                default:
                    break;
                case 1:
                    unbindService();
                    break;
                case 2:
                    bindService();
                    break;
            }
            return true;
        }

        return false;
    }

    public void updateSummary() {
        updateSummary(webServerService != null && webServerService.getServerSocket() != null);
    }

    public void updateSummary(final boolean started) {
        if (mWirelessFileManager == null) return;
        final String summary;
        if (started) {
            summary = getString(R.string.stop_wfm, "http://" + NetworkInfo.getAnyIpAddress() + ":"
                    + String.valueOf(webServerService.getServerSocket().getLocalPort()));
        } else {
            summary = getString(R.string.start_wfm);
        }
        mWirelessFileManager.setSummary(summary);
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            webServerService = ((WebServerService.WebServerBinder) binder).getService();
            updateSummary();
        }

        public void onServiceDisconnected(final ComponentName className) {
            webServerService = null;
            updateSummary(false);
        }
    };

}
