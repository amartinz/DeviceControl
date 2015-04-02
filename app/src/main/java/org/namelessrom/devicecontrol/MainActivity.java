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
package org.namelessrom.devicecontrol;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.pollfish.constants.Position;
import com.pollfish.main.PollFish;
import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.modules.about.AboutFragment;
import org.namelessrom.devicecontrol.modules.appmanager.AppListFragment;
import org.namelessrom.devicecontrol.modules.cpu.CpuSettingsFragment;
import org.namelessrom.devicecontrol.modules.cpu.GovernorFragment;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureFragment;
import org.namelessrom.devicecontrol.modules.device.DeviceInformationFragment;
import org.namelessrom.devicecontrol.modules.device.sub.FastChargeFragment;
import org.namelessrom.devicecontrol.modules.device.sub.SoundControlFragment;
import org.namelessrom.devicecontrol.modules.editor.BuildPropEditorFragment;
import org.namelessrom.devicecontrol.modules.editor.SysctlEditorFragment;
import org.namelessrom.devicecontrol.modules.editor.SysctlFragment;
import org.namelessrom.devicecontrol.modules.flasher.FlasherFragment;
import org.namelessrom.devicecontrol.modules.performance.FilesystemFragment;
import org.namelessrom.devicecontrol.modules.performance.GpuSettingsFragment;
import org.namelessrom.devicecontrol.modules.performance.InformationFragment;
import org.namelessrom.devicecontrol.modules.performance.ThermalFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.EntropyFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.IoSchedConfigFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.KsmFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.UksmFragment;
import org.namelessrom.devicecontrol.modules.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.modules.preferences.PreferencesFragment;
import org.namelessrom.devicecontrol.modules.tasker.TaskerFragment;
import org.namelessrom.devicecontrol.modules.tools.ToolsMoreFragment;
import org.namelessrom.devicecontrol.modules.tools.WirelessFileManagerFragment;
import org.namelessrom.devicecontrol.ui.adapters.MenuListArrayAdapter;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.proprietary.Configuration;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final Object lockObject = new Object();

    private static long mBackPressed;
    private Toast mToast;

    public static SlidingMenu sSlidingMenu;
    public static MaterialMenuIconToolbar sMaterialMenu;

    public static boolean sDisableFragmentAnimations;

    private Fragment mCurrentFragment;

    private int mTitle = R.string.home;
    private int mFragmentTitle = R.string.home;
    private int mSubFragmentTitle = -1;

    private ArrayList<MenuListArrayAdapter.MenuItem> setupMenuLists() {
        final ArrayList<MenuListArrayAdapter.MenuItem> menuItems = new ArrayList<>();

        // header - device
        menuItems.add(new MenuListArrayAdapter.MenuItem(-1, R.string.device, -1));
        // device - information
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_DEVICE_INFORMATION, R.string.device_information,
                R.drawable.ic_device_info));
        // device - features
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_FEATURES, R.string.features,
                R.drawable.ic_developer_mode));

        // header - performance
        menuItems.add(new MenuListArrayAdapter.MenuItem(-1, R.string.performance, -1));
        // performance - information
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_PERFORMANCE_INFO, R.string.information,
                R.drawable.ic_menu_perf_info));
        // performance - cpu
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_PERFORMANCE_CPU_SETTINGS, R.string.cpusettings,
                R.drawable.ic_memory));
        // performance - gpu
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_PERFORMANCE_GPU_SETTINGS, R.string.gpusettings,
                R.drawable.ic_display));
        // performance - filesystem
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_FILESYSTEM, R.string.filesystem,
                R.drawable.ic_storage));
        // performance - thermal
        if (Utils.fileExists(getString(R.string.directory_msm_thermal))
                || Utils.fileExists(getString(R.string.file_intelli_thermal_base))) {
            menuItems.add(new MenuListArrayAdapter.MenuItem(
                    DeviceConstants.ID_THERMAL, R.string.thermal,
                    R.drawable.ic_heat));
        }

        // header - tools
        menuItems.add(new MenuListArrayAdapter.MenuItem(-1, R.string.tools, -1));
        // tools - app manager
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_TOOLS_APP_MANAGER, R.string.app_manager,
                R.drawable.ic_android));
        // tools - tasker
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_TOOLS_TASKER, R.string.tasker,
                R.drawable.ic_extension));
        // tools - flasher
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_TOOLS_FLASHER, R.string.flasher,
                R.drawable.ic_flash));
        // tools - more
        menuItems.add(new MenuListArrayAdapter.MenuItem(
                DeviceConstants.ID_TOOLS_MORE, R.string.more,
                R.drawable.ic_widgets));

        return menuItems;
    }

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected void onResume() {
        super.onResume();
        final String pfApiKey = Configuration.getPollfishApiKeyDc();
        if (!TextUtils.equals("---", pfApiKey) && DeviceConfiguration.get(this).showPollfish) {
            Logger.v(this, "PollFish.init()");
            PollFish.init(this, pfApiKey, Position.BOTTOM_RIGHT, 30);
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup action bar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mSubFragmentTitle == -1) {
                    sSlidingMenu.toggle(true);
                } else {
                    onCustomBackPressed(true);
                }
            }
        });

        // setup material menu icon
        sMaterialMenu =
                new MaterialMenuIconToolbar(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
                    @Override public int getToolbarViewId() { return R.id.toolbar; }
                };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sMaterialMenu.setNeverDrawTouch(true);
        }

        Utils.setupDirectories();

        final FrameLayout container = (FrameLayout) findViewById(R.id.container);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            container.setBackground(null);
        } else {
            //noinspection deprecation
            container.setBackgroundDrawable(null);
        }

        final View v = getLayoutInflater().inflate(R.layout.menu_list, container, false);
        final ListView menuList = (ListView) v.findViewById(R.id.navbarlist);
        final LinearLayout menuContainer = (LinearLayout) v.findViewById(R.id.menu_container);
        // setup our static items
        menuContainer.findViewById(R.id.menu_prefs).setOnClickListener(this);
        menuContainer.findViewById(R.id.menu_about).setOnClickListener(this);

        sSlidingMenu = new SlidingMenu(this);
        sSlidingMenu.setMode(SlidingMenu.LEFT);
        sSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        sSlidingMenu.setShadowDrawable(R.drawable.shadow_left);
        sSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        sSlidingMenu.setFadeEnabled(true);
        sSlidingMenu.setFadeDegree(0.45f);
        sSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        sSlidingMenu.setMenu(v);

        // setup touch mode
        MainActivity.setSwipeOnContent(DeviceConfiguration.get(this).swipeOnContent);

        // setup menu list
        final ArrayList<MenuListArrayAdapter.MenuItem> menuItems = setupMenuLists();
        final MenuListArrayAdapter adapter = new MenuListArrayAdapter(
                this, R.layout.menu_main_list_item, menuItems);
        menuList.setAdapter(adapter);
        menuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        menuList.setOnItemClickListener(this);

        loadFragmentPrivate(DeviceConstants.ID_ABOUT, false);
        getSupportFragmentManager().executePendingTransactions();

        Utils.startTaskerService();

        if (DeviceConfiguration.get(this).dcFirstStart) {
            DeviceConfiguration.get(this).dcFirstStart = false;
            DeviceConfiguration.get(this).saveConfiguration(this);
        }
    }

    @Override public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.menu_prefs:
                loadFragmentPrivate(DeviceConstants.ID_PREFERENCES, false);
                break;
            case R.id.menu_about:
                loadFragmentPrivate(DeviceConstants.ID_ABOUT, false);
                break;
        }
    }

    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        final MenuListArrayAdapter.MenuItem item =
                (MenuListArrayAdapter.MenuItem) adapterView.getItemAtPosition(i);
        Logger.d(this, "onItemClick -> %s", item.id);
        loadFragmentPrivate(item.id, false);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        sMaterialMenu.syncState(savedInstanceState);
    }

    @Override protected void onSaveInstanceState(@NonNull final Bundle outState) {
        sMaterialMenu.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    private void onCustomBackPressed(final boolean animatePressed) {
        Logger.v(this, "onCustomBackPressed(%s)", animatePressed);

        // toggle menu if it is showing and return
        if (sSlidingMenu.isMenuShowing()) {
            sSlidingMenu.toggle(true);
            return;
        }

        // if we have a OnBackPressedListener at the fragment, go in
        if (mCurrentFragment instanceof OnBackPressedListener) {
            final OnBackPressedListener listener = ((OnBackPressedListener) mCurrentFragment);

            // if our listener handles onBackPressed(), return
            if (listener.onBackPressed()) {
                Logger.v(this, "onBackPressed()");
                return;
            }

            // else we will have to go back or exit.
            // in this case, lets get the correct icons
            final MaterialMenuDrawable.IconState iconState;
            if (listener.showBurger()) {
                iconState = MaterialMenuDrawable.IconState.BURGER;
            } else {
                iconState = MaterialMenuDrawable.IconState.ARROW;
            }

            Logger.v(this, "iconState: %s", iconState);

            // we can separate actionbar back actions and back key presses
            if (animatePressed) {
                sMaterialMenu.animatePressedState(iconState);
            } else {
                sMaterialMenu.animateState(iconState);
            }

            // after animating, go further
        }

        // we we have at least one fragment in the BackStack, pop it and return
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();

            // restore title / actionbar
            if (mSubFragmentTitle != -1) {
                mTitle = mSubFragmentTitle;
            } else {
                mTitle = mFragmentTitle;
            }
            restoreActionBar();

            return;
        }

        // if nothing matched by now, we do not have any fragments in the BackStack, nor we have
        // the menu open. in that case lets detect a double back press and exit the activity
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            if (mToast != null) { mToast.cancel(); }
            finish();
        } else {
            mToast = Toast.makeText(getBaseContext(),
                    getString(R.string.action_press_again), Toast.LENGTH_SHORT);
            mToast.show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override public void onBackPressed() {
        onCustomBackPressed(false);
    }

    @Override protected void onDestroy() {
        synchronized (lockObject) {
            Logger.i(this, "closing shells");
            try {
                RootTools.closeAllShells();
            } catch (Exception e) {
                Logger.e(this, String.format("onDestroy(): %s", e));
            }
        }
        super.onDestroy();
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public void setFragment(final Fragment fragment) {
        if (fragment == null) return;
        Logger.v(this, "setFragment: %s", fragment.getId());
        mCurrentFragment = fragment;
    }

    public static void loadFragment(final Activity activity, final int id) {
        loadFragment(activity, id, false);
    }

    public static void loadFragment(final Activity activity, final int id, final boolean onResume) {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).loadFragmentPrivate(id, onResume);
        }
    }

    private void loadFragmentPrivate(final int i, final boolean onResume) {
        switch (i) {
            default: // slip through...
                //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_ABOUT:
                if (!onResume) mCurrentFragment = new AboutFragment();
                mTitle = mFragmentTitle = R.string.app_name;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_DEVICE_INFORMATION:
                if (!onResume) mCurrentFragment = new DeviceInformationFragment();
                mTitle = mFragmentTitle = R.string.device;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_FEATURES:
                if (!onResume) mCurrentFragment = new DeviceFeatureFragment();
                mTitle = mFragmentTitle = R.string.features;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_FAST_CHARGE:
                if (!onResume) mCurrentFragment = new FastChargeFragment();
                mTitle = mSubFragmentTitle = R.string.fast_charge;
                break;
            case DeviceConstants.ID_SOUND_CONTROL:
                if (!onResume) mCurrentFragment = new SoundControlFragment();
                mTitle = mSubFragmentTitle = R.string.sound_control;
                break;
            case DeviceConstants.ID_KSM:
                if (!onResume) mCurrentFragment = new KsmFragment();
                mTitle = mSubFragmentTitle = R.string.ksm;
                break;
            case DeviceConstants.ID_UKSM:
                if (!onResume) mCurrentFragment = new UksmFragment();
                mTitle = mSubFragmentTitle = R.string.uksm;
                break;
            case DeviceConstants.ID_VOLTAGE:
                if (!onResume) mCurrentFragment = new VoltageFragment();
                mTitle = mSubFragmentTitle = R.string.voltage_control;
                break;
            case DeviceConstants.ID_ENTROPY:
                if (!onResume) mCurrentFragment = new EntropyFragment();
                mTitle = mSubFragmentTitle = R.string.entropy;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_PERFORMANCE_INFO:
                if (!onResume) mCurrentFragment = new InformationFragment();
                mTitle = mFragmentTitle = R.string.information;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_PERFORMANCE_CPU_SETTINGS:
                if (!onResume) mCurrentFragment = new CpuSettingsFragment();
                mTitle = mFragmentTitle = R.string.cpusettings;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_GOVERNOR_TUNABLE:
                if (!onResume) mCurrentFragment = new GovernorFragment();
                mTitle = mSubFragmentTitle = R.string.cpu_governor_tuning;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_PERFORMANCE_GPU_SETTINGS:
                if (!onResume) mCurrentFragment = new GpuSettingsFragment();
                mTitle = mFragmentTitle = R.string.gpusettings;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_FILESYSTEM:
                if (!onResume) mCurrentFragment = new FilesystemFragment();
                mTitle = mFragmentTitle = R.string.filesystem;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_IOSCHED_TUNING:
                if (!onResume) mCurrentFragment = new IoSchedConfigFragment();
                mTitle = mSubFragmentTitle = R.string.io;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_THERMAL:
                if (!onResume) mCurrentFragment = new ThermalFragment();
                mTitle = mFragmentTitle = R.string.thermal;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_APP_MANAGER:
                if (!onResume) mCurrentFragment = new AppListFragment();
                mTitle = mFragmentTitle = R.string.app_manager;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_TASKER:
                if (!onResume) mCurrentFragment = new TaskerFragment();
                mTitle = mFragmentTitle = R.string.tasker;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_FLASHER:
                if (!onResume) mCurrentFragment = new FlasherFragment();
                mTitle = mFragmentTitle = R.string.flasher;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_MORE:
                if (!onResume) mCurrentFragment = new ToolsMoreFragment();
                mTitle = mFragmentTitle = R.string.more;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_TOOLS_VM:
                if (!onResume) mCurrentFragment = new SysctlFragment();
                mTitle = mSubFragmentTitle = R.string.sysctl_vm;
                break;
            case DeviceConstants.ID_TOOLS_EDITORS_VM:
                if (!onResume) mCurrentFragment = new SysctlEditorFragment();
                mTitle = mSubFragmentTitle = R.string.sysctl_vm;
                break;
            case DeviceConstants.ID_TOOLS_EDITORS_BUILD_PROP:
                if (!onResume) mCurrentFragment = new BuildPropEditorFragment();
                mTitle = mSubFragmentTitle = R.string.buildprop;
                break;
            case DeviceConstants.ID_TOOLS_WIRELESS_FM:
                if (!onResume) mCurrentFragment = new WirelessFileManagerFragment();
                mTitle = mSubFragmentTitle = R.string.wireless_file_manager;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_PREFERENCES:
                if (!onResume) mCurrentFragment = new PreferencesFragment();
                mTitle = mFragmentTitle = R.string.preferences;
                mSubFragmentTitle = -1;
                break;
        }

        restoreActionBar();

        if (onResume) {
            return;
        }

        final boolean isSubFragment = mSubFragmentTitle != -1;

        final FragmentManager fragmentManager = getSupportFragmentManager();
        if (!isSubFragment && fragmentManager.getBackStackEntryCount() > 0) {
            // set a lock to prevent calling setFragment as onResume gets called
            AppHelper.preventOnResume = true;
            MainActivity.sDisableFragmentAnimations = true;
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            MainActivity.sDisableFragmentAnimations = false;
            // release the lock
            AppHelper.preventOnResume = false;
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();

        if (isSubFragment) {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                    R.anim.slide_in_left, R.anim.slide_out_left);
            ft.addToBackStack(null);
        }

        ft.replace(R.id.container, mCurrentFragment);
        ft.commit();

        final MaterialMenuDrawable.IconState iconState;
        if (isSubFragment) {
            iconState = MaterialMenuDrawable.IconState.ARROW;
        } else {
            iconState = MaterialMenuDrawable.IconState.BURGER;
        }

        sMaterialMenu.animateState(iconState);
    }

    private void restoreActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    public static void setSwipeOnContent(final boolean swipeOnContent) {
        if (sSlidingMenu == null) return;

        sSlidingMenu.setTouchModeAbove(swipeOnContent
                ? SlidingMenu.TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_MARGIN);
    }

}
