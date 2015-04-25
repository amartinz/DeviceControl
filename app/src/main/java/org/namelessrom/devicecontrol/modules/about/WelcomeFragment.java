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
package org.namelessrom.devicecontrol.modules.about;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.DonationActivity;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public class WelcomeFragment extends Fragment {
    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);

        final TextView details = (TextView) view.findViewById(R.id.detailsTxtView);
        details.setText(getString(R.string.welcome_message, getString(R.string.app_name)));

        final Drawable translateDrawable = DrawableHelper.getDrawable(R.drawable.ic_translate);
        final Button translateButton = (Button) view.findViewById(R.id.translateButton);
        translateButton.setCompoundDrawablesWithIntrinsicBounds(
                DrawableHelper.applyAccentColorFilter(translateDrawable), null, null, null);
        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                AppHelper.viewInBrowser("https://translate.nameless-rom.org");
            }
        });

        final Button donateButton = (Button) view.findViewById(R.id.donateButton);
        donateButton.setText(
                String.format("%s %s", getString(R.string.donate_), getString(R.string.heart)));
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Intent intent = new Intent(getActivity(), DonationActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
