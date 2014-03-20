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
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;

/**
 * Created by alex on 18.12.13.
 */
public class PreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private DrawerLayout mDrawerLayout;
    private View         mFragmentContainerView;

    //==============================================================================================
    // Appearance
    //==============================================================================================
    private CustomCheckBoxPreference mCustomAnimations;
    private CustomListPreference     mTransformerId;

    //==============================================================================================
    // Debug
    //==============================================================================================
    private CustomCheckBoxPreference mExtensiveLogging;

    //==============================================================================================
    // Reapply on boot
    //==============================================================================================
    private CustomCheckBoxPreference mSobVm;
    private CustomCheckBoxPreference mSobSysctl;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml._device_control);

        final Activity activity = getActivity();
        PreferenceHelper.getInstance(activity);

        mCustomAnimations = (CustomCheckBoxPreference)
                findPreference("prefs_jf_appearance_custom_animations");
        mCustomAnimations.setChecked(PreferenceHelper.getCustomAnimations());
        mCustomAnimations.setOnPreferenceChangeListener(this);

        mTransformerId = (CustomListPreference)
                findPreference("prefs_jf_appearance_custom_transformer");
        if (mTransformerId.getValue() == null) mTransformerId.setValueIndex(0);
        mTransformerId.setOnPreferenceChangeListener(this);

        mExtensiveLogging = (CustomCheckBoxPreference) findPreference("jf_extensive_logging");
        mExtensiveLogging.setOnPreferenceChangeListener(this);

        mSobVm = (CustomCheckBoxPreference) findPreference("prefs_sob_vm");
        mSobVm.setChecked(PreferenceHelper.getBoolean("prefs_sob_vm", false));
        mSobVm.setOnPreferenceChangeListener(this);

        mSobSysctl = (CustomCheckBoxPreference) findPreference("prefs_sob_sysctl");
        mSobSysctl.setChecked(PreferenceHelper.getBoolean("prefs_sob_sysctl", false));
        mSobSysctl.setOnPreferenceChangeListener(this);

        final Preference mVersion = findPreference("prefs_version");
        mVersion.setEnabled(false);
        try {
            final PackageInfo pInfo = activity.getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            mVersion.setTitle(getString(R.string.app_version_name, pInfo.versionName));
            mVersion.setSummary(getString(R.string.app_version_code, pInfo.versionCode));
        } catch (Exception ignored) {
            final String unknown = getString(R.string.unknown);
            mVersion.setTitle(unknown);
            mVersion.setSummary(unknown);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        view.setBackgroundResource(R.drawable.preference_drawer_background);

        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;
        boolean restart = false;

        if (preference == mCustomAnimations) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setCustomAnimations(value);
            changed = true;
            restart = true;
        } else if (preference == mTransformerId) {
            PreferenceHelper.setTransformerId(newValue.toString());
            changed = true;
            restart = mCustomAnimations.isChecked();
        } else if (preference == mExtensiveLogging) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(JF_EXTENSIVE_LOGGING, value);
            org.namelessrom.devicecontrol.Application.IS_LOG_DEBUG = value;
            changed = true;
        } else if (preference == mSobVm) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean("prefs_sob_vm", value);
            changed = true;
        } else if (preference == mSobSysctl) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean("prefs_sob_sysctl", value);
            changed = true;
        }

        if (restart) {
            Utils.restartActivity(getActivity());
        }

        return changed;
    }

    //

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_prefs, GravityCompat.END);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public void openDrawer() {
        if (mDrawerLayout != null) { mDrawerLayout.openDrawer(mFragmentContainerView); }
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) { mDrawerLayout.closeDrawer(mFragmentContainerView); }
    }
}
