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

package org.namelessrom.devicecontrol;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.namelessrom.devicecontrol.base.BaseActivity;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.modules.home.HomeFragment;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    // work around the support library bug
    private MenuItem mPreviousMenuItem;

    private BaseFragment mCurrentFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // lock the drawer so we can only open it AFTER we are done with our checks
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        final ImageView drawerHeaderSettings = (ImageView) mDrawerLayout.findViewById(R.id.drawer_header_settings);
        drawerHeaderSettings.setOnClickListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        setupDrawerItems();

        // TODO: setup first launch, root checks etc
        unlockMenu();
        replaceFragment(new HomeFragment(), -1, null);
    }

    @Override public void onBackPressed() {
        if (mCurrentFragment != null && mCurrentFragment.onBackPressed()) {
            return;
        }
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                if (mCurrentFragment != null && mCurrentFragment.onActionBarHomeClicked()) {
                    return true;
                }
                // only open drawer as it is ABOVE the toolbar and we can not close it that way
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerItems() {
        // manually check home drawer entry
        mPreviousMenuItem = findMenuItem(R.id.nav_item_home);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(true);
        }
    }

    @Nullable private MenuItem findMenuItem(final int menuId) {
        if (mNavigationView == null || mNavigationView.getMenu() == null) {
            return null;
        }
        return mNavigationView.getMenu().findItem(menuId);
    }

    /** Searches and checks the {@link MenuItem} if found **/
    public void checkMenuItem(final int menuId) {
        final MenuItem menuItem = findMenuItem(menuId);
        if (menuItem != null) {
            checkMenuItem(menuItem);
        }
    }

    private void checkMenuItem(@NonNull final MenuItem menuItem) {
        // to work around a bug in the design library we need to store the menu item and
        // uncheck it manually everytime we check a new item
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(false);
        }
        menuItem.setChecked(true);
        mPreviousMenuItem = menuItem;

        mNavigationView.invalidate();
    }

    public void replaceFragment(BaseFragment fragment, @MenuRes int menuId, String backStackTag) {
        if (menuId != -1) {
            checkMenuItem(menuId);
        }

        mCurrentFragment = fragment;
        final FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.fragment_container, mCurrentFragment);
        if (backStackTag != null) {
            trans.addToBackStack(backStackTag);
        }
        trans.commit();
    }

    public void unlockMenu() {
        final ActionBar actionBar = getSupportActionBar();
        assert (actionBar != null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
        return false;
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.drawer_header_settings: {
                break;
            }
        }
    }
}
