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
package org.namelessrom.devicecontrol.ui.fragments.preferences;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.preference.PreferenceFragment;

import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.ui.fragments.SobDialogFragment;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;

public class MainPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    //==============================================================================================
    // App
    //==============================================================================================
    private CustomTogglePreference mMonkeyPref;

    //==============================================================================================
    // General
    //==============================================================================================
    private CustomPreference mSetOnBoot;
    private CustomTogglePreference mShowLauncher;
    private CustomTogglePreference mSkipChecks;

    //==============================================================================================
    // Interface
    //==============================================================================================
    private CustomTogglePreference mSwipeOnContent;
    // TODO: more customization
    private CustomTogglePreference mDarkTheme;

    //==============================================================================================
    // Support
    //==============================================================================================
    private CustomTogglePreference mShowPollfish;

    //==============================================================================================
    // Debug
    //==============================================================================================
    private CustomTogglePreference mExtensiveLogging;

    @Override public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml._device_control);

        final DeviceConfiguration configuration = DeviceConfiguration.get(getActivity());

        mExtensiveLogging = (CustomTogglePreference) findPreference(
                DeviceConfiguration.EXTENSIVE_LOGGING);
        if (mExtensiveLogging != null) {
            mExtensiveLogging.setChecked(configuration.extensiveLogging);
            mExtensiveLogging.setOnPreferenceChangeListener(this);
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("prefs_general");
        mSetOnBoot = (CustomPreference) findPreference("prefs_set_on_boot");

        mShowLauncher = (CustomTogglePreference) findPreference(DeviceConfiguration.SHOW_LAUNCHER);
        if (mShowLauncher != null) {
            if (Application.IS_NAMELESS) {
                mShowLauncher.setChecked(configuration.showLauncher);
                mShowLauncher.setOnPreferenceChangeListener(this);
            } else {
                category.removePreference(mShowLauncher);
            }
        }

        mSkipChecks = (CustomTogglePreference) findPreference(DeviceConfiguration.SKIP_CHECKS);
        if (mSkipChecks != null) {
            mSkipChecks.setChecked(configuration.skipChecks);
            mSkipChecks.setOnPreferenceChangeListener(this);
        }

        category = (PreferenceCategory) findPreference("prefs_app");

        if (Utils.existsInFile(Scripts.BUILD_PROP, "ro.nameless.secret=1")) {
            mMonkeyPref = new CustomTogglePreference(getActivity());
            mMonkeyPref.setKey(DeviceConfiguration.MONKEY);
            mMonkeyPref.setTitle(R.string.become_a_monkey);
            mMonkeyPref.setSummaryOn(R.string.is_monkey);
            mMonkeyPref.setSummaryOff(R.string.no_monkey);
            mMonkeyPref.setChecked(DeviceConfiguration.get(getActivity()).monkey);
            mMonkeyPref.setOnPreferenceChangeListener(this);
            category.addPreference(mMonkeyPref);
        }

        mDarkTheme = (CustomTogglePreference) findPreference(DeviceConfiguration.DARK_THEME);
        mDarkTheme.setChecked(Application.get().isDarkTheme());
        mDarkTheme.setOnPreferenceChangeListener(this);

        mSwipeOnContent =
                (CustomTogglePreference) findPreference(DeviceConfiguration.SWIPE_ON_CONTENT);
        mSwipeOnContent.setChecked(configuration.swipeOnContent);
        mSwipeOnContent.setOnPreferenceChangeListener(this);

        mShowPollfish = (CustomTogglePreference) findPreference(DeviceConfiguration.SHOW_POLLFISH);
        mShowPollfish.setChecked(configuration.showPollfish);
        mShowPollfish.setOnPreferenceChangeListener(this);

        setupVersionPreference();
    }

    private void setupVersionPreference() {
        final CustomPreference mVersion = (CustomPreference) findPreference("prefs_version");
        if (mVersion != null) {
            String title;
            String summary;
            try {
                final PackageManager pm = Application.get().getPackageManager();
                if (pm != null) {
                    final PackageInfo pInfo = pm.getPackageInfo(getActivity().getPackageName(), 0);
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
    }

    @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mShowPollfish == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(getActivity()).showPollfish = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());

            if (value) {
                PollFish.show();
            } else {
                PollFish.hide();
            }
            mShowPollfish.setChecked(value);
            return true;
        } else if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(getActivity()).extensiveLogging = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());

            Logger.setEnabled(value);
            mExtensiveLogging.setChecked(value);
            return true;
        } else if (mShowLauncher == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(getActivity()).showLauncher = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());

            Application.get().toggleLauncherIcon(value);
            mShowLauncher.setChecked(value);
            return true;
        } else if (mSkipChecks == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(getActivity()).skipChecks = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());

            mSkipChecks.setChecked(value);
            return true;
        } else if (mMonkeyPref == preference) {
            final boolean value = (Boolean) newValue;
            DeviceConfiguration.get(getActivity()).monkey = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());
            mMonkeyPref.setChecked(value);
            return true;
        } else if (mSwipeOnContent == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(getActivity()).swipeOnContent = value;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());

            mSwipeOnContent.setChecked(value);

            // update the menu
            MainActivity.setSwipeOnContent(value);
            return true;
        } else if (mDarkTheme == preference) {
            final boolean isDark = (Boolean) newValue;
            Application.get().setDarkTheme(isDark);
            mDarkTheme.setChecked(isDark);

            if (isDark) {
                Application.get().setAccentColor(getResources().getColor(R.color.accent));
            } else {
                Application.get().setAccentColor(getResources().getColor(R.color.accent_light));
            }

            // restart the activity to apply new theme
            Utils.restartActivity(getActivity());
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (mSetOnBoot == preference) {
            new SobDialogFragment()
                    .show(getActivity().getSupportFragmentManager(), "sob_dialog_fragment");
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
