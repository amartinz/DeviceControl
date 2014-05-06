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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.ReplaceFragmentEvent;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.EditorFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.freezer.FreezerFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.PropModderFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.VmFragment;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class PressToLoadFragment extends Fragment implements DeviceConstants {

    public static final String ARG_FRAGMENT = "arg_fragment";
    public static final String ARG_IMG      = "arg_img";

    public static final int FRAGMENT_VM         = 0;
    public static final int FRAGMENT_BUILD_PROP = 2;

    private Fragment mFragment;
    private String   mText;

    private int mImgId = R.mipmap.ic_launcher;
    private int mFragmentId;

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mFragmentId = getArguments().getInt(ARG_FRAGMENT);
        mImgId = getArguments().getInt(ARG_IMG);

        switch (mFragmentId) {
            case 1:
                mFragment = EditorFragment.newInstance(1);
                mText = getString(R.string.fragment_press_to_load, "SysCtl Editor");
                break;
            case 3:
                mFragment = FreezerFragment.newInstance(0, "usr");
                mText = getString(R.string.fragment_press_to_load, "Freezer");
                break;
            case 4:
                mFragment = FreezerFragment.newInstance(1, "usr");
                mText = getString(R.string.fragment_press_to_load, "Unfreezer");
                break;
            default:
                mText = "Could not identify fragment to load";
                break;
        }
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
                f = new ReplaceFragment();
                break;
        }
        onReplaceFragment(new ReplaceFragmentEvent(f, false));
    }

    @Subscribe
    public void onReplaceFragment(final ReplaceFragmentEvent event) {
        if (event == null) { return; }

        final Fragment f = event.getFragment();
        final boolean animate = event.isAnimate();

        final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        if (animate) {
            ft.setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                    R.anim.card_flip_left_in, R.anim.card_flip_left_out);
        }
        ft.replace(R.id.container_ptl, f)
                .addToBackStack(null)
                .commit();
    }

    private class ReplaceFragment extends Fragment {

        @Override
        public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                final Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_press_to_load, container, false);

            final TextView tvHelp = (TextView) view.findViewById(R.id.help_textview);
            tvHelp.setText(mText);

            final ImageView ivHelp = (ImageView) view.findViewById(R.id.help_imageview);
            ivHelp.setImageResource(mImgId);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onReplaceFragment(new ReplaceFragmentEvent(mFragment, true));
                }
            });

            return view;
        }
    }
}
