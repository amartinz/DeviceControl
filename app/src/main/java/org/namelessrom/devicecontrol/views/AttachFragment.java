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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.animation.Animation;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.listeners.OnSectionAttachedListener;
import org.namelessrom.devicecontrol.utils.AppHelper;

public abstract class AttachFragment extends Fragment implements OnBackPressedListener {

    /** @return The fragment id */
    protected int getFragmentId() {
        return -1;
    }

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSectionAttachedListener) {
            ((OnSectionAttachedListener) activity).onSectionAttached(getFragmentId());
        }
    }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Activity activity = getActivity();
        if (activity instanceof ActivityCallbacks) {
            ((ActivityCallbacks) activity).closeDrawerIfShowing();
        }
    }

    @Override public void onResume() {
        super.onResume();
        final Activity activity = getActivity();
        if (!AppHelper.preventOnResume && activity instanceof MainActivity) {
            ((MainActivity) activity).setFragment(this);
        }
        if (activity instanceof ActivityCallbacks) {
            ((ActivityCallbacks) activity).shouldLoadFragment(getFragmentId(), true);
        }

        checkMenuItem();
    }

    @Override public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (MainActivity.sDisableFragmentAnimations) {
            final Animation a = new Animation() { };
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    /*
         * whether we should show the burger on a back action.
         * defaults to true, set to false to show an arrow
         * possible use case when to set it to false: sub fragment navigation
         */
    @Override public boolean showBurger() { return true; }

    @Override public boolean onBackPressed() { return false; }

    @Nullable public final BaseActivity getBaseActivity() {
        final Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return (BaseActivity) activity;
        }
        return null;
    }

    private void checkMenuItem() {
        final int menuItemId = getFragmentId();
        if (menuItemId != -1) {
            final BaseActivity activity = getBaseActivity();
            if (activity != null) {
                activity.checkMenuItem(menuItemId);
            }
        }
    }

}
