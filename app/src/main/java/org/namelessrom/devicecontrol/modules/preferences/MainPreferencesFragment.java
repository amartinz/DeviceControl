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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;
import butterknife.Bind;
import butterknife.ButterKnife;

public class MainPreferencesFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    // TODO: more customization
    @Bind(R.id.prefs_light_theme) MaterialSwitchPreference mLightTheme;
    @Bind(R.id.prefs_low_end_gfx) MaterialSwitchPreference mLowEndGfx;

    @Bind(R.id.prefs_show_pollfish) MaterialSwitchPreference mShowPollfish;

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_app_main;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mLightTheme.setChecked(AppResources.get().isLightTheme());
        mLightTheme.setOnPreferenceChangeListener(this);

        mLowEndGfx.setChecked(AppResources.get().isLowEndGfx(getContext()));
        mLowEndGfx.setOnPreferenceChangeListener(this);

        final DeviceConfig configuration = DeviceConfig.get();
        mShowPollfish.setChecked(configuration.showPollfish);
        mShowPollfish.setOnPreferenceChangeListener(this);
    }

    @SuppressLint("CommitPrefEdits")
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
        } else if (mLightTheme == preference) {
            final boolean isLight = (Boolean) newValue;
            AppResources.get().setLightTheme(isLight);
            mLightTheme.setChecked(isLight);

            if (isLight) {
                AppResources.get().setAccentColor(ContextCompat.getColor(getActivity(), R.color.accent_light));
            } else {
                AppResources.get().setAccentColor(ContextCompat.getColor(getActivity(), R.color.accent));

            }

            // restart the activity to apply new theme
            Utils.restartActivity(getActivity());
            return true;
        } else if (mLowEndGfx == preference) {
            final boolean isLowEndGfx = (Boolean) newValue;
            AppResources.get().setLowEndGfx(isLowEndGfx);
            mLowEndGfx.setChecked(isLowEndGfx);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            prefs.edit().putBoolean(Constants.KEY_LOW_END_GFX, isLowEndGfx).commit();

            // restart the activity to update effects
            Utils.restartActivity(getActivity());
            return true;
        }

        return false;
    }

}
