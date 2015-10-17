/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.modules.more;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.modules.home.DonationActivity;
import org.namelessrom.devicecontrol.views.AboutCardView;

public class AboutFragment extends BaseFragment {
    @Override protected int getMenuItemId() {
        return R.id.nav_item_more_about;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        final AboutCardView aboutCardView = (AboutCardView) view.findViewById(R.id.about_card_view);
        aboutCardView.setupWithActivity(getActivity());

        view.findViewById(R.id.about_donate).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), DonationActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

}
