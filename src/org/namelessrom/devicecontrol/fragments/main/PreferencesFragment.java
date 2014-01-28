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

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

/**
 * Created by alex on 18.12.13.
 */
public class PreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;
    private PackageManager mPm;

    //==============================================================================================
    // Appearance
    //==============================================================================================
    private SwitchPreference mCustomAnimations;
    private ListPreference mTransformerId;

    //==============================================================================================
    // Debug
    //==============================================================================================
    private SwitchPreference mExtensiveLogging;

    //==============================================================================================
    // Reapply on boot
    //==============================================================================================
    private CheckBoxPreference mSobVm;
    private CheckBoxPreference mSobSysctl;

    //==============================================================================================
    // Extras
    //==============================================================================================
    private SwitchPreference mExtrasLauncher;

    //==============================================================================================
    // About
    //==============================================================================================
    private Preference mVersion;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml._device_control);

        PreferenceHelper.getInstance(getActivity());

        mPm = getActivity().getPackageManager();

        mCustomAnimations = (SwitchPreference)
                findPreference("prefs_jf_appearance_custom_animations");
        mCustomAnimations.setChecked(PreferenceHelper.getCustomAnimations());
        mCustomAnimations.setOnPreferenceChangeListener(this);

        mTransformerId = (ListPreference) findPreference("prefs_jf_appearance_custom_transformer");
        if (mTransformerId.getValue() == null) mTransformerId.setValueIndex(0);
        mTransformerId.setOnPreferenceChangeListener(this);

        mExtensiveLogging = (SwitchPreference) findPreference("jf_extensive_logging");
        mExtensiveLogging.setOnPreferenceChangeListener(this);

        if (getResources().getBoolean(R.bool.is_system_app)) {
            mExtrasLauncher = (SwitchPreference) findPreference("jf_extras_launcher");
            mExtrasLauncher.setChecked(switchLauncher(false));
            mExtrasLauncher.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference("prefs_jf_extras"));
        }

        mSobVm = (CheckBoxPreference) findPreference("prefs_sob_vm");
        mSobVm.setChecked(PreferenceHelper.getBoolean("prefs_sob_vm", false));
        mSobVm.setOnPreferenceChangeListener(this);

        mSobSysctl = (CheckBoxPreference) findPreference("prefs_sob_sysctl");
        mSobSysctl.setChecked(PreferenceHelper.getBoolean("prefs_sob_sysctl", false));
        mSobSysctl.setOnPreferenceChangeListener(this);

        mVersion = findPreference("prefs_version");
        mVersion.setEnabled(false);
        try {
            final PackageInfo pInfo = getActivity().getPackageManager()
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
        View view = super.onCreateView(inflater, container, savedInstanceState);

        view.setBackgroundResource(R.drawable.preference_drawer_background);

        return view;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;
        boolean restart = false;

        if (preference == mCustomAnimations) {
            PreferenceHelper.setCustomAnimations(mCustomAnimations.isChecked());
            changed = true;
            restart = true;
        } else if (preference == mTransformerId) {
            PreferenceHelper.setTransformerId(newValue.toString());
            changed = true;
            restart = mCustomAnimations.isChecked();
        } else if (preference == mExtensiveLogging) {
            PreferenceHelper.setBoolean(JF_EXTENSIVE_LOGGING, mExtensiveLogging.isChecked());
            changed = true;
        } else if (preference == mExtrasLauncher) {
            mExtrasLauncher.setChecked(switchLauncher(true));
            changed = true;
        } else if (preference == mSobVm) {
            PreferenceHelper.setBoolean("prefs_sob_vm", (Boolean) newValue);
            changed = true;
        } else if (preference == mSobSysctl) {
            PreferenceHelper.setBoolean("prefs_sob_sysctl", (Boolean) newValue);
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
        if (mDrawerLayout != null)
            mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        if (mDrawerLayout != null)
            mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    //

    private boolean switchLauncher(boolean shouldSwitch) {
        boolean isShowing;

        ComponentName component = new ComponentName(PACKAGE_NAME,
                PACKAGE_NAME + ".activities.DummyLauncher");
        isShowing = ((mPm.getComponentEnabledSetting(component) ==
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
                || (mPm.getComponentEnabledSetting(component) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED));

        if (shouldSwitch) {
            if (isShowing) {
                mPm.setComponentEnabledSetting(component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            } else {
                mPm.setComponentEnabledSetting(component,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
            isShowing = !isShowing;
        }

        return isShowing;
    }
}
