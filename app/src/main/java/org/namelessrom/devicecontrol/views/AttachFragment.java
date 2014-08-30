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
package org.namelessrom.devicecontrol.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

public class AttachFragment extends Fragment implements OnBackPressedListener {

    protected void onAttach(final Activity activity, final int number) {
        super.onAttach(activity);
        BusProvider.getBus().post(new SectionAttachedEvent(number));
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (MainActivity.sSlidingMenu != null && MainActivity.sSlidingMenu.isMenuShowing()) {
            MainActivity.sSlidingMenu.toggle(true);
        }
    }

    /*
     * whether we should show the burger on a back action.
     * defaults to true, set to false to show an arrow
     * possible use case when to set it to false: sub fragment navigation
     */
    @Override public boolean showBurger() { return true; }

    @Override public boolean onBackPressed() { return false; }

}
