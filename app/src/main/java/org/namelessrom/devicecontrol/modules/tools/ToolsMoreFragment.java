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
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.IOUtils;

import alexander.martinz.libs.execution.RootShell;

public class ToolsMoreFragment extends AttachPreferenceFragment {
    private CustomPreference mMediaScan;

    private CustomPreference mBuildProp;
    private CustomPreference mSysctlVm;
    private CustomPreference mWirelessFileManager;

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_MORE; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tools_more);

        mMediaScan = (CustomPreference) findPreference("media_scan");

        mWirelessFileManager = (CustomPreference) findPreference("wireless_file_manager");

        mBuildProp = (CustomPreference) findPreference("build_prop");
        mSysctlVm = (CustomPreference) findPreference("sysctl_vm");
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen prefScreen, @NonNull final Preference preference) {
        if (mMediaScan == preference) {
            startMediaScan();
            return true;
        }

        final int id;
        if (mWirelessFileManager == preference) {
            id = DeviceConstants.ID_TOOLS_WIRELESS_FM;
        } else if (mBuildProp == preference) {
            id = DeviceConstants.ID_TOOLS_EDITORS_BUILD_PROP;
        } else if (mSysctlVm == preference) {
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

        return super.onPreferenceTreeClick(prefScreen, preference);
    }

    private void startMediaScan() {
        final String format = "am broadcast -a android.intent.action.MEDIA_MOUNTED -d file://%s;";
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format(format, IOUtils.get().getPrimarySdCard()));
        if (!TextUtils.isEmpty(IOUtils.get().getSecondarySdCard())) {
            sb.append(String.format(format, IOUtils.get().getSecondarySdCard()));
        }
        RootShell.fireAndForget(sb.toString());

        final View view = tryToGetAnyView();
        if (view != null) {
            Snackbar.make(view, R.string.media_scan_triggered, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), R.string.media_scan_triggered, Toast.LENGTH_LONG).show();
        }
    }

}
