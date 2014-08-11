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

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Purchase;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.negusoft.holoaccent.activity.AccentActivity;
import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.adapters.MenuListArrayAdapter;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.DonationStartedEvent;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.events.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.fragments.HomeFragment;
import org.namelessrom.devicecontrol.fragments.LicenseFragment;
import org.namelessrom.devicecontrol.fragments.PreferencesFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.device.FeaturesFragment;
import org.namelessrom.devicecontrol.fragments.device.sub.FastChargeFragment;
import org.namelessrom.devicecontrol.fragments.performance.CpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.ExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.GpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.InformationFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.EntropyFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.FilesystemFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.GovernorFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.HotpluggingFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.KsmFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.LowMemoryKillerFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.ThermalFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.fragments.tools.AppListFragment;
import org.namelessrom.devicecontrol.fragments.tools.ToolsMoreFragment;
import org.namelessrom.devicecontrol.fragments.tools.WirelessFileManagerFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.BuildPropEditorFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.BuildPropFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.SysctlEditorFragment;
import org.namelessrom.devicecontrol.fragments.tools.editor.SysctlFragment;
import org.namelessrom.devicecontrol.fragments.tools.flasher.FlasherFragment;
import org.namelessrom.devicecontrol.fragments.tools.flasher.FlasherPreferencesFragment;
import org.namelessrom.devicecontrol.fragments.tools.tasker.TaskListFragment;
import org.namelessrom.devicecontrol.fragments.tools.tasker.TaskerFragment;
import org.namelessrom.devicecontrol.proprietary.Constants;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.File;

import butterknife.ButterKnife;

