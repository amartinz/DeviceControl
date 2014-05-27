package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
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
import org.namelessrom.devicecontrol.widgets.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

public class WirelessFileManagerFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private WebServerService webServerService;

    private CustomPreference         mWirelessFileManager;
    private CustomCheckBoxPreference mBrowseRoot;
    private CustomEditTextPreference mPort;

    private CustomCheckBoxPreference mUseAuth;
    private CustomEditTextPreference mUsername;
    private CustomEditTextPreference mPassword;

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_WIRELESS_FM);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onPause() {
        super.onPause();
        unbindService();
    }

    @Override public void onResume() {
        super.onResume();
        bindService();
    }

    private void bindService() {
        final Intent i = new Intent(Application.applicationContext, WebServerService.class);
        if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
            Application.applicationContext.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        try {
            Application.applicationContext.unbindService(mConnection);
            webServerService = null;
        } catch (Exception ignored) { }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.tools_wireless_file_manager);

        String tmp;

        mWirelessFileManager = (CustomPreference) findPreference("wireless_file_manager");
        if (mWirelessFileManager != null) {
            if (!AppHelper.isServiceRunning(WebServerService.class.getName())) {
                mWirelessFileManager.setSummary(R.string.start_wfm);
            }
            mWirelessFileManager.setOnPreferenceClickListener(this);
        }

        mBrowseRoot = (CustomCheckBoxPreference) findPreference("wfm_root");
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

        mUseAuth = (CustomCheckBoxPreference) findPreference("wfm_auth");
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

    @Override public boolean onPreferenceClick(final Preference preference) {
        if (mWirelessFileManager == preference) {
            int callBind;
            final Intent i = new Intent(Application.applicationContext, WebServerService.class);
            if (AppHelper.isServiceRunning(WebServerService.class.getName())) {
                i.setAction(WebServerService.ACTION_STOP);
                mWirelessFileManager.setSummary(R.string.start_wfm);
                callBind = 1;
            } else {
                i.setAction(WebServerService.ACTION_START);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                callBind = 2;
            }
            Application.applicationContext.startService(i);
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

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(final ComponentName className, final IBinder binder) {
            webServerService = ((WebServerService.WebServerBinder) binder).getService();
            if (mWirelessFileManager != null) {
                final String port = ((webServerService != null
                        && webServerService.getSocket() != null)
                        ? String.valueOf(webServerService.getSocket().getLocalPort())
                        : PreferenceHelper.getString(mPort.getKey(), "8080"));
                mWirelessFileManager.setSummary(getString(R.string.stop_wfm,
                        "http://" + NetworkInfo.getAnyIpAddress() + ":" + port));
            }
        }

        public void onServiceDisconnected(final ComponentName className) {
            webServerService = null;
            if (mWirelessFileManager != null) {
                mWirelessFileManager.setSummary(R.string.start_wfm);
            }
        }
    };

}
