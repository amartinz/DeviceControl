/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.preferences;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.MainActivityCallbacks;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;

public class MainPreferencesFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    private MaterialSwitchPreference mSwipeOnContent;
    // TODO: more customization
    private MaterialSwitchPreference mDarkTheme;

    private MaterialSwitchPreference mShowPollfish;

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_app_device_control_main;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final DeviceConfig configuration = DeviceConfig.get();

        mDarkTheme = (MaterialSwitchPreference) view.findViewById(R.id.prefs_dark_theme);
        mDarkTheme.setChecked(AppResources.get().isDarkTheme());
        mDarkTheme.setOnPreferenceChangeListener(this);

        mSwipeOnContent = (MaterialSwitchPreference) view.findViewById(R.id.prefs_swipe_on_content);
        mSwipeOnContent.setChecked(configuration.swipeOnContent);
        mSwipeOnContent.setOnPreferenceChangeListener(this);

        mShowPollfish = (MaterialSwitchPreference) view.findViewById(R.id.prefs_show_pollfish);
        mShowPollfish.setChecked(configuration.showPollfish);
        mShowPollfish.setOnPreferenceChangeListener(this);

        setupVersionPreference(view);
    }

    private void setupVersionPreference(View view) {
        MaterialPreference version = (MaterialPreference) view.findViewById(R.id.prefs_version);
        if (version != null) {
            version.setBackgroundColor(AppResources.get().getCardBackgroundColor());

            String title;
            String summary;
            try {
                final PackageManager pm = Application.get().getPackageManager();
                if (pm != null) {
                    final PackageInfo pInfo = pm.getPackageInfo(getActivity().getPackageName(), 0);
                    title = getString(R.string.app_version_name, pInfo.versionName);
                    summary = getString(R.string.app_version_code, pInfo.versionCode);
                } else {
                    throw new Exception("pm is null");
                }
            } catch (Exception ignored) {
                title = getString(R.string.app_version_name, getString(R.string.unknown));
                summary = getString(R.string.app_version_code, getString(R.string.unknown));
            }
            version.setTitle(title);
            version.setSummary(summary);
        }
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
        if (mShowPollfish == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfig.get().showPollfish = value;
            DeviceConfig.get().save();

            if (value) {
                PollFish.show();
            } else {
                PollFish.hide();
            }
            mShowPollfish.setChecked(value);
            return true;
        } else if (mSwipeOnContent == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfig.get().swipeOnContent = value;
            DeviceConfig.get().save();

            mSwipeOnContent.setChecked(value);

            // update the menu
            final Activity activity = getActivity();
            if (activity instanceof MainActivityCallbacks) {
                ((MainActivityCallbacks) activity).setSwipeOnContent(value);
            }
            return true;
        } else if (mDarkTheme == preference) {
            final boolean isDark = (Boolean) newValue;
            AppResources.get().setDarkTheme(isDark);
            mDarkTheme.setChecked(isDark);

            if (isDark) {
                AppResources.get().setAccentColor(getResources().getColor(R.color.accent));
            } else {
                AppResources.get().setAccentColor(getResources().getColor(R.color.accent_light));
            }

            // restart the activity to apply new theme
            Utils.restartActivity(getActivity());
            return true;
        }

        return false;
    }

}
