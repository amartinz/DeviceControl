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

package org.namelessrom.devicecontrol.fragments.tools.sub.editor;

import android.app.Activity;
import android.os.Bundle;
import android.support.v13.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.fragments.dynamic.PressToLoadFragment;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachFragment;

public class EditorTabbedFragment extends AttachFragment implements DeviceConstants {

    public static final String TAG = "tools_editor_tabbed";

    private FragmentTabHost mTabHost;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_TOOLS_EDITORS); }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            default:
                break;
        }

        return false;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        setHasOptionsMenu(true);

        final View v = layoutInflater.inflate(R.layout.fragment_tabhost, viewGroup, false);

        mTabHost = (FragmentTabHost) v.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);

        /* VM EDITOR */
        Bundle b = new Bundle();
        b.putInt(PressToLoadFragment.ARG_FRAGMENT, PressToLoadFragment.FRAGMENT_VM);
        b.putInt(PressToLoadFragment.ARG_IMG, R.mipmap.ic_launcher);

        mTabHost.addTab(mTabHost
                .newTabSpec("vmeditor")
                .setIndicator(getString(R.string.vm))
                , PressToLoadFragment.class, b);

        /* SYSCTL EDITOR */
        b = new Bundle();
        b.putInt(PressToLoadFragment.ARG_FRAGMENT, 1);
        b.putInt(PressToLoadFragment.ARG_IMG, R.mipmap.ic_launcher);

        mTabHost.addTab(mTabHost
                .newTabSpec("sysctl")
                .setIndicator(getString(R.string.sysctl))
                , PressToLoadFragment.class, b);

        /* BUILD.PROP EDITOR */
        b = new Bundle();
        b.putInt(PressToLoadFragment.ARG_FRAGMENT, 2);
        b.putInt(PressToLoadFragment.ARG_IMG, R.mipmap.ic_launcher);

        mTabHost.addTab(mTabHost
                        .newTabSpec("buildprop")
                        .setIndicator(getString(R.string.buildprop)),
                PressToLoadFragment.class, b
        );

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }
}
