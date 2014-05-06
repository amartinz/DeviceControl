/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.DonationStartedEvent;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;
import org.namelessrom.devicecontrol.proprietary.Constants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class PreferencesFragment extends AttachPreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    //==============================================================================================
    // App
    //==============================================================================================
    private CustomPreference         mDonatePreference;
    private CustomCheckBoxPreference mMonkeyPref;
    //==============================================================================================
    // Set On Boot
    //==============================================================================================
    private CustomCheckBoxPreference mSobDevice;
    private CustomCheckBoxPreference mSobCpu;
    private CustomCheckBoxPreference mSobGpu;
    private CustomCheckBoxPreference mSobExtras;
    private CustomCheckBoxPreference mSobVoltage;
    private CustomCheckBoxPreference mSobVm;
    private CustomCheckBoxPreference mSobSysCtl;
    //==============================================================================================
    // Debug
    //==============================================================================================
    private CustomCheckBoxPreference mExtensiveLogging;
    private CustomCheckBoxPreference mShowLauncher;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID_DUMMY);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml._device_control);

        final Activity activity = getActivity();
        PreferenceHelper.getInstance(activity);

        mExtensiveLogging = (CustomCheckBoxPreference) findPreference(EXTENSIVE_LOGGING);
        if (mExtensiveLogging != null) {
            mExtensiveLogging.setChecked(PreferenceHelper.getBoolean(EXTENSIVE_LOGGING));
            mExtensiveLogging.setOnPreferenceChangeListener(this);
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("prefs_general");
        if (category != null) {
            mShowLauncher = (CustomCheckBoxPreference) findPreference(SHOW_LAUNCHER);
            if (mShowLauncher != null) {
                if (Application.IS_NAMELESS) {
                    mShowLauncher.setChecked(PreferenceHelper.getBoolean(SHOW_LAUNCHER));
                    mShowLauncher.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mShowLauncher);
                }
            }

            if (category.getPreferenceCount() == 0) {
                getPreferenceScreen().removePreference(category);
            }
        }

        final CustomPreference mVersion = (CustomPreference) findPreference("prefs_version");
        if (mVersion != null) {
            mVersion.setEnabled(false);
            String title, summary;
            try {
                if (activity == null) { throw new Exception("activity is null"); }
                final PackageManager pm = activity.getPackageManager();
                if (pm != null) {
                    final PackageInfo pInfo = pm.getPackageInfo(
                            activity.getPackageName(), 0);
                    title = getString(R.string.app_version_name, pInfo.versionName);
                    summary = getString(R.string.app_version_code, pInfo.versionCode);
                } else {
                    throw new Exception("pm not null");
                }
            } catch (Exception ignored) {
                title = getString(R.string.app_version_name, getString(R.string.unknown));
                summary = getString(R.string.app_version_code, getString(R.string.unknown));
            }
            mVersion.setTitle(title);
            mVersion.setSummary(summary);
        }

        category = (PreferenceCategory) findPreference("prefs_app");
        if (category != null) {
            mDonatePreference = (CustomPreference) findPreference("pref_donate");
            if (mDonatePreference != null) {
                mDonatePreference
                        .setEnabled(PreferenceHelper.getBoolean(Constants.Iab.getPref(), false));
            }

            if (Utils.existsInBuildProp("ro.nameless.secret=1")) {
                mMonkeyPref = new CustomCheckBoxPreference(getActivity());
                mMonkeyPref.setKey("monkey");
                mMonkeyPref.setTitle(R.string.become_a_monkey);
                mMonkeyPref.setSummaryOn(R.string.is_monkey);
                mMonkeyPref.setSummaryOff(R.string.no_monkey);
                mMonkeyPref.setChecked(PreferenceHelper.getBoolean("monkey", false));
                mMonkeyPref.setOnPreferenceChangeListener(this);
                category.addPreference(mMonkeyPref);
            }
        }

        mSobDevice = (CustomCheckBoxPreference) findPreference(SOB_DEVICE);
        if (mSobDevice != null) {
            mSobDevice.setChecked(PreferenceHelper.getBoolean(SOB_DEVICE));
            mSobDevice.setOnPreferenceChangeListener(this);
        }

        mSobCpu = (CustomCheckBoxPreference) findPreference(SOB_CPU);
        if (mSobCpu != null) {
            mSobCpu.setChecked(PreferenceHelper.getBoolean(SOB_CPU));
            mSobCpu.setOnPreferenceChangeListener(this);
        }

        mSobGpu = (CustomCheckBoxPreference) findPreference(SOB_GPU);
        if (mSobGpu != null) {
            mSobGpu.setChecked(PreferenceHelper.getBoolean(SOB_GPU));
            mSobGpu.setOnPreferenceChangeListener(this);
        }

        mSobExtras = (CustomCheckBoxPreference) findPreference(SOB_EXTRAS);
        if (mSobExtras != null) {
            mSobExtras.setChecked(PreferenceHelper.getBoolean(SOB_EXTRAS));
            mSobExtras.setOnPreferenceChangeListener(this);
        }

        mSobVoltage = (CustomCheckBoxPreference) findPreference(SOB_VOLTAGE);
        if (mSobVoltage != null) {
            mSobVoltage.setChecked(PreferenceHelper.getBoolean(SOB_VOLTAGE));
            mSobVoltage.setOnPreferenceChangeListener(this);
        }

        mSobVm = (CustomCheckBoxPreference) findPreference(SOB_VM);
        if (mSobVm != null) {
            mSobVm.setChecked(PreferenceHelper.getBoolean(SOB_VM));
            mSobVm.setOnPreferenceChangeListener(this);
        }

        mSobSysCtl = (CustomCheckBoxPreference) findPreference(SOB_SYSCTL);
        if (mSobSysCtl != null) {
            mSobSysCtl.setChecked(PreferenceHelper.getBoolean(SOB_SYSCTL));
            mSobSysCtl.setOnPreferenceChangeListener(this);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            view.setBackgroundResource(R.drawable.preference_drawer_background);
        }

        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(EXTENSIVE_LOGGING, value);
            Application.IS_LOG_DEBUG = value;
            mExtensiveLogging.setChecked(value);
            changed = true;
        } else if (mShowLauncher == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SHOW_LAUNCHER, value);
            Application.toggleLauncherIcon(value);
            mShowLauncher.setChecked(value);
            changed = true;
        } else if (mMonkeyPref == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean("monkey", value);
            // TODO: add some more easter eggs?
            mMonkeyPref.setChecked(value);
            changed = true;
        } else if (mSobDevice == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_DEVICE, value);
            mSobDevice.setChecked(value);
            changed = true;
        } else if (mSobCpu == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_CPU, value);
            mSobCpu.setChecked(value);
            changed = true;
        } else if (mSobGpu == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_GPU, value);
            mSobGpu.setChecked(value);
            changed = true;
        } else if (mSobExtras == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_EXTRAS, value);
            mSobExtras.setChecked(value);
            changed = true;
        } else if (mSobVoltage == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_VOLTAGE, value);
            mSobVoltage.setChecked(value);
            changed = true;
        } else if (mSobVm == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_VM, value);
            mSobVm.setChecked(value);
            changed = true;
        } else if (mSobSysCtl == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SOB_SYSCTL, value);
            mSobSysCtl.setChecked(value);
            changed = true;
        }

        return changed;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (mDonatePreference == preference) {
            final Activity activity = getActivity();
            if (activity == null) { return false; }
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.donate)
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }
                    );
            final ListView listView = new ListView(activity);
            final String[] items = getResources().getStringArray(R.array.donation_items);
            listView.setAdapter(new ArrayAdapter<String>(
                    activity,
                    android.R.layout.simple_list_item_1,
                    items));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    int i = position;
                    if (i > 5) i = 5;
                    if (i < 0) i = 0;
                    final String sku = DonationStartedEvent.SKU_DONATION_BASE + String.valueOf(i);
                    logDebug("SKU: " + sku);
                    BusProvider.getBus().post(new DonationStartedEvent(sku));
                }
            });
            builder.setView(listView);

            final AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


}
