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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.namelessrom.devicecontrol.base.BaseActivity;
import org.namelessrom.devicecontrol.modules.home.DonationActivity;
import org.namelessrom.devicecontrol.modules.home.HomeFragment;
import org.namelessrom.devicecontrol.modules.info.InfoFragment;
import org.namelessrom.devicecontrol.modules.more.AboutFragment;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.wizard.WizardCallbacks;
import org.namelessrom.devicecontrol.wizard.firstlaunch.FirstLaunchWizard;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private Toolbar mToolbar;

    private Runnable mDrawerRunnable;

    private FirstLaunchWizard mFirstLaunchWizard;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewCompat.setElevation(mToolbar, 4.0f);
        setSupportActionBar(mToolbar);

        // lock the drawer so we can only open it AFTER we are done with our checks
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override public void onDrawerSlide(View drawerView, float slideOffset) { }

            @Override public void onDrawerOpened(View drawerView) { }

            @Override public void onDrawerClosed(View drawerView) {
                if (mDrawerRunnable != null) {
                    mDrawerRunnable.run();
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override public void onDrawerStateChanged(int newState) { }
        });

        final ImageView drawerHeaderSettings = (ImageView) mDrawerLayout.findViewById(R.id.drawer_header_settings);
        drawerHeaderSettings.setOnClickListener(this);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (FirstLaunchWizard.isFirstLaunch(this)) {
            mFirstLaunchWizard = FirstLaunchWizard.create(mWizardCallbacks);
            replaceFragment(mFirstLaunchWizard, null);
        } else {
            setup();
        }
    }

    private void setup() {
        // TODO: root checks, etc
        setupDrawerItems();
        unlockMenu();
        replaceFragment(new HomeFragment(), R.id.nav_item_home, null);
    }

    @Override public void onBackPressed() {
        if (mFirstLaunchWizard != null && mFirstLaunchWizard.isSetupActive) {
            mFirstLaunchWizard.onPreviousPage();
            return;
        }
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

    @Override public boolean onNavigationItemSelected(final MenuItem menuItem) {
        // close drawer
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.closeDrawer(GravityCompat.START);

        // start action once drawer is closed
        mDrawerRunnable = new Runnable() {
            @Override public void run() {
                boolean shouldCheck = true;

                // TODO: implement every navigation item
                final int id = menuItem.getItemId();
                switch (id) {
                    case R.id.nav_item_home: {
                        if (!(mCurrentFragment instanceof HomeFragment)) {
                            // clear the fragment back stack when getting back to home
                            final FragmentManager fm = getSupportFragmentManager();
                            if (fm.getBackStackEntryCount() > 0) {
                                fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }

                            // the HomeFragment is the central place, never add it to the back stack
                            replaceFragment(new HomeFragment(), null);
                        }
                        break;
                    }
                    //==============================================================================
                    case R.id.nav_item_info_device: {
                        if (!(mCurrentFragment instanceof InfoFragment)) {
                            replaceFragment(new InfoFragment(), "");
                        }
                        break;
                    }
                    //==============================================================================
                    case R.id.nav_item_more_about: {
                        if (!(mCurrentFragment instanceof AboutFragment)) {
                            replaceFragment(new AboutFragment(), "");
                        }
                        break;
                    }
                    case R.id.nav_item_more_privacy: {
                        AppHelper.launchUrlViaTabs(MainActivity.this, getString(R.string.non_dc_privacy_url));
                        shouldCheck = false;
                        break;
                    }
                }

                if (shouldCheck) {
                    checkMenuItem(menuItem);
                }

                mDrawerRunnable = null;
            }
        };
        return true;
    }

    private void setupDrawerItems() {
        // manually check home drawer entry
        mPreviousMenuItem = findMenuItem(R.id.nav_item_home);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(true);
        }
    }

    public void unlockMenu() {
        final ActionBar actionBar = getSupportActionBar();
        assert (actionBar != null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.drawer_header_settings: {
                break;
            }
        }
    }

    private final WizardCallbacks mWizardCallbacks = new WizardCallbacks() {
        @Override public void onSetupStarted() {
            mFirstLaunchWizard.isSetupActive = true;
        }

        @Override public void onSetupDone(boolean isAborted) {
            mFirstLaunchWizard.isSetupActive = false;
            FirstLaunchWizard.setFirstLaunchDone(MainActivity.this, true);

            setup();
        }
    };
}
