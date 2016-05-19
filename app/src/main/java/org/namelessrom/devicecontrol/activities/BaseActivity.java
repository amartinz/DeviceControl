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
package org.namelessrom.devicecontrol.activities;

import android.app.Activity;
import android.app.ActivityManager.TaskDescription;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;

public abstract class BaseActivity extends AppCompatActivity {
    protected MaterialMenuIconToolbar mMaterialMenu;
    protected NavigationView mNavigationView;
    protected MenuItem mPreviousMenuItem;

    protected void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ViewCompat.setElevation(toolbar, 4.0f);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    finish();
                }
            });
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void setupMaterialMenu(Activity activity) {
        mMaterialMenu = new MaterialMenuIconToolbar(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
            @Override public int getToolbarViewId() { return R.id.toolbar; }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMaterialMenu.setNeverDrawTouch(true);
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final AppResources res = AppResources.get();
            // WTF! IRIS506Q android version "unknown"
            try {
                getWindow().setStatusBarColor(res.getPrimaryColor());
            } catch (Exception e) {
                Log.e("BaseActivity", "get a stone and throw it at your device vendor", e);
            }

            // color recents tab
            final Bitmap appIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_devicecontrol);
            final TaskDescription description = new TaskDescription(String.valueOf(getTitle()), appIcon, res.getAccentColor());
            setTaskDescription(description);
        }
    }

    @Override protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mMaterialMenu != null) {
            mMaterialMenu.syncState(savedInstanceState);
        }
    }

    @Override protected void onSaveInstanceState(final Bundle outState) {
        if (mMaterialMenu != null) {
            mMaterialMenu.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
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
        return findMenuItem(mNavigationView, menuId);
    }

    @Nullable public MenuItem findMenuItem(@Nullable NavigationView navigationView, final int menuId) {
        if (navigationView == null || navigationView.getMenu() == null) {
            return null;
        }
        return navigationView.getMenu().findItem(menuId);
    }

}
