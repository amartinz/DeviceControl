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
package org.namelessrom.devicecontrol.fragments.dynamic;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.PropModderFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.VmFragment;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class PressToLoadFragment extends Fragment implements DeviceConstants {

    public static final String ARG_FRAGMENT = "arg_fragment";
    public static final String ARG_IMG      = "arg_img";

    public static final int FRAGMENT_VM         = 0;
    public static final int FRAGMENT_BUILD_PROP = 2;

    private int mFragmentId;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mFragmentId = getArguments().getInt(ARG_FRAGMENT);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        return layoutInflater.inflate(R.layout.fragment_ptl, viewGroup, false);
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        Fragment f;
        switch (mFragmentId) {
            case FRAGMENT_VM:
                f = new VmFragment();
                break;
            case FRAGMENT_BUILD_PROP:
                f = new PropModderFragment();
                break;
            default:
                f = null;
                break;
        }
        if (f == null) return;

        onReplaceFragment(f, false);
    }

    private void onReplaceFragment(final Fragment f, final boolean animate) {

        // TODO: fix it up for API 14
        final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out);
        }
        ft.replace(R.id.container_ptl, f)
                .addToBackStack(null)
                .commit();
    }

}