public class MainActivity extends AccentActivity
        implements DeviceConstants, FileConstants, AdapterView.OnItemClickListener,
        SlidingMenu.OnClosedListener, SlidingMenu.OnOpenedListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final Object lockObject = new Object();
    private static long  back_pressed;
    private        Toast mToast;

    private IabHelper mHelper;

    public static SlidingMenu mSlidingMenu;

    private Fragment mCurrentFragment;

    private int mActionBarDrawable    = R.drawable.ic_launcher;
    private int mSubActionBarDrawable = -1;
    private int mTitle                = R.string.home;
    private int mFragmentTitle        = R.string.home;
    private int mSubFragmentTitle     = -1;

    private static final int[] MENU_ENTRIES = {
            R.string.device,        // Device
            ID_DEVICE,
            ID_FEATURES,
            R.string.performance,   // Performance
            ID_PERFORMANCE_INFO,
            ID_PERFORMANCE_CPU_SETTINGS,
            ID_PERFORMANCE_GPU_SETTINGS,
            ID_PERFORMANCE_EXTRA,
            R.string.tools,         // Tools
            ID_TOOLS_TASKER,
            ID_TOOLS_FLASHER,
            ID_TOOLS_MORE,
            R.string.information,   // Information
            ID_PREFERENCES,
            ID_LICENSES
    };

    private static final int[] MENU_ICONS = {
            -1, // Device
            R.drawable.ic_menu_device,
            R.drawable.ic_menu_features,
            -1, // Performance
            R.drawable.ic_menu_perf_info,
            R.drawable.ic_menu_perf_cpu,
            R.drawable.ic_menu_perf_gpu,
            R.drawable.ic_menu_perf_extras,
            -1, // Tools
            R.drawable.ic_menu_tasker,
            R.drawable.ic_menu_flash,
            R.drawable.ic_menu_code,
            -1, // Information
            R.drawable.ic_menu_preferences,
            R.drawable.ic_menu_licences
    };

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override public int getOverrideAccentColor() {
        return PreferenceHelper.getInt("pref_color", Application.getColor(R.color.accent));
    }

    @Override protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (PreferenceHelper.getBoolean(DC_FIRST_START, true)) {
            PreferenceHelper.setBoolean(DC_FIRST_START, false);
        }

        Utils.setupDirectories();

        final View v = getLayoutInflater().inflate(R.layout.menu_list, null, false);
        final ListView mMenuList = ButterKnife.findById(v, R.id.navbarlist);

        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setMode(SlidingMenu.LEFT);
        mSlidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.shadow);
        mSlidingMenu.setBehindWidthRes(R.dimen.slidingmenu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        mSlidingMenu.setMenu(v);

        // setup touch mode
        MainActivity.setSwipeOnContent(PreferenceHelper.getBoolean("swipe_on_content", false));

        final MenuListArrayAdapter mAdapter = new MenuListArrayAdapter(
                this,
                R.layout.menu_main_list_item,
                MENU_ENTRIES,
                MENU_ICONS);
        mMenuList.setAdapter(mAdapter);
        mMenuList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mMenuList.setOnItemClickListener(this);

        mSlidingMenu.setOnClosedListener(this);
        mSlidingMenu.setOnOpenedListener(this);

        setUpIab();

        loadFragment(ID_HOME);
        Utils.startTaskerService();

        final String downgradePath = getFilesDir() + DC_DOWNGRADE;
        if (Utils.fileExists(downgradePath)) {
            if (!new File(downgradePath).delete()) {
                Logger.wtf(this, "Could not delete downgrade indicator file!");
            }
            Toast.makeText(this, R.string.downgraded, Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        loadFragment((Integer) adapterView.getItemAtPosition(i));
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSubFragmentTitle == -1) {
                    mSlidingMenu.toggle(true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onBackPressed() {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.toggle(true);
        } else if (mCurrentFragment instanceof OnBackPressedListener
                && ((OnBackPressedListener) mCurrentFragment).onBackPressed()) {
            Logger.v(this, "onBackPressed()");
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
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

    @Override protected void onDestroy() {
        DatabaseHandler.tearDown();
        synchronized (lockObject) {
            Logger.i(this, "closing shells");
            try {
                RootTools.closeAllShells();
                if (mHelper != null) {
                    mHelper.dispose();
                    mHelper = null;
                }
            } catch (Exception e) {
                Logger.e(this, String.format("onDestroy(): %s", e));
            }
        }
        super.onDestroy();
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadFragment(final int i) {
        switch (i) {
            default: // slip through...
            case ID_HOME:
                mCurrentFragment = new HomeFragment();
                break;
            case ID_DEVICE:
                mCurrentFragment = new DeviceFragment();
                break;
            case ID_FEATURES:
                mCurrentFragment = new FeaturesFragment();
                break;
            case ID_PERFORMANCE_INFO:
                mCurrentFragment = new InformationFragment();
                break;
            case ID_PERFORMANCE_CPU_SETTINGS:
                mCurrentFragment = new CpuSettingsFragment();
                break;
            case ID_PERFORMANCE_GPU_SETTINGS:
                mCurrentFragment = new GpuSettingsFragment();
                break;
            case ID_PERFORMANCE_EXTRA:
                mCurrentFragment = new ExtrasFragment();
                break;
            case ID_TOOLS_TASKER:
                mCurrentFragment = new TaskerFragment();
                break;
            case ID_TOOLS_FLASHER:
                mCurrentFragment = new FlasherFragment();
                break;
            case ID_TOOLS_MORE:
                mCurrentFragment = new ToolsMoreFragment();
                break;
            case ID_PREFERENCES:
                mCurrentFragment = new PreferencesFragment();
                break;
            case ID_LICENSES:
                mCurrentFragment = new LicenseFragment();
                break;
        }

        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.container, mCurrentFragment);

        ft.commit();
    }

    @Subscribe public int onSectionAttached(final SectionAttachedEvent event) {
        final int id = event.getId();
        switch (id) {
            case ID_RESTORE:
                if (mSubFragmentTitle != -1) {
                    mTitle = mSubFragmentTitle;
                } else {
                    mTitle = mFragmentTitle;
                }
                break;
            case ID_RESTORE_FROM_SUB:
                mSubFragmentTitle = -1;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle;
                break;
            case ID_FIRST_MENU:
                mTitle = R.string.menu;
                break;
            case ID_SECOND_MENU:
                mTitle = R.string.help;
                break;
            default:
                mActionBarDrawable = R.drawable.ic_launcher;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.app_name;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_HOME:
                mActionBarDrawable = R.drawable.ic_launcher;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.home;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_DEVICE:
                mActionBarDrawable = R.drawable.ic_menu_device;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.device;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_FEATURES:
                mActionBarDrawable = R.drawable.ic_menu_features;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.features;
                mSubFragmentTitle = -1;
                break;
            case ID_FAST_CHARGE:
                mSubActionBarDrawable = R.drawable.ic_general_battery;
                mTitle = mSubFragmentTitle = R.string.fast_charge;
                break;
            //--------------------------------------------------------------------------------------
            case ID_PERFORMANCE_INFO:
                mActionBarDrawable = R.drawable.ic_menu_perf_info;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.information;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_PERFORMANCE_CPU_SETTINGS:
                mActionBarDrawable = R.drawable.ic_menu_perf_cpu;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.cpusettings;
                mSubFragmentTitle = -1;
                break;
            case ID_GOVERNOR_TUNABLE:
                mTitle = mSubFragmentTitle = R.string.cpu_governor_tuning;
                break;
            //--------------------------------------------------------------------------------------
            case ID_PERFORMANCE_GPU_SETTINGS:
                mActionBarDrawable = R.drawable.ic_menu_perf_gpu;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.gpusettings;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case ID_PERFORMANCE_EXTRA:
                mActionBarDrawable = R.drawable.ic_menu_perf_extras;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.extras;
                mSubFragmentTitle = -1;
                break;
            case ID_KSM:
                mSubActionBarDrawable = R.drawable.ic_menu_perf_extras;
                mTitle = mSubFragmentTitle = R.string.ksm;
                break;
            case ID_HOTPLUGGING:
                mSubActionBarDrawable = R.drawable.ic_general_hotplug;
                mTitle = mSubFragmentTitle = R.string.hotplugging;
                break;
            case ID_THERMAL:
                mSubActionBarDrawable = R.drawable.ic_general_heat;
                mTitle = mSubFragmentTitle = R.string.thermal;
                break;
            case ID_VOLTAGE:
                mSubActionBarDrawable = R.drawable.ic_general_voltage;
                mTitle = mSubFragmentTitle = R.string.voltage_control;
                break;
            case ID_ENTROPY:
                mSubActionBarDrawable = R.drawable.ic_menu_perf_extras;
                mTitle = mSubFragmentTitle = R.string.entropy;
                break;
            case ID_FILESYSTEM:
                mSubActionBarDrawable = R.drawable.ic_menu_perf_extras;
                mTitle = mSubFragmentTitle = R.string.filesystem;
                break;
            case ID_LOWMEMORYKILLER:
                mSubActionBarDrawable = R.drawable.ic_menu_perf_extras;
                mTitle = mSubFragmentTitle = R.string.low_memory_killer;
                break;
            //--------------------------------------------------------------------------------------
            case ID_TOOLS_TASKER:
                mActionBarDrawable = R.drawable.ic_menu_tasker;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.tasker;
                mSubFragmentTitle = -1;
                break;
            case ID_TOOLS_TASKER_LIST:
                mSubActionBarDrawable = R.drawable.ic_menu_tasker;
                mTitle = mSubFragmentTitle = R.string.tasker;
                break;
            //--------------------------------------------------------------------------------------
            case ID_TOOLS_FLASHER:
                mActionBarDrawable = R.drawable.ic_menu_flash;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.flasher;
                mSubFragmentTitle = -1;
                break;
            case ID_TOOLS_FLASHER_PREFS:
                mSubActionBarDrawable = R.drawable.ic_menu_flash;
                mTitle = mSubFragmentTitle = R.string.flasher;
                break;
            //--------------------------------------------------------------------------------------
            case ID_TOOLS_MORE:
                mActionBarDrawable = R.drawable.ic_menu_code;
                mSubActionBarDrawable = -1;
                mTitle = mFragmentTitle = R.string.more;
                mSubFragmentTitle = -1;
                break;
            case ID_TOOLS_VM:
            case ID_TOOLS_EDITORS_VM:
                mSubActionBarDrawable = R.drawable.ic_general_editor;
                mTitle = mSubFragmentTitle = R.string.sysctl_vm;
                break;
            case ID_TOOLS_BUILD_PROP:
            case ID_TOOLS_EDITORS_BUILD_PROP:
                mSubActionBarDrawable = R.drawable.ic_general_editor;
                mTitle = mSubFragmentTitle = R.string.buildprop;
                break;
            case ID_TOOLS_APP_MANAGER:
                mSubActionBarDrawable = R.drawable.ic_general_app;
                mTitle = mSubFragmentTitle = R.string.app_manager;
                break;
            case ID_TOOLS_WIRELESS_FM:
                mSubActionBarDrawable = R.drawable.ic_general_wifi;
                mTitle = mSubFragmentTitle = R.string.wireless_file_manager;
                break;
            //--------------------------------------------------------------------------------------
        }
        restoreActionBar();

        return id;
    }

    public void restoreActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            final int drawableId = ((mSubActionBarDrawable != -1)
                    ? mSubActionBarDrawable : mActionBarDrawable);
            actionBar.setIcon(drawableId);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override public void onOpened() {
        int id;
        if (mSlidingMenu.isMenuShowing() && !mSlidingMenu.isSecondaryMenuShowing()) {
            id = ID_FIRST_MENU;
        } else {
            id = ID_SECOND_MENU;
        }
        onSectionAttached(new SectionAttachedEvent(id));
    }

    @Override public void onClosed() { onSectionAttached(new SectionAttachedEvent(ID_RESTORE)); }

    //==============================================================================================
    // In App Purchase
    //==============================================================================================
    private void setUpIab() {
        final String key = Constants.Iab.getKey();
        if (!key.equals("---") && AppHelper.isPlayStoreInstalled()) {
            mHelper = new IabHelper(this, key);
            if (Logger.getEnabled()) {
                mHelper.enableDebugLogging(true, "IABDEVICECONTROL");
            }
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    Logger.i(this, "IAB: " + result);
                    PreferenceHelper.setBoolean(Constants.Iab.getPref(), result.isSuccess());
                }
            });
        } else {
            PreferenceHelper.setBoolean(Constants.Iab.getPref(), false);
        }
    }

    @Subscribe public void onDonationStartedEvent(final DonationStartedEvent event) {
        if (event == null) { return; }

        final String sku = event.getSku();
        final int reqCode = event.getReqCode();
        final String token = event.getToken();
        Logger.v(this, String.format("IAB: sku: %s | reqCode: %s | token: %s",
                sku, String.valueOf(reqCode), token));
        mHelper.launchPurchaseFlow(this, sku, reqCode, mPurchaseFinishedListener, token);
    }

    private final IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(final IabResult result, final Purchase purchase) {
            if (result.isSuccess()) {
                mHelper.consumeAsync(purchase, null);
            }
        }
    };

    @Override protected void onActivityResult(final int req, final int res, final Intent data) {
        if (!mHelper.handleActivityResult(req, res, data)) {
            super.onActivityResult(req, res, data);
        }
    }

    @Subscribe public void onSubFragmentEvent(final SubFragmentEvent event) {
        if (event == null) return;

        final int id = event.getId();

        switch (id) {
            //--------------------------------------------------------------------------------------
            case ID_FAST_CHARGE:
                mCurrentFragment = new FastChargeFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_GOVERNOR_TUNABLE:
                mCurrentFragment = new GovernorFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_KSM:
                mCurrentFragment = new KsmFragment();
                break;
            case ID_HOTPLUGGING:
                mCurrentFragment = new HotpluggingFragment();
                break;
            case ID_THERMAL:
                mCurrentFragment = new ThermalFragment();
                break;
            case ID_VOLTAGE:
                mCurrentFragment = new VoltageFragment();
                break;
            case ID_ENTROPY:
                mCurrentFragment = new EntropyFragment();
                break;
            case ID_FILESYSTEM:
                mCurrentFragment = new FilesystemFragment();
                break;
            case ID_LOWMEMORYKILLER:
                mCurrentFragment = new LowMemoryKillerFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_TOOLS_TASKER_LIST:
                mCurrentFragment = new TaskListFragment();
                break;
            case ID_TOOLS_VM:
                mCurrentFragment = new SysctlFragment();
                break;
            case ID_TOOLS_BUILD_PROP:
                mCurrentFragment = new BuildPropFragment();
                break;
            case ID_TOOLS_EDITORS_VM:
                mCurrentFragment = new SysctlEditorFragment();
                break;
            case ID_TOOLS_EDITORS_BUILD_PROP:
                mCurrentFragment = new BuildPropEditorFragment();
                break;
            case ID_TOOLS_APP_MANAGER:
                mCurrentFragment = new AppListFragment();
                break;
            case ID_TOOLS_WIRELESS_FM:
                mCurrentFragment = new WirelessFileManagerFragment();
                break;
            case ID_TOOLS_FLASHER_PREFS:
                mCurrentFragment = new FlasherPreferencesFragment();
                break;
            //--------------------------------------------------------------------------------------
            default:
                break;
        }

        if (mCurrentFragment == null) return;

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right,
                R.animator.slide_in_left, R.animator.slide_out_left);

        ft.replace(R.id.container, mCurrentFragment);
        ft.addToBackStack(null);

        ft.commit();
    }

    public static void setSwipeOnContent(final boolean swipeOnContent) {
        if (mSlidingMenu == null) return;

        mSlidingMenu.setTouchModeAbove(
                swipeOnContent ? SlidingMenu.TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_MARGIN);
    }
}
