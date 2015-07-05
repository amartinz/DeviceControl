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

import android.os.Bundle;
import android.view.View;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.theme.AppResources;

import alexander.martinz.libs.materialpreferences.MaterialListPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;

public class AdvancedPreferencesFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    private MaterialSwitchPreference mSkipChecks;

    private MaterialListPreference mShellContext;

    private MaterialSwitchPreference mDebugStrictMode;
    private MaterialSwitchPreference mExtensiveLogging;

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_app_device_control_advanced;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final DeviceConfig configuration = DeviceConfig.get();

        mSkipChecks = (MaterialSwitchPreference) view.findViewById(R.id.prefs_skip_checks);
        mSkipChecks.setBackgroundColor(AppResources.get().getCardBackgroundColor());
        mSkipChecks.setChecked(configuration.skipChecks);
        mSkipChecks.setOnPreferenceChangeListener(this);

        mShellContext = (MaterialListPreference) view.findViewById(R.id.prefs_su_shell_context);
        mShellContext.setValue(configuration.suShellContext);
        String summary = getString(R.string.su_shell_context_summary,
                getString(R.string.normal), mShellContext.getValue());
        mShellContext.setSummary(summary);
        mShellContext.setValue(configuration.suShellContext);
        mShellContext.setOnPreferenceChangeListener(this);

        mDebugStrictMode =
                (MaterialSwitchPreference) view.findViewById(R.id.prefs_debug_strict_mode);
        mDebugStrictMode.setChecked(configuration.debugStrictMode);
        mDebugStrictMode.setOnPreferenceChangeListener(this);

        mExtensiveLogging =
                (MaterialSwitchPreference) view.findViewById(R.id.prefs_extensive_logging);
        mExtensiveLogging.setChecked(configuration.extensiveLogging);
        mExtensiveLogging.setOnPreferenceChangeListener(this);
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
        final DeviceConfig deviceConfig = DeviceConfig.get();

        if (mSkipChecks == preference) {
            final boolean value = (Boolean) newValue;

            deviceConfig.skipChecks = value;
            deviceConfig.save();

            mSkipChecks.setChecked(value);
            return true;
        } else if (mShellContext == preference) {
            final String value = String.valueOf(newValue);

            String summary = getString(R.string.su_shell_context_summary,
                    getString(R.string.normal), value);
            mShellContext.setSummary(summary);

            deviceConfig.suShellContext = value;
            deviceConfig.save();

            // reopen shells to switch context
            reopenShells();

            return true;
        } else if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;

            deviceConfig.extensiveLogging = value;
            deviceConfig.save();

            Logger.setEnabled(value);
            mExtensiveLogging.setChecked(value);
            return true;
        } else if (mDebugStrictMode == preference) {
            final boolean value = (Boolean) newValue;

            deviceConfig.debugStrictMode = value;
            deviceConfig.save();

            Logger.setStrictModeEnabled(value);
            mDebugStrictMode.setChecked(value);
            return true;
        }

        return false;
    }

    private void reopenShells() {
        Logger.i(this, "reopening shells");
        try {
            RootTools.closeAllShells();
        } catch (Exception e) {
            Logger.e(this, String.format("reopenShells() -> %s", e));
        }
    }

}
