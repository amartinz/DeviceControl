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
package org.namelessrom.devicecontrol;

import android.app.ActionBar;
import android.app.Activity;
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
import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.events.DonationStartedEvent;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.fragments.HelpFragment;
import org.namelessrom.devicecontrol.fragments.HomeFragment;
import org.namelessrom.devicecontrol.fragments.PreferencesFragment;
import org.namelessrom.devicecontrol.fragments.WebViewFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.device.FeaturesFragment;
import org.namelessrom.devicecontrol.fragments.device.sub.FastChargeFragment;
import org.namelessrom.devicecontrol.fragments.performance.CpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.ExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.GpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.InformationFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.GovernorFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.HotpluggingFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.ThermalFragment;
import org.namelessrom.devicecontrol.fragments.performance.sub.VoltageFragment;
import org.namelessrom.devicecontrol.fragments.tools.AppListFragment;
import org.namelessrom.devicecontrol.fragments.tools.ToolsMoreFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.BuildPropEditorFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.BuildPropFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.SysctlEditorFragment;
import org.namelessrom.devicecontrol.fragments.tools.sub.editor.SysctlFragment;
import org.namelessrom.devicecontrol.fragments.tools.tasker.TaskListFragment;
import org.namelessrom.devicecontrol.fragments.tools.tasker.TaskerFragment;
import org.namelessrom.devicecontrol.proprietary.Constants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.adapters.MenuListArrayAdapter;

