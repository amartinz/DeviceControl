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

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.DonationStartedEvent;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.fragments.HelpFragment;
import org.namelessrom.devicecontrol.fragments.dynamic.WebViewFragment;
import org.namelessrom.devicecontrol.fragments.DeviceFragment;
import org.namelessrom.devicecontrol.fragments.HomeFragment;
import org.namelessrom.devicecontrol.fragments.PreferencesFragment;
import org.namelessrom.devicecontrol.fragments.TaskerFragment;
import org.namelessrom.devicecontrol.fragments.performance.CpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.ExtrasFragment;
import org.namelessrom.devicecontrol.fragments.performance.GpuSettingsFragment;
import org.namelessrom.devicecontrol.fragments.performance.InformationFragment;
import org.namelessrom.devicecontrol.fragments.tools.EditorTabbedFragment;
import org.namelessrom.devicecontrol.fragments.tools.FreezerTabbedFragment;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.adapters.MenuListArrayAdapter;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class MainActivity extends Activity
        implements DeviceConstants, AdapterView.OnItemClickListener, SlidingMenu.OnClosedListener,
        SlidingMenu.OnOpenedListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final Object lockObject = new Object();
    private static long  back_pressed;
    private        Toast mToast;

    private IabHelper mHelper;

    public static SlidingMenu mSlidingMenu;
    private static final int ID_RESTORE     = 10;
    private static final int ID_FIRST_MENU  = 20;
    private static final int ID_SECOND_MENU = 30;

    private int mTitle         = R.string.home;
    private int mFragmentTitle = R.string.home;

    private static final int ID_HOME                     = 0;
    private static final int ID_DEVICE                   = 2;
    private static final int ID_PERFORMANCE_INFO         = 4;
    private static final int ID_PERFORMANCE_CPU_SETTINGS = 5;
    private static final int ID_PERFORMANCE_GPU_SETTINGS = 6;
    private static final int ID_PERFORMANCE_EXTRA        = 7;
    private static final int ID_TASKER                   = 9;
    private static final int ID_TOOLS_EDITORS            = 10;
    private static final int ID_TOOLS_FREEZER            = 11;
    private static final int ID_PREFERENCES              = 13;
    private static final int ID_LICENSES                 = 14;

    public static final int[] MENU_ICONS = {
            R.drawable.ic_menu_home,
            -1, // Device
            R.drawable.ic_menu_device,
            -1, // Performance
            R.drawable.ic_menu_perf_info,
            R.drawable.ic_menu_perf_cpu,
            R.drawable.ic_menu_perf_gpu,
            R.drawable.ic_menu_perf_extras,
            -1, // Tools
            R.drawable.ic_menu_tasker,
            R.drawable.ic_menu_editor,
            R.drawable.ic_menu_freezer,
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

        Utils.setupDirectories(this);
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

        mSlidingMenu.setOnClosedListener(this);
        mSlidingMenu.setOnOpenedListener(this);

        setUpIab();

        loadFragment(ID_HOME);
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
                mSlidingMenu.toggle(true);
                break;
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
            logDebug("closing shells");
            RootTools.closeAllShells();
            if (mHelper != null) mHelper.dispose();
            mHelper = null;
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadFragment(final int i) {
        Fragment main;
        Fragment right;

        switch (i) {
            default:
            case ID_HOME:
                main = new HomeFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_HOME);
                break;
            case ID_DEVICE:
                main = new DeviceFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_DEVICE);
                break;
            case ID_PERFORMANCE_INFO:
                main = new InformationFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_PERF_INFO);
                break;
            case ID_PERFORMANCE_CPU_SETTINGS:
                main = new CpuSettingsFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_CPU);
                break;
            case ID_PERFORMANCE_GPU_SETTINGS:
                main = new GpuSettingsFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_GPU);
                break;
            case ID_PERFORMANCE_EXTRA:
                main = new ExtrasFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_EXTRAS);
                break;
            case ID_TASKER:
                main = new TaskerFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_TASKER);
                break;
            case ID_TOOLS_EDITORS:
                main = new EditorTabbedFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_EDITORS);
                break;
            case ID_TOOLS_FREEZER:
                main = new FreezerTabbedFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_FREEZER);
                break;
            case ID_PREFERENCES:
                main = new PreferencesFragment();
                right = HelpFragment.newInstance(HelpFragment.TYPE_PREFERENCE);
                break;
            case ID_LICENSES:
                main = WebViewFragment.newInstance(WebViewFragment.TYPE_LICENSES);
                right = HelpFragment.newInstance(HelpFragment.TYPE_LICENSES);
                break;
        }

        final FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        final FragmentTransaction ft = fragmentManager.beginTransaction();

        ft.replace(R.id.container, main);
        ft.replace(R.id.menu_frame, right);

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Subscribe
    public void onSectionAttached(final SectionAttachedEvent event) {
        final int id = event.getId();
        switch (id) {
            case ID_RESTORE:
                mTitle = mFragmentTitle;
                break;
            case ID_FIRST_MENU:
                mTitle = R.string.menu;
                break;
            case ID_SECOND_MENU:
                mTitle = R.string.help;
                break;
            default:
                mTitle = mFragmentTitle = R.string.app_name;
                break;
            case HomeFragment.ID:
                mTitle = mFragmentTitle = R.string.home;
                break;
            case DeviceFragment.ID:
                mTitle = mFragmentTitle = R.string.device;
                break;
            case InformationFragment.ID:
                mTitle = mFragmentTitle = R.string.information;
                break;
            case CpuSettingsFragment.ID:
                mTitle = mFragmentTitle = R.string.cpusettings;
                break;
            case GpuSettingsFragment.ID:
                mTitle = mFragmentTitle = R.string.gpusettings;
                break;
            case ExtrasFragment.ID:
                mTitle = mFragmentTitle = R.string.extras;
                break;
            case TaskerFragment.ID:
                mTitle = mFragmentTitle = R.string.tasker;
                break;
            case EditorTabbedFragment.ID:
                mTitle = mFragmentTitle = R.string.editors;
                break;
            case FreezerTabbedFragment.ID:
                mTitle = mFragmentTitle = R.string.freezer;
                break;
        }
        restoreActionBar();
    }

    public void restoreActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
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
        mHelper = new IabHelper(this, Application.Iab.getKey());
        if (Application.IS_LOG_DEBUG) {
            mHelper.enableDebugLogging(true, "IABDEVICECONTROL");
        }
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                logDebug("IAB: " + result);
                PreferenceHelper.setBoolean(Application.Iab.getPref(), result.isSuccess());
            }
        });
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
    public void onAddFragmentToBackstack(final Fragment f) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, f)
                .addToBackStack(null)
                .commit();
    }

}
