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
package org.namelessrom.devicecontrol.modules.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.namelessrom.devicecontrol.DCApplication;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.utils.AppHelper;

public class CommunityFragment extends BaseFragment {
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_community, container, false);

        final Button betaButton = (Button) view.findViewById(R.id.betaTestButton);
        betaButton.setOnClickListener(v -> AppHelper.launchUrlViaTabs(getActivity(), getString(R.string.non_beta_url)));

        final Button communityButton = (Button) view.findViewById(R.id.communityButton);
        communityButton.setOnClickListener(
                v -> AppHelper.launchUrlViaTabs(getActivity(), getString(R.string.non_google_plus_community_url)));

        DCApplication.get(getContext()).getCustomTabsHelper().warmup();

        return view;
    }
}
