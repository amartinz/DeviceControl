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

import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import com.pollfish.constants.Position;
import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.proprietary.Configuration;

import alexander.martinz.libs.logger.Logger;

public abstract class BaseActivity extends AppCompatActivity {
    protected DrawerLayout mDrawerLayout;
    protected NavigationView mNavigationView;
    protected MenuItem mPreviousMenuItem;

    protected BaseFragment mCurrentFragment;

    @Override protected void onResume() {
        super.onResume();
        if (!Utils.isNext(this) && PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_show_pollfish), true)) {
            final String pfApiKey = Configuration.getPollfishApiKeyDc();
            if (!TextUtils.equals("---", pfApiKey)) {
                Logger.v(this, "PollFish.init()");
                PollFish.init(this, pfApiKey, Position.BOTTOM_RIGHT, 30);
            }
        }
    }

    public void replaceFragment(BaseFragment fragment, String backStackTag) {
        replaceFragment(fragment, backStackTag, true);
    }

    public void replaceFragment(BaseFragment fragment, String backStackTag, boolean shouldCheckMenuItem) {
        replaceFragment(fragment, -1, backStackTag, shouldCheckMenuItem);
    }

    public void replaceFragment(BaseFragment fragment, int menuItemId, String backStackTag, boolean shouldCheckMenuItem) {
        if (shouldCheckMenuItem && menuItemId != -1) {
            checkMenuItem(menuItemId);
        }

        final FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, fragment);
        if (backStackTag != null) {
            trans.addToBackStack(backStackTag);
        }
        trans.commit();
    }

    /** Searches and checks the {@link MenuItem} if found **/
    public void checkMenuItem(final int menuId) {
        final MenuItem menuItem = findMenuItem(menuId);
        if (menuItem != null) {
            checkMenuItem(menuItem);
        }
    }

    /** Checks the {@link MenuItem} **/
    public void checkMenuItem(@NonNull final MenuItem menuItem) {
        // to work around a bug in the design library we need to store the menu item and
        // uncheck it manually everytime we check a new item
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        menuItem.setChecked(true);
        mPreviousMenuItem = menuItem;

        if (mNavigationView != null) {
            mNavigationView.invalidate();
        }
    }

    @Nullable public MenuItem findMenuItem(final int menuId) {
        if (mNavigationView == null || mNavigationView.getMenu() == null) {
            return null;
        }
        return mNavigationView.getMenu().findItem(menuId);
    }

    public void setCurrentFragment(BaseFragment baseFragment) {
        mCurrentFragment = baseFragment;
    }
}
