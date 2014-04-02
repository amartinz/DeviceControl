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
package org.namelessrom.devicecontrol.fragments.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
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
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.DonationStartedEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class PreferencesFragment extends AttachPreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    //==============================================================================================
    // In App Purchase
    //==============================================================================================
    private CustomPreference         mDonatePreference;
    //==============================================================================================
    // Debug
    //==============================================================================================
    private CustomCheckBoxPreference mExtensiveLogging;
    private CustomCheckBoxPreference mShowLauncher;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml._device_control);

        PreferenceHelper.getInstance(mActivity);

        mExtensiveLogging = (CustomCheckBoxPreference) findPreference(EXTENSIVE_LOGGING);
        mExtensiveLogging.setOnPreferenceChangeListener(this);

        PreferenceCategory category = (PreferenceCategory) findPreference("prefs_general");
        if (category != null) {
            mShowLauncher = (CustomCheckBoxPreference) findPreference(SHOW_LAUNCHER);
            if (Application.IS_NAMELESS) {
                mShowLauncher.setOnPreferenceChangeListener(this);
            } else {
                category.removePreference(mShowLauncher);
            }
            if (category.getPreferenceCount() == 0) {
                getPreferenceScreen().removePreference(category);
            }
        }

        final Preference mVersion = findPreference("prefs_version");
        mVersion.setEnabled(false);
        try {
            final PackageInfo pInfo = mActivity.getPackageManager()
                    .getPackageInfo(mActivity.getPackageName(), 0);
            mVersion.setTitle(getString(R.string.app_version_name, pInfo.versionName));
            mVersion.setSummary(getString(R.string.app_version_code, pInfo.versionCode));
        } catch (Exception ignored) {
            final String unknown = getString(R.string.unknown);
            mVersion.setTitle(unknown);
            mVersion.setSummary(unknown);
        }

        mDonatePreference = (CustomPreference) findPreference("pref_donate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        view.setBackgroundResource(R.drawable.preference_drawer_background);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(EXTENSIVE_LOGGING, value);
            Application.IS_LOG_DEBUG = value;
            changed = true;
        } else if (mShowLauncher == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SHOW_LAUNCHER, value);
            Application.toggleLauncherIcon(value);
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
                public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
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
