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

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.tools.ToolsEditor;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class PressToLoadFragment extends Fragment implements DeviceConstants {

    public static final String ARG_FRAGMENT = "arg_fragment";
    public static final String ARG_IMG = "arg_img";

    private Fragment fragment;
    private String mText;
    private int mImgId = R.mipmap.ic_launcher;

    public static PressToLoadFragment newInstance(int fragmentId, int imgId) {
        PressToLoadFragment f = new PressToLoadFragment();

        Bundle b = new Bundle();
        b.putInt(PressToLoadFragment.ARG_FRAGMENT, fragmentId);
        b.putInt(PressToLoadFragment.ARG_IMG, imgId);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        final int fragmentId = getArguments().getInt(ARG_FRAGMENT);
        mImgId = getArguments().getInt(ARG_IMG);

        switch (fragmentId) {
            case 0:
                fragment = ToolsEditor.newInstance(0);
                mText = getString(R.string.fragment_press_to_load, "VM Editor");
                break;
            case 1:
                fragment = ToolsEditor.newInstance(1);
                mText = getString(R.string.fragment_press_to_load, "SysCtl Editor");
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
        replaceFragment(new ReplaceFragment());
    }

    private void replaceFragment(Fragment f) {
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                        R.anim.card_flip_left_in, R.anim.card_flip_left_out)
                .replace(R.id.container_ptl, f)
                .addToBackStack(null)
                .commit();
    }

    private class ReplaceFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_press_to_load, container, false);

            TextView tvHelp = (TextView) view.findViewById(R.id.help_textview);
            tvHelp.setText(mText);

            ImageView ivHelp = (ImageView) view.findViewById(R.id.help_imageview);
            ivHelp.setImageResource(mImgId);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    replaceFragment(fragment);
                }
            });

            return view;
        }
    }

}
