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
package org.namelessrom.devicecontrol.modules.bootup;

import android.os.Bundle;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.materialpreferences.MaterialListPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;

public class MainBootupFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {
    private MaterialSwitchPreference mBootupEnabled;

    private MaterialPreferenceCategory mCatAutomatedRestoration;
    private MaterialSwitchPreference mAutomatedRestorationEnabled;
    private MaterialListPreference mAutomatedRestorationDelay;

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_bootup_restoration_main;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final BootupConfig configuration = BootupConfig.get();

        mBootupEnabled = (MaterialSwitchPreference) view.findViewById(R.id.prefs_bootup_enabled);
        mBootupEnabled.setBackgroundColor(AppResources.getColor(getContext(), R.color.cardview_background));
        mBootupEnabled.setChecked(configuration.isEnabled);
        mBootupEnabled.setOnPreferenceChangeListener(this);

        mCatAutomatedRestoration =
                (MaterialPreferenceCategory) view.findViewById(R.id.cat_bootup_automated);
        mAutomatedRestorationEnabled =
                (MaterialSwitchPreference) view.findViewById(R.id.prefs_bootup_automated_enabled);
        mAutomatedRestorationEnabled.setChecked(configuration.isAutomatedRestoration);
        mAutomatedRestorationEnabled.setOnPreferenceChangeListener(this);

        mAutomatedRestorationDelay =
                (MaterialListPreference) view.findViewById(R.id.prefs_bootup_automated_delay);
        mAutomatedRestorationDelay
                .setValue(String.valueOf(configuration.automatedRestorationDelay));
        mAutomatedRestorationDelay.setOnPreferenceChangeListener(this);

        updateCategories();
    }

    private void updateCategories() {
        mCatAutomatedRestoration.setEnabled(mBootupEnabled.isChecked());

        boolean automatedRestoration = mAutomatedRestorationEnabled.isChecked();
        mAutomatedRestorationEnabled.setSummary(automatedRestoration
                ? getString(R.string.automated_restoration_on)
                : getString(R.string.automated_restoration_off));

        mAutomatedRestorationDelay.setEnabled(automatedRestoration);
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object o) {
        final BootupConfig configuration = BootupConfig.get();
        boolean handled = false;

        if (mBootupEnabled == preference) {
            boolean value = (Boolean) o;
            configuration.isEnabled = value;
            mBootupEnabled.setChecked(value);
            handled = true;
        } else if (mAutomatedRestorationEnabled == preference) {
            boolean value = (Boolean) o;
            configuration.isAutomatedRestoration = value;
            mAutomatedRestorationEnabled.setChecked(value);
            handled = true;
        } else if (mAutomatedRestorationDelay == preference) {
            int value;
            try {
                value = Utils.parseInt(String.valueOf(o), 0);
            } catch (NumberFormatException nfe) {
                value = 0;
            }
            configuration.automatedRestorationDelay = value;
            handled = true;
        }

        if (handled) {
            configuration.save();
            updateCategories();
            return true;
        }
        return false;
    }
}
