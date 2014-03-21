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
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.dynamic.WebViewFragment;
import org.namelessrom.devicecontrol.fragments.main.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.main.InformationFragment;
import org.namelessrom.devicecontrol.fragments.main.PreferencesFragment;
import org.namelessrom.devicecontrol.fragments.main.TaskerFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceCpuSettings;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceInformationFragment;
import org.namelessrom.devicecontrol.fragments.tools.ToolsEditorTabbed;
import org.namelessrom.devicecontrol.fragments.tools.ToolsFreezerTabbed;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.adapters.MenuListArrayAdapter;

import java.io.IOException;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class MainActivity extends Activity
        implements DeviceConstants, AdapterView.OnItemClickListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private        CharSequence mTitle;
    private static long         back_pressed;
    private        Toast        mToast;
    final Object lockObject = new Object();

    public static SlidingMenu mSlidingMenu;

    private static final int ID_DEVICE                   = 1;
    private static final int ID_PERFORMANCE_INFO         = 3;
    private static final int ID_PERFORMANCE_CPU_SETTINGS = 4;
    private static final int ID_PERFORMANCE_EXTRA        = 5;
    private static final int ID_TASKER                   = 7;
    private static final int ID_TOOLS_EDITORS            = 8;
    private static final int ID_TOOLS_FREEZER            = 9;
    private static final int ID_LICENSES                 = 11;

    public static final int[] MENU_ICONS = {
            -1, // Device
            0,
            -1, // Performance
            0,
            0,
            0,
            -1, // Tools
            0,
            0,
            0,
            -1, // Information
            0
    };

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Application.HAS_ROOT) {
            Toast.makeText(this
                    , getString(R.string.app_warning_root, getString(R.string.app_name))
                    , Toast.LENGTH_LONG).show();
        }

        PreferenceHelper.getInstance(this);

        if (PreferenceHelper.getBoolean(DC_FIRST_START, true)) {
            // Set flag to enable BootUp receiver
            PreferenceHelper.setBoolean(DC_FIRST_START, false);
        }

        Utils.setupDirectories();
        Utils.createFiles(this, true);

        final View v = getLayoutInflater().inflate(R.layout.menu_list, null, false);
        final ListView mMenuList = (ListView) v.findViewById(R.id.navbarlist);

        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setBackground(getResources().getDrawable(R.drawable.bg_menu_dark));
        mSlidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(v);

        final View vv = getLayoutInflater().inflate(R.layout.menu_prefs, null, false);
        mSlidingMenu.setSecondaryMenu(vv);
        mSlidingMenu.setSecondaryShadowDrawable(R.drawable.shadow_right);

        final MenuListArrayAdapter mAdapter = new MenuListArrayAdapter(
                this,
                R.layout.menu_main_list_item,
                getResources().getStringArray(R.array.menu_entries),
                MENU_ICONS);
        mMenuList.setAdapter(mAdapter);
        mMenuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuList.setOnItemClickListener(this);

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        final Fragment main = new InformationFragment();
        final Fragment right = new PreferencesFragment();
        ft.replace(R.id.container, main);
        ft.replace(R.id.menu_frame, right);
        ft.commit();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Fragment main;
        Fragment right;

        switch (i) {
            default:
            case ID_DEVICE:
                main = new DeviceFragment();
                right = new PreferencesFragment();
                break;
            case ID_PERFORMANCE_INFO:
                main = new PerformanceInformationFragment();
                right = new PreferencesFragment();
                break;
            case ID_PERFORMANCE_CPU_SETTINGS:
                main = new PerformanceCpuSettings();
                right = new PreferencesFragment();
                break;
            case ID_PERFORMANCE_EXTRA:
                main = new PerformanceExtrasFragment();
                right = new PreferencesFragment();
                break;
            case ID_TASKER:
                main = new TaskerFragment();
                right = new PreferencesFragment();
                break;
            case ID_TOOLS_EDITORS:
                main = new ToolsEditorTabbed();
                right = new PreferencesFragment();
                break;
            case ID_TOOLS_FREEZER:
                main = new ToolsFreezerTabbed();
                right = new PreferencesFragment();
                break;
            case ID_LICENSES:
                main = WebViewFragment.newInstance(WebViewFragment.TYPE_LICENSES);
                right = new PreferencesFragment();
                break;

        }

        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.container, main);
        ft.replace(R.id.menu_frame, right);

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //case R.id.action_settings:
            //    break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.toggle(true);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                if (mToast != null) { mToast.cancel(); }
                finish();
            } else {
                mToast = Toast.makeText(getBaseContext(),
                        getString(R.string.action_press_again), Toast.LENGTH_SHORT);
                mToast.show();
            }
            back_pressed = System.currentTimeMillis();
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

    /**
     * @param id The id of the attached Fragment
     */
    public void onSectionAttached(int id) {
        switch (id) {
            case DeviceFragment.ID:
                mTitle = getString(R.string.device);
                break;
            case PerformanceInformationFragment.ID:
                mTitle = getString(R.string.information);
                break;
            case PerformanceCpuSettings.ID:
                mTitle = getString(R.string.cpusettings);
                break;
            case PerformanceExtrasFragment.ID:
                mTitle = getString(R.string.extras);
                break;
            case TaskerFragment.ID:
                mTitle = getString(R.string.tasker);
                break;
            case ToolsEditorTabbed.ID:
                mTitle = getString(R.string.editors);
                break;
            case ToolsFreezerTabbed.ID:
                mTitle = getString(R.string.freezer);
                break;
            default:
                mTitle = getString(R.string.app_name);
                break;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }
}
