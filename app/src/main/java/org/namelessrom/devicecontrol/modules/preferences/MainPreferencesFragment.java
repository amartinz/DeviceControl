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
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.pollfish.main.PollFish;
import com.sense360.android.quinoa.lib.Sense360;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.theme.AppResources;

import alexander.martinz.libs.execution.ShellManager;
import alexander.martinz.libs.materialpreferences.MaterialListPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;
import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainPreferencesFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    // TODO: more customization
    @Bind(R.id.prefs_light_theme) MaterialSwitchPreference lightTheme;
    @Bind(R.id.prefs_low_end_gfx) MaterialSwitchPreference lowEndGfx;

    @Bind(R.id.prefs_show_pollfish) MaterialSwitchPreference showPollfish;
    @Bind(R.id.prefs_use_sense360) MaterialSwitchPreference useSense360;

    @Bind(R.id.prefs_expert_enable) MaterialSwitchPreference expertEnable;
    @Bind(R.id.prefs_expert_skip_checks) MaterialSwitchPreference skipChecks;
    @Bind(R.id.prefs_expert_su_shell_context) MaterialListPreference shellContext;

    @Override protected int getLayoutResourceId() {
        return R.layout.pref_app_main;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        final Context context = getContext();
        final DeviceConfig configuration = DeviceConfig.get();

        lightTheme.setChecked(AppResources.get().isLightTheme());
        lightTheme.setOnPreferenceChangeListener(this);

        lowEndGfx.setChecked(AppResources.get().isLowEndGfx(context));
        lowEndGfx.setOnPreferenceChangeListener(this);

        showPollfish.setChecked(configuration.showPollfish);
        showPollfish.setOnPreferenceChangeListener(this);

        useSense360.setChecked(!Sense360.isUserOptedOut(getContext().getApplicationContext()));
        useSense360.setOnPreferenceChangeListener(this);
        useSense360.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                final Activity activity = getActivity();
                App.get(activity).getCustomTabsHelper().launchUrl(activity, Constants.URL_SENSE360);
                return true;
            }
        });

        expertEnable.setChecked(configuration.expertMode);
        expertEnable.setOnPreferenceChangeListener(this);

        skipChecks.setChecked(configuration.skipChecks);
        skipChecks.setOnPreferenceChangeListener(this);

        // TODO: investigate if needed
        /*
        shellContext.setValue(configuration.suShellContext);
        String summary = getString(R.string.su_shell_context_summary, getString(R.string.normal), shellContext.getValue());
        shellContext.setSummary(summary);
        shellContext.setValue(configuration.suShellContext);
        shellContext.setOnPreferenceChangeListener(this);
        */

        updateExpertVisibility(configuration.expertMode);
    }

    @SuppressLint("CommitPrefEdits")
    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
        if (showPollfish == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfig.get().showPollfish = value;
            DeviceConfig.get().save();

            if (value) {
                PollFish.show();
            } else {
                PollFish.hide();
            }
            showPollfish.setChecked(value);
            return true;
        } else if (lightTheme == preference) {
            final boolean isLight = (Boolean) newValue;
            AppResources.get().setLightTheme(isLight);
            lightTheme.setChecked(isLight);

            if (isLight) {
                AppResources.get().setAccentColor(ContextCompat.getColor(getActivity(), R.color.accent_light));
            } else {
                AppResources.get().setAccentColor(ContextCompat.getColor(getActivity(), R.color.accent));
            }

            if (getActivity() instanceof PreferencesActivity) {
                ((PreferencesActivity) getActivity()).needsRestart();
            }
            return true;
        } else if (lowEndGfx == preference) {
            final boolean isLowEndGfx = (Boolean) newValue;
            AppResources.get().setLowEndGfx(isLowEndGfx);
            lowEndGfx.setChecked(isLowEndGfx);

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
            prefs.edit().putBoolean(Constants.KEY_LOW_END_GFX, isLowEndGfx).commit();

            if (getActivity() instanceof PreferencesActivity) {
                ((PreferencesActivity) getActivity()).needsRestart();
            }
            return true;
        } else if (useSense360 == preference) {
            final boolean useSense360 = (Boolean) newValue;

            final Context applicationContext = getContext().getApplicationContext();
            if (useSense360) {
                Timber.v("Starting Sense360");
                Sense360.userOptIn(applicationContext);
                Sense360.start(applicationContext);
            } else {
                Timber.v("Stopping Sense360");
                Sense360.userOptOut(applicationContext);
                Sense360.stop(applicationContext);
            }

            return true;
        }

        final DeviceConfig deviceConfig = DeviceConfig.get();
        if (expertEnable == preference) {
            final boolean value = (Boolean) newValue;

            deviceConfig.expertMode = value;
            deviceConfig.save();

            expertEnable.setChecked(value);
            updateExpertVisibility(deviceConfig.expertMode);
            return true;
        } else if (skipChecks == preference) {
            final boolean value = (Boolean) newValue;

            deviceConfig.skipChecks = value;
            deviceConfig.save();

            skipChecks.setChecked(value);
            return true;
        } else if (shellContext == preference) {
            final String value = String.valueOf(newValue);

            final String summary = getString(R.string.su_shell_context_summary, getString(R.string.normal), value);
            shellContext.setSummary(summary);

            deviceConfig.suShellContext = value;
            deviceConfig.save();

            // reopen shells to switch context
            Timber.v("reopening shells");
            ShellManager.get().cleanupShells();
            return true;
        }

        return false;
    }

    private void updateExpertVisibility(boolean isExpertMode) {
        final int visibility = (isExpertMode ? View.VISIBLE : View.GONE);
        skipChecks.setVisibility(visibility);
        // TODO: investigate if needed
        shellContext.setVisibility(View.GONE);
    }

}
