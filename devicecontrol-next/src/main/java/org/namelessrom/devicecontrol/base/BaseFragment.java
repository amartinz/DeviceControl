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

package org.namelessrom.devicecontrol.base;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.animation.Animation;

public abstract class BaseFragment extends Fragment {
    public static boolean sDisableFragmentAnimations;

    /** @return true when handled, false when not handled */
    public boolean onActionBarHomeClicked() {
        return false;
    }

    /** @return true when handled, false when not handled */
    public boolean onBackPressed() {
        return false;
    }

    @Override public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (BaseFragment.sDisableFragmentAnimations) {
            final Animation a = new Animation() { };
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override public void onResume() {
        super.onResume();

        final BaseActivity activity = getBaseActivity();
        if (activity != null) {
            activity.setCurrentFragment(this);
        }

        // recheck menu item in drawer if id != -1
        checkMenuItem();
    }


    @Nullable public final BaseActivity getBaseActivity() {
        final Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return (BaseActivity) activity;
        }
        return null;
    }

    private void checkMenuItem() {
        final int menuItemId = getMenuItemId();
        if (menuItemId != -1) {
            final BaseActivity activity = getBaseActivity();
            if (activity != null) {
                activity.checkMenuItem(menuItemId);
            }
        }
    }

    protected int getMenuItemId() {
        return -1;
    }

}