import java.io.File;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class MainActivity extends Activity
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

    private int mActionBarDrawable    = R.drawable.ic_launcher;
    private int mSubActionBarDrawable = -1;
    private int mTitle                = R.string.home;
    private int mFragmentTitle        = R.string.home;
    private int mSubFragmentTitle     = -1;

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
            R.drawable.ic_menu_code,
            -1, // Information
            R.drawable.ic_menu_preferences,
            R.drawable.ic_menu_licences
    };

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================
    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceHelper.getInstance(this);
        if (PreferenceHelper.getBoolean(DC_FIRST_START, true)) {
            PreferenceHelper.setBoolean(DC_FIRST_START, false);
        }

        Utils.setupDirectories();

        final View v = getLayoutInflater().inflate(R.layout.menu_list, null, false);
        final ListView mMenuList = (ListView) v.findViewById(R.id.navbarlist);

        mSlidingMenu = new SlidingMenu(this);
        mSlidingMenu.setBackgroundResource(R.drawable.bg_menu_dark);
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

        mSlidingMenu.setOnClosedListener(this);
        mSlidingMenu.setOnOpenedListener(this);

        setUpIab();

        loadFragment(ID_HOME);
        Utils.startTaskerService();

        final String downgradePath = getFilesDir() + DC_DOWNGRADE;
        if (Utils.fileExists(downgradePath)) {
            if (!new File(downgradePath).delete()) {
                logDebug("Could not delete downgrade indicator file!");
            }
            Toast.makeText(this, R.string.downgraded, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        loadFragment(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mSubFragmentTitle == -1) {
                    mSlidingMenu.toggle(true);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.toggle(true);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        synchronized (lockObject) {
            logDebug("closing shells");
            try {
                RootTools.closeAllShells();
                if (mHelper != null) {
                    mHelper.dispose();
                    mHelper = null;
                }
            } catch (Exception e) {
                logDebug("onDestroy(): " + e);
            }
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadFragment(final int i) {
        Fragment main;
        Fragment right = HelpFragment.newInstance(i);

        switch (i) {
            default:
                right = HelpFragment.newInstance(ID_DUMMY);
            case ID_HOME:
                main = new HomeFragment();
                break;
            case ID_DEVICE:
                main = new DeviceFragment();
                break;
            case ID_FEATURES:
                main = new FeaturesFragment();
                break;
            case ID_PERFORMANCE_INFO:
                main = new InformationFragment();
                break;
            case ID_PERFORMANCE_CPU_SETTINGS:
                main = new CpuSettingsFragment();
                break;
            case ID_PERFORMANCE_GPU_SETTINGS:
                main = new GpuSettingsFragment();
                break;
            case ID_PERFORMANCE_EXTRA:
                main = new ExtrasFragment();
                break;
            case ID_TOOLS_TASKER:
                main = new TaskerFragment();
                break;
            case ID_TOOLS_MORE:
                main = new ToolsMoreFragment();
                break;
            case ID_PREFERENCES:
                main = new PreferencesFragment();
                break;
            case ID_LICENSES:
                main = WebViewFragment.newInstance(WebViewFragment.TYPE_LICENSES);
                break;
        }

        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.container, main);
        ft.replace(R.id.menu_frame, right);

        ft.commit();
    }

    @Subscribe
    public void onSectionAttached(final SectionAttachedEvent event) {
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
            //--------------------------------------------------------------------------------------
        }
        restoreActionBar();
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

    @Override
    public void onOpened() {
        int id;
        if (mSlidingMenu.isMenuShowing() && !mSlidingMenu.isSecondaryMenuShowing()) {
            id = ID_FIRST_MENU;
        } else {
            id = ID_SECOND_MENU;
        }
        onSectionAttached(new SectionAttachedEvent(id));
    }

    @Override
    public void onClosed() { onSectionAttached(new SectionAttachedEvent(ID_RESTORE)); }

    //==============================================================================================
    // In App Purchase
    //==============================================================================================
    private void setUpIab() {
        final String key = Constants.Iab.getKey();
        if (!key.equals("---") && Utils.isGmsInstalled()) {
            mHelper = new IabHelper(this, key);
            if (Application.IS_LOG_DEBUG) {
                mHelper.enableDebugLogging(true, "IABDEVICECONTROL");
            }
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    logDebug("IAB: " + result);
                    PreferenceHelper.setBoolean(Constants.Iab.getPref(), result.isSuccess());
                }
            });
        } else {
            PreferenceHelper.setBoolean(Constants.Iab.getPref(), false);
        }
    }

    @Subscribe
    public void onDonationStartedEvent(final DonationStartedEvent event) {
        if (event == null) { return; }

        final String sku = event.getSku();
        final int reqCode = event.getReqCode();
        final String token = event.getToken();
        logDebug("IAB: sku: " + sku
                + " | reqCode: " + String.valueOf(reqCode)
                + " | token: " + token);
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

    @Override
    protected void onActivityResult(final int req, final int res, final Intent data) {
        if (!mHelper.handleActivityResult(req, res, data)) {
            super.onActivityResult(req, res, data);
        }
    }

    @Subscribe
    public void onSubFragmentEvent(final SubFragmentEvent event) {
        if (event == null) return;

        Fragment main = null;
        final int id = event.getId();

        Fragment right = HelpFragment.newInstance(id);

        switch (id) {
            //--------------------------------------------------------------------------------------
            case ID_FAST_CHARGE:
                main = new FastChargeFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_GOVERNOR_TUNABLE:
                main = new GovernorFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_HOTPLUGGING:
                main = new HotpluggingFragment();
                break;
            case ID_THERMAL:
                main = new ThermalFragment();
                break;
            case ID_VOLTAGE:
                main = new VoltageFragment();
                break;
            //--------------------------------------------------------------------------------------
            case ID_TOOLS_TASKER_LIST:
                main = new TaskListFragment();
                break;
            case ID_TOOLS_VM:
                main = new SysctlFragment();
                break;
            case ID_TOOLS_BUILD_PROP:
                main = new BuildPropFragment();
                break;
            case ID_TOOLS_EDITORS_VM:
                main = new SysctlEditorFragment();
                break;
            case ID_TOOLS_EDITORS_BUILD_PROP:
                main = new BuildPropEditorFragment();
                break;
            case ID_TOOLS_APP_MANAGER:
                main = new AppListFragment();
                break;
            //--------------------------------------------------------------------------------------
            default:
                break;
        }

        if (main == null || right == null) return;

        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.animator.slide_in_right, R.animator.slide_out_right,
                R.animator.slide_in_left, R.animator.slide_out_left);

        ft.replace(R.id.container, main);
        ft.replace(R.id.menu_frame, right);
        ft.addToBackStack(null);

        ft.commit();
    }

}
