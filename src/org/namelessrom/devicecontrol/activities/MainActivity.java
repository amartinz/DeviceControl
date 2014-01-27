/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.main.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.main.HelpDialogFragment;
import org.namelessrom.devicecontrol.fragments.main.InformationFragment;
import org.namelessrom.devicecontrol.fragments.main.NavigationDrawerFragment;
import org.namelessrom.devicecontrol.fragments.main.PerformanceFragment;
import org.namelessrom.devicecontrol.fragments.main.PreferencesFragment;
import org.namelessrom.devicecontrol.fragments.main.TaskerFragment;
import org.namelessrom.devicecontrol.fragments.main.ToolsFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.IOException;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static NavigationDrawerFragment mNavigationDrawerFragment;
    private static PreferencesFragment mPreferencesFragment;
    private CharSequence mTitle;
    private static long back_pressed;
    private Toast mToast;
    final Object lockObject = new Object();

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Application.IS_LOG_DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

            RootTools.debugMode = true;
        }

        if (!Application.HAS_ROOT) {
            Toast.makeText(this
                    , getString(R.string.app_warning_root, getString(R.string.app_name))
                    , Toast.LENGTH_LONG).show();
        }

        PreferenceHelper.getInstance(this);

        Utils.setupDirectories();
        Utils.createFiles(this, true);

        mTitle = getTitle();

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        mPreferencesFragment = (PreferencesFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer_prefs);

        mPreferencesFragment.setUp(
                R.id.navigation_drawer_prefs,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment;

        switch (position) {
            default:
            case InformationFragment.ID:
                fragment = new InformationFragment();
                break;
            case DeviceFragment.ID:
                fragment = new DeviceFragment();
                break;
            case PerformanceFragment.ID:
                fragment = new PerformanceFragment();
                break;
            case TaskerFragment.ID:
                fragment = new TaskerFragment();
                break;
            case ToolsFragment.ID:
                fragment = new ToolsFragment();
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (isNavDrawerOpen()) {
                    mNavigationDrawerFragment.closeDrawer();
                }
                if (isPrefDrawerOpen()) {
                    mPreferencesFragment.closeDrawer();
                } else {
                    mPreferencesFragment.openDrawer();
                }
                break;
            case R.id.action_help:
                HelpDialogFragment.newInstance().show(getFragmentManager(), HelpDialogFragment.TAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else if (!mNavigationDrawerFragment.isDrawerOpen()
                && !mPreferencesFragment.isDrawerOpen()) {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                if (mToast != null)
                    mToast.cancel();
                finish();
            } else {
                mToast = Toast.makeText(getBaseContext(),
                        getString(R.string.action_press_again), Toast.LENGTH_SHORT);
                mToast.show();
            }
            back_pressed = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (lockObject) {
            try {
                logDebug("closing shells");
                RootTools.closeAllShells();
            } catch (IOException e) {
                logDebug("Shell error: " + e.getMessage());
            }
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    boolean isNavDrawerOpen() {
        return mNavigationDrawerFragment.isDrawerOpen();
    }

    boolean isPrefDrawerOpen() {
        return mPreferencesFragment.isDrawerOpen();
    }

    /**
     * @param id The id of the attached Fragment
     */
    public void onSectionAttached(int id) {
        switch (id) {
            default:
            case InformationFragment.ID:
                mTitle = getString(R.string.section_title_information);
                break;
            case DeviceFragment.ID:
                mTitle = getString(R.string.section_title_device);
                break;
            case PerformanceFragment.ID:
                mTitle = getString(R.string.section_title_performance);
                break;
            case TaskerFragment.ID:
                mTitle = getString(R.string.section_title_tasker);
                break;
            case ToolsFragment.ID:
                mTitle = getString(R.string.section_title_tools);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
}
