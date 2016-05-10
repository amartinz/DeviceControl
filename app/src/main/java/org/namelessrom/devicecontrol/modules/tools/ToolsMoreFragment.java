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
package org.namelessrom.devicecontrol.modules.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.receivers.ActionReceiver;
import org.namelessrom.devicecontrol.utils.AppHelper;

import at.amartinz.execution.BusyBox;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ToolsMoreFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceClickListener, MaterialPreference.MaterialPreferenceChangeListener {
    @BindView(R.id.pref_wireless_file_manager) MaterialPreference wirelessFileManager;

    @BindView(R.id.pref_buildprop) MaterialPreference buildProp;
    @BindView(R.id.pref_sysctl_vm) MaterialPreference sysctlVm;

    @BindView(R.id.pref_media_scan) MaterialPreference mediaScan;
    @BindView(R.id.pref_quick_actions) MaterialSwitchPreference quickActions;

    @Override protected int getLayoutResourceId() {
        return R.layout.tools_more;
    }

    public ToolsMoreFragment() { }

    @NonNull @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);

        final Context context = view.getContext().getApplicationContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean quickActionsEnabled = prefs.getBoolean(getString(R.string.key_quick_actions), false);
        if (quickActionsEnabled) {
            ActionReceiver.Notification.showNotification(context);
        }
        quickActions.setChecked(quickActionsEnabled);

        return view;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wirelessFileManager.setOnPreferenceClickListener(this);

        final boolean hasBusyBox = BusyBox.isAvailable();
        buildProp.setEnabled(hasBusyBox);
        buildProp.setOnPreferenceClickListener(this);
        sysctlVm.setEnabled(hasBusyBox);
        sysctlVm.setOnPreferenceClickListener(this);

        mediaScan.setOnPreferenceClickListener(this);
        quickActions.setOnPreferenceChangeListener(this);
    }

    @Override public boolean onPreferenceClicked(MaterialPreference preference) {
        if (mediaScan == preference) {
            AppHelper.startMediaScan(mediaScan, getContext());
            return true;
        }

        final int id;
        if (wirelessFileManager == preference) {
            id = DeviceConstants.ID_TOOLS_WIRELESS_FM;
        } else if (buildProp == preference) {
            id = DeviceConstants.ID_TOOLS_EDITORS_BUILD_PROP;
        } else if (sysctlVm == preference) {
            id = DeviceConstants.ID_TOOLS_VM;
        } else {
            id = Integer.MIN_VALUE;
        }

        if (id != Integer.MIN_VALUE) {
            final Activity activity = getActivity();
            if (activity instanceof ActivityCallbacks) {
                ((ActivityCallbacks) activity).shouldLoadFragment(id);
            }
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
        if (quickActions == preference) {
            final Context context = getContext().getApplicationContext();
            final boolean enabled = (Boolean) newValue;
            if (enabled) {
                ActionReceiver.Notification.showNotification(context);
            } else {
                ActionReceiver.Notification.cancelNotification(context);
            }

            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(getString(R.string.key_quick_actions), enabled)
                    .apply();
            return true;
        }
        return false;
    }
}
