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
import org.namelessrom.devicecontrol.ui.fragments.SobDialogFragment;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class MainPreferencesFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, DeviceConstants {

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

        mExtensiveLogging = (CustomTogglePreference) findPreference(EXTENSIVE_LOGGING);
        if (mExtensiveLogging != null) {
            mExtensiveLogging.setChecked(PreferenceHelper.getBoolean(EXTENSIVE_LOGGING));
            mExtensiveLogging.setOnPreferenceChangeListener(this);
        }

        PreferenceCategory category = (PreferenceCategory) findPreference("prefs_general");
        mSetOnBoot = (CustomPreference) findPreference("prefs_set_on_boot");

        mShowLauncher = (CustomTogglePreference) findPreference(SHOW_LAUNCHER);
        if (mShowLauncher != null) {
            if (Application.IS_NAMELESS) {
                mShowLauncher.setChecked(PreferenceHelper.getBoolean(SHOW_LAUNCHER, true));
                mShowLauncher.setOnPreferenceChangeListener(this);
            } else {
                category.removePreference(mShowLauncher);
            }
        }

        mSkipChecks = (CustomTogglePreference) findPreference(SKIP_CHECKS);
        if (mSkipChecks != null) {
            mSkipChecks.setChecked(PreferenceHelper.getBoolean(SKIP_CHECKS));
            mSkipChecks.setOnPreferenceChangeListener(this);
        }

        category = (PreferenceCategory) findPreference("prefs_app");

        if (Utils.existsInFile(Scripts.BUILD_PROP, "ro.nameless.secret=1")) {
            mMonkeyPref = new CustomTogglePreference(getActivity());
            mMonkeyPref.setKey("monkey");
            mMonkeyPref.setTitle(R.string.become_a_monkey);
            mMonkeyPref.setSummaryOn(R.string.is_monkey);
            mMonkeyPref.setSummaryOff(R.string.no_monkey);
            mMonkeyPref.setChecked(PreferenceHelper.getBoolean("monkey", false));
            mMonkeyPref.setOnPreferenceChangeListener(this);
            category.addPreference(mMonkeyPref);
        }

        mDarkTheme = (CustomTogglePreference) findPreference("dark_theme");
        mDarkTheme.setChecked(PreferenceHelper.getBoolean(mDarkTheme.getKey(), false));
        mDarkTheme.setOnPreferenceChangeListener(this);

        mSwipeOnContent = (CustomTogglePreference) findPreference("swipe_on_content");
        mSwipeOnContent.setChecked(PreferenceHelper.getBoolean(mSwipeOnContent.getKey()));
        mSwipeOnContent.setOnPreferenceChangeListener(this);

        mShowPollfish = (CustomTogglePreference) findPreference("show_pollfish");
        mShowPollfish.setChecked(PreferenceHelper.getBoolean(mShowPollfish.getKey(), false));
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
            PreferenceHelper.setBoolean(mShowPollfish.getKey(), value);
            if (value) {
                PollFish.show();
            } else {
                PollFish.hide();
            }
            mShowPollfish.setChecked(value);
            return true;
        } else if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(EXTENSIVE_LOGGING, value);
            Logger.setEnabled(value);
            mExtensiveLogging.setChecked(value);
            return true;
        } else if (mShowLauncher == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SHOW_LAUNCHER, value);
            Application.get().toggleLauncherIcon(value);
            mShowLauncher.setChecked(value);
            return true;
        } else if (mSkipChecks == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(SKIP_CHECKS, value);
            mSkipChecks.setChecked(value);
            return true;
        } else if (mMonkeyPref == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean("monkey", value);
            mMonkeyPref.setChecked(value);
            return true;
        } else if (mSwipeOnContent == preference) {
            final boolean value = (Boolean) newValue;
            PreferenceHelper.setBoolean(mSwipeOnContent.getKey(), value);
            mSwipeOnContent.setChecked(value);

            // update the menu
            MainActivity.setSwipeOnContent(value);
            return true;
        } else if (mDarkTheme == preference) {
            final boolean isDark = (Boolean) newValue;
            Application.get().setDarkTheme(isDark);
            PreferenceHelper.setBoolean(mDarkTheme.getKey(), isDark);
            mDarkTheme.setChecked(isDark);

            if (isDark) {
                Application.get().setAccentColor(getResources().getColor(R.color.accent));
            } else {
                Application.get().setAccentColor(getResources().getColor(R.color.accent_light));
            }
            PreferenceHelper.setInt("pref_color", Application.get().getAccentColor());

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
