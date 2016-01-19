/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.pollfish.constants.Position;
import com.pollfish.main.PollFish;
import com.sense360.android.quinoa.lib.Sense360;

import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.modules.about.AboutFragment;
import org.namelessrom.devicecontrol.modules.appmanager.AppListFragment;
import org.namelessrom.devicecontrol.modules.bootup.BootupFragment;
import org.namelessrom.devicecontrol.modules.cpu.CpuSettingsFragment;
import org.namelessrom.devicecontrol.modules.cpu.GovernorFragment;
import org.namelessrom.devicecontrol.modules.device.DeviceFeatureFragment;
import org.namelessrom.devicecontrol.modules.info.DeviceInfoFragment;
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
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.execution.ShellManager;
import alexander.martinz.vendor.Configuration;

public class MainActivity extends BaseActivity implements ActivityCallbacks, NavigationView.OnNavigationItemSelectedListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final Object lockObject = new Object();

    private static long mBackPressed;
    private Toast mToast;

    private Runnable mDrawerRunnable;
    private DrawerLayout mDrawerLayout;

    public static boolean sDisableFragmentAnimations;

    private Fragment mCurrentFragment;

    private int mTitle = R.string.home;
    private int mFragmentTitle = R.string.home;
    private int mSubFragmentTitle = -1;

    private CheckRequirementsTask mCheckRequirementsTask;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================


    @Override protected void setupTheme() {
        final boolean isLightTheme = AppResources.get().isLightTheme();
        setTheme(isLightTheme ? R.style.AppTheme_Light_Translucent : R.style.AppTheme_Dark_Translucent);
    }

    @Override protected void onResume() {
        super.onResume();
        if (DeviceConfig.get().showPollfish) {
            final String pfApiKey = Configuration.getPollfishApiKeyDc();
            if (!TextUtils.equals("---", pfApiKey)) {
                Logger.v(this, "PollFish.init()");
                PollFish.init(this, pfApiKey, Position.BOTTOM_RIGHT, 50);
            }
        }

        // TODO: verify on non sim device
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        final String simCountryIso = telephonyManager.getSimCountryIso().toLowerCase();
        Logger.v(this, "SimCountryIso: %s", simCountryIso);
        if (BuildConfig.DEBUG || "us".equals(simCountryIso)) {
            // TODO: configurable
            Sense360.start(getApplicationContext());
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupToolbar();
        setupMaterialMenu(this);

        // lock the drawer so we can only open it AFTER we are done with our checks
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override public void onDrawerClosed(View drawerView) {
                if (mDrawerRunnable != null) {
                    mDrawerLayout.post(mDrawerRunnable);
                }
            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view_content);
        mNavigationView.setNavigationItemSelectedListener(this);

        loadFragmentPrivate(DeviceConstants.ID_HOME, false);
        getSupportFragmentManager().executePendingTransactions();

        mCheckRequirementsTask = new CheckRequirementsTask(this);
        mCheckRequirementsTask.setPostExecuteHook(new Runnable() {
            @Override public void run() {
                setupDrawerItems();
            }
        });
        mCheckRequirementsTask.execute();
    }

    private void setupToolbar() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar, 4.0f);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mSubFragmentTitle == -1) {
                    toggleDrawer();
                } else {
                    onCustomBackPressed(true);
                }
            }
        });
    }

    private void setupDrawerItems() {
        // manually check home drawer entry
        mPreviousMenuItem = findMenuItem(R.id.nav_item_home);
        if (mPreviousMenuItem != null) {
            mPreviousMenuItem.setChecked(true);
        }

        if (!Utils.fileExists(getString(R.string.directory_msm_thermal))
            && !Utils.fileExists(getString(R.string.file_intelli_thermal_base))) {
            final MenuItem thermalItem = findMenuItem(R.id.nav_item_controls_thermal);
            if (thermalItem != null) {
                thermalItem.setVisible(false);
            }
        }

        final View headerView = mNavigationView.getHeaderView(0);
        final ImageView headerImage = (ImageView) headerView.findViewById(R.id.drawer_header_image);
        headerImage.setImageResource(AppResources.get().getDrawerHeaderResId());
        final ImageButton headerSettings = (ImageButton) headerView.findViewById(R.id.drawer_header_settings);
        headerSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                shouldLoadFragment(DeviceConstants.ID_PREFERENCES);
            }
        });

        final FrameLayout footerContainer = (FrameLayout) findViewById(R.id.navigation_view_footer_container);
        ViewCompat.setElevation(footerContainer, 4f);
        final CheckedTextView footerAppVersion = (CheckedTextView) findViewById(R.id.nav_item_footer_version);
        footerAppVersion.setDuplicateParentStateEnabled(true);

        PackageInfo myInfo = null;
        try {
            myInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException ignored) { }

        if (myInfo != null && !TextUtils.isEmpty(myInfo.versionName)) {
            footerAppVersion.setText(myInfo.versionName);

            // extract the git short log from the version name
            final String versionName = myInfo.versionName.replace("-dev", "").trim().toLowerCase();
            if (versionName.contains("-git-")) {
                final String[] splitted = versionName.split("-git-");
                if (splitted.length == 2) {
                    final String commitUrl = String.format(Constants.GITHUB_DC_COMMIT_URL_BASE, splitted[1]);
                    // preheat a bit
                    ((Application) getApplicationContext()).getCustomTabsHelper().mayLaunchUrl(commitUrl);
                    footerContainer.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            ((Application) getApplicationContext()).getCustomTabsHelper().launchUrl(MainActivity.this, commitUrl);
                        }
                    });
                }
            }
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        restoreActionBar();
        return super.onCreateOptionsMenu(menu);
    }

    private void restoreActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
        }
    }

    @Override public void toggleDrawer() {
        if (mDrawerLayout == null) {
            return;
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override public boolean closeDrawerIfShowing() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    @Override public boolean onNavigationItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        shouldLoadFragment(id);
        return true;
    }

    private void onCustomBackPressed(final boolean animatePressed) {
        // toggle menu if it is showing and return
        if (closeDrawerIfShowing()) {
            return;
        }

        // if we have a OnBackPressedListener at the fragment, go in
        if (mCurrentFragment instanceof OnBackPressedListener) {
            final OnBackPressedListener listener = ((OnBackPressedListener) mCurrentFragment);

            // if our listener handles onBackPressed(), return
            if (listener.onBackPressed()) {
                Logger.v(this, "onBackPressed() handled by current fragment");
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

            // we can separate actionbar back actions and back key presses
            if (animatePressed) {
                mMaterialMenu.animatePressedState(iconState);
            } else {
                mMaterialMenu.animateState(iconState);
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
            if (mToast != null) {
                mToast.cancel();
            }
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
                ShellManager.get().cleanupShells();
            } catch (Exception e) {
                Logger.e(this, String.format("onDestroy(): %s", e));
            }
        }
        if (mCheckRequirementsTask != null) {
            mCheckRequirementsTask.destroy();
        }
        super.onDestroy();
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public void setFragment(final Fragment fragment) {
        if (fragment == null) {
            return;
        }
        Logger.v(this, "setFragment: %s", fragment.getId());
        mCurrentFragment = fragment;
    }

    @Override public void shouldLoadFragment(final int id) {
        shouldLoadFragment(id, false);
    }

    @Override public void shouldLoadFragment(final int id, final boolean onResume) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            // close drawer
            mDrawerLayout.closeDrawer(GravityCompat.START);

            mDrawerRunnable = new Runnable() {
                @Override public void run() {
                    loadFragmentPrivate(id, onResume);

                    mDrawerRunnable = null;
                }
            };
            return;
        }

        loadFragmentPrivate(id, onResume);
    }

    private void loadFragmentPrivate(final int i, final boolean onResume) {
        switch (i) {
            default: // slip through...
                //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_HOME:
                if (!onResume) {
                    mCurrentFragment = new AboutFragment();
                }
                mTitle = mFragmentTitle = R.string.app_name;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_INFO_DEVICE:
                if (!onResume) {
                    mCurrentFragment = new DeviceInfoFragment();
                }
                mTitle = mFragmentTitle = R.string.device;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_CTRL_DEVICE:
                if (!onResume) {
                    mCurrentFragment = new DeviceFeatureFragment();
                }
                mTitle = mFragmentTitle = R.string.features;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_FAST_CHARGE:
                if (!onResume) {
                    mCurrentFragment = new FastChargeFragment();
                }
                mTitle = mSubFragmentTitle = R.string.fast_charge;
                break;
            case DeviceConstants.ID_SOUND_CONTROL:
                if (!onResume) {
                    mCurrentFragment = new SoundControlFragment();
                }
                mTitle = mSubFragmentTitle = R.string.sound_control;
                break;
            case DeviceConstants.ID_KSM:
                if (!onResume) {
                    mCurrentFragment = new KsmFragment();
                }
                mTitle = mSubFragmentTitle = R.string.ksm;
                break;
            case DeviceConstants.ID_UKSM:
                if (!onResume) {
                    mCurrentFragment = new UksmFragment();
                }
                mTitle = mSubFragmentTitle = R.string.uksm;
                break;
            case DeviceConstants.ID_VOLTAGE:
                if (!onResume) {
                    mCurrentFragment = new VoltageFragment();
                }
                mTitle = mSubFragmentTitle = R.string.voltage_control;
                break;
            case DeviceConstants.ID_ENTROPY:
                if (!onResume) {
                    mCurrentFragment = new EntropyFragment();
                }
                mTitle = mSubFragmentTitle = R.string.entropy;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_INFO_PERFORMANCE:
                if (!onResume) {
                    mCurrentFragment = new InformationFragment();
                }
                mTitle = mFragmentTitle = R.string.information;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_CTRL_PROCESSOR:
                if (!onResume) {
                    mCurrentFragment = new CpuSettingsFragment();
                }
                mTitle = mFragmentTitle = R.string.cpusettings;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_GOVERNOR_TUNABLE:
                if (!onResume) {
                    mCurrentFragment = new GovernorFragment();
                }
                mTitle = mSubFragmentTitle = R.string.cpu_governor_tuning;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_CTRL_GRAPHICS:
                if (!onResume) {
                    mCurrentFragment = new GpuSettingsFragment();
                }
                mTitle = mFragmentTitle = R.string.gpusettings;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_CTRL_FILE_SYSTEM:
                if (!onResume) {
                    mCurrentFragment = new FilesystemFragment();
                }
                mTitle = mFragmentTitle = R.string.file_system;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_IOSCHED_TUNING:
                if (!onResume) {
                    mCurrentFragment = new IoSchedConfigFragment();
                }
                mTitle = mSubFragmentTitle = R.string.io;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_CTRL_THERMAL:
                if (!onResume) {
                    mCurrentFragment = new ThermalFragment();
                }
                mTitle = mFragmentTitle = R.string.thermal;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_BOOTUP_RESTORATION:
                if (!onResume) {
                    mCurrentFragment = new BootupFragment();
                }
                mTitle = mFragmentTitle = R.string.bootup_restoration;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_APP_MANAGER:
                if (!onResume) {
                    mCurrentFragment = new AppListFragment();
                }
                mTitle = mFragmentTitle = R.string.app_manager;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_TASKER:
                if (!onResume) {
                    mCurrentFragment = new TaskerFragment();
                }
                mTitle = mFragmentTitle = R.string.tasker;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_FLASHER:
                if (!onResume) {
                    mCurrentFragment = new FlasherFragment();
                }
                mTitle = mFragmentTitle = R.string.flasher;
                mSubFragmentTitle = -1;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_TOOLS_MORE:
                if (!onResume) {
                    mCurrentFragment = new ToolsMoreFragment();
                }
                mTitle = mFragmentTitle = R.string.more;
                mSubFragmentTitle = -1;
                break;
            case DeviceConstants.ID_TOOLS_VM:
                if (!onResume) {
                    mCurrentFragment = new SysctlFragment();
                }
                mTitle = mSubFragmentTitle = R.string.sysctl_vm;
                break;
            case DeviceConstants.ID_TOOLS_EDITORS_VM:
                if (!onResume) {
                    mCurrentFragment = new SysctlEditorFragment();
                }
                mTitle = mSubFragmentTitle = R.string.sysctl_vm;
                break;
            case DeviceConstants.ID_TOOLS_EDITORS_BUILD_PROP:
                if (!onResume) {
                    mCurrentFragment = new BuildPropEditorFragment();
                }
                mTitle = mSubFragmentTitle = R.string.buildprop;
                break;
            case DeviceConstants.ID_TOOLS_WIRELESS_FM:
                if (!onResume) {
                    mCurrentFragment = new WirelessFileManagerFragment();
                }
                mTitle = mSubFragmentTitle = R.string.wireless_file_manager;
                break;
            //--------------------------------------------------------------------------------------
            case DeviceConstants.ID_PREFERENCES:
                if (!onResume) {
                    mCurrentFragment = new PreferencesFragment();
                }
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
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_left);
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

        mMaterialMenu.animateState(iconState);
    }

}
