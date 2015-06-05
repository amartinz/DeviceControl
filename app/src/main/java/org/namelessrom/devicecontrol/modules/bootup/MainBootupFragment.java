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
import android.support.v4.app.FragmentManager;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.preferences.SobDialogFragment;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class MainBootupFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener {
    private MaterialPreference mSetOnBoot;

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_bootup_restoration_main;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSetOnBoot = (MaterialPreference) view.findViewById(R.id.prefs_set_on_boot);
        mSetOnBoot.setOnPreferenceClickListener(this);
    }

    @Override public boolean onPreferenceClicked(MaterialPreference preference) {
        if (mSetOnBoot == preference) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            new SobDialogFragment().show(fragmentManager, "sob_dialog_fragment");
            return true;
        }

        return false;
    }
}
