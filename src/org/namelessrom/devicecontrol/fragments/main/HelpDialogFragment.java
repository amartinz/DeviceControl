/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.devicecontrol.fragments.main;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.dialogs.DeviceHelpDialog;

/**
 * Created by alex on 21.01.14.
 */
public class HelpDialogFragment extends DialogFragment {

    public static final String TAG = "dialog_fragment_help";

    private FragmentTabHost mTabHost;

    public HelpDialogFragment() {
        // Empty Constructor
    }

    public static HelpDialogFragment newInstance() {
        return new HelpDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {

        View v = layoutInflater.inflate(R.layout.fragment_tabhost, viewGroup, false);

        mTabHost = (FragmentTabHost) v.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("about")
                .setIndicator(getString(R.string.action_about)),
                DeviceHelpDialog.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("help")
                .setIndicator(getString(R.string.action_help)),
                DeviceHelpDialog.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("licenses")
                .setIndicator(getString(R.string.action_licenses)),
                DeviceHelpDialog.class, null);


        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        Dialog d = getDialog();
        d.setTitle(R.string.action_help);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }
}
