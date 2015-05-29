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
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;

public class AdvancedPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    private CustomTogglePreference mSkipChecks;

    //==============================================================================================
    // Shell
    //==============================================================================================
    private CustomListPreference mShellContext;

    //==============================================================================================
    // Debug
    //==============================================================================================
    private CustomTogglePreference mDebugStrictMode;
    private CustomTogglePreference mExtensiveLogging;

    @Override public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.a_device_control_advanced);

        final DeviceConfiguration configuration = DeviceConfiguration.get(getActivity());

        mSkipChecks = (CustomTogglePreference) findPreference(DeviceConfiguration.SKIP_CHECKS);
        if (mSkipChecks != null) {
            mSkipChecks.setChecked(configuration.skipChecks);
            mSkipChecks.setOnPreferenceChangeListener(this);
        }

        mShellContext = (CustomListPreference) findPreference(DeviceConfiguration.SU_SHELL_CONTEXT);
        mShellContext.setValueIndex(mShellContext.findIndexOfValue(configuration.suShellContext));
        String summary = getString(R.string.su_shell_context_summary,
                getString(R.string.normal), mShellContext.getEntry());
        mShellContext.setSummary(summary);
        mShellContext.setValue(configuration.suShellContext);
        mShellContext.setOnPreferenceChangeListener(this);

        mDebugStrictMode = (CustomTogglePreference) findPreference(
                DeviceConfiguration.DEBUG_STRICT_MODE);
        mDebugStrictMode.setChecked(configuration.debugStrictMode);
        mDebugStrictMode.setOnPreferenceChangeListener(this);

        mExtensiveLogging = (CustomTogglePreference) findPreference(
                DeviceConfiguration.EXTENSIVE_LOGGING);
        mExtensiveLogging.setChecked(configuration.extensiveLogging);
        mExtensiveLogging.setOnPreferenceChangeListener(this);
    }

    @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Activity activity = getActivity();

        if (mSkipChecks == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(activity).skipChecks = value;
            DeviceConfiguration.get(activity).saveConfiguration(activity);

            mSkipChecks.setChecked(value);
            return true;
        } else if (mShellContext == preference) {
            final String value = String.valueOf(newValue);

            String summary = getString(R.string.su_shell_context_summary,
                    getString(R.string.normal), value);
            mShellContext.setSummary(summary);

            DeviceConfiguration.get(activity).suShellContext = value;
            DeviceConfiguration.get(activity).saveConfiguration(activity);

            // reopen shells to switch context
            reopenShells();

            return true;
        } else if (mExtensiveLogging == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(activity).extensiveLogging = value;
            DeviceConfiguration.get(activity).saveConfiguration(activity);

            Logger.setEnabled(value);
            mExtensiveLogging.setChecked(value);
            return true;
        } else if (mDebugStrictMode == preference) {
            final boolean value = (Boolean) newValue;

            DeviceConfiguration.get(activity).debugStrictMode = value;
            DeviceConfiguration.get(activity).saveConfiguration(activity);

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
