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
package org.namelessrom.devicecontrol.modules.tools;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.WebServerConfig;
import org.namelessrom.devicecontrol.net.NetworkInfo;
import org.namelessrom.devicecontrol.services.WebServerService;
import org.namelessrom.devicecontrol.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;


public class WirelessFileManagerFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private CustomTogglePreference mWirelessFileManager;
    private CustomTogglePreference mBrowseRoot;
    private CustomEditTextPreference mPort;

    private CustomTogglePreference mUseAuth;
    private CustomEditTextPreference mUsername;
    private CustomEditTextPreference mPassword;

    private final Handler mHandler = new Handler();

    private WebServerConfig webServerConfig;

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_WIRELESS_FM; }

    @Override public void onResume() {
        super.onResume();
        if (mWirelessFileManager != null) {
            updateWebServerPreference();
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_wireless_file_manager);

        webServerConfig = WebServerConfig.get();

        mWirelessFileManager = (CustomTogglePreference) findPreference("wireless_file_manager");
        updateWebServerPreference();
        mWirelessFileManager.setOnPreferenceClickListener(this);

        mBrowseRoot = (CustomTogglePreference) findPreference(WebServerConfig.ROOT);
        mBrowseRoot.setChecked(webServerConfig.root);
        mBrowseRoot.setOnPreferenceChangeListener(this);

        mPort = (CustomEditTextPreference) findPreference(WebServerConfig.PORT);
        mPort.setSummary(String.valueOf(webServerConfig.port));
        mPort.setText(String.valueOf(webServerConfig.port));
        mPort.setOnPreferenceChangeListener(this);

        mUseAuth = (CustomTogglePreference) findPreference(WebServerConfig.USE_AUTH);
        mUseAuth.setChecked(webServerConfig.useAuth);
        mUseAuth.setOnPreferenceChangeListener(this);

        mUsername = (CustomEditTextPreference) findPreference(WebServerConfig.USERNAME);
        mUsername.setSummary(webServerConfig.username);
        mUsername.setText(webServerConfig.username);
        mUsername.setOnPreferenceChangeListener(this);

        mPassword = (CustomEditTextPreference) findPreference(WebServerConfig.PASSWORD);
        mPassword.setSummary("******");
        mPassword.setText(webServerConfig.password);
        mPassword.setOnPreferenceChangeListener(this);
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mBrowseRoot == preference) {
            webServerConfig.root = (Boolean) o;
            webServerConfig.save();
            return true;
        } else if (mPort == preference) {
            final String value = String.valueOf(o);

            webServerConfig.port = Utils.parseInt(value, 8080);
            webServerConfig.save();

            mPort.setText(value);
            mPort.setSummary(value);
            return true;
        } else if (mUseAuth == preference) {
            webServerConfig.useAuth = (Boolean) o;
            webServerConfig.save();
            return true;
        } else if (mUsername == preference) {
            final String value = String.valueOf(o);

            webServerConfig.username = value;
            webServerConfig.save();

            mUsername.setText(value);
            mUsername.setSummary(value);
            return true;
        } else if (mPassword == preference) {
            final String value = String.valueOf(o);

            webServerConfig.password = value;
            webServerConfig.save();

            mPassword.setText(value);
            mPassword.setSummary("******");
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceClick(final Preference preference) {
        if (mWirelessFileManager == preference) {
            final Intent i = new Intent(getActivity(), WebServerService.class);
            if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
                i.setAction(WebServerService.ACTION_STOP);
                mWirelessFileManager.setSummary(R.string.web_server_not_running);
            } else {
                i.setAction(WebServerService.ACTION_START);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            getActivity().startService(i);
            mWirelessFileManager.setEnabled(false);
            mHandler.postDelayed(new Runnable() {
                @Override public void run() {
                    if (mWirelessFileManager != null) {
                        updateWebServerPreference();
                        mWirelessFileManager.setEnabled(true);
                    }
                }
            }, 1000);
            return true;
        }

        return false;
    }

    public void updateWebServerPreference() {
        final boolean isRunning = AppHelper.isServiceRunning(WebServerService.class.getName());
        final String text;
        if (isRunning) {
            final String ip = NetworkInfo.getAnyIpAddress();
            final String port = String.valueOf(WebServerConfig.get().port);
            text = getString(R.string.web_server_running, String.format("http://%s:%s", ip, port));
        } else {
            text = getString(R.string.web_server_not_running);
        }
        mWirelessFileManager.setSummary(text);
        mWirelessFileManager.setChecked(isRunning);
    }

}
