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
package org.namelessrom.devicecontrol.modules.appmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.modules.appmanager.permissions.AppSecurityPermissions;
import org.namelessrom.devicecontrol.utils.AppHelper;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class AppDetailsActivity extends BaseActivity implements PackageStatsObserver.OnPackageStatsListener, View.OnClickListener {
    public static final String ARG_FROM_ACTIVITY = "arg_from_activity";
    public static final String ARG_PACKAGE_NAME = "arg_package_name";

    private static final int DIALOG_TYPE_DISABLE = 0;
    private static final int DIALOG_TYPE_UNINSTALL = 1;

    private static final int ANIM_DUR = 1000;

    private static final Handler mHandler = new Handler();
    private final PackageManager mPm = App.get().getPackageManager();

    private AppItem mAppItem;

    private View mAppDetailsContainer;
    private View mAppDetailsError;

    private LinearLayout mAppContainer;
    private AppIconImageView mAppIcon;
    private TextView mAppLabel;
    private TextView mAppPackage;
    private TextView mAppVersion;

    private Button mForceStop;
    private Button mUninstall;

    private PieChart mCacheGraph;
    private LinearLayout mCacheInfo;

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);

        // Setup toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent i = getIntent();
        if (i != null && i.hasExtra(ARG_FROM_ACTIVITY) && getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final String[] titles = { getString(R.string.app_details) };
        final AppPagerAdapter adapter = new AppPagerAdapter(titles);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        final TabLayout tabHost = (TabLayout) findViewById(R.id.tabHost);
        // TODO: make it visible once used
        tabHost.setVisibility(View.GONE);
        //tabHost.setupWithViewPager(viewPager);

        mAppDetailsContainer = findViewById(R.id.app_details_container);
        mAppDetailsError = findViewById(R.id.app_details_error);

        mAppContainer = (LinearLayout) findViewById(R.id.item_app);

        // allow to launch app via pressing on the icon
        mAppIcon = (AppIconImageView) findViewById(R.id.app_icon);
        mAppIcon.setOnClickListener(this);

        mAppLabel = (TextView) findViewById(R.id.app_label);
        mAppPackage = (TextView) findViewById(R.id.app_package);
        mAppVersion = (TextView) findViewById(R.id.app_version);
        mCacheGraph = (PieChart) findViewById(R.id.app_cache_graph);
        mCacheInfo = (LinearLayout) findViewById(R.id.app_cache_info_container);

        mForceStop = (Button) findViewById(R.id.app_force_stop);
        ViewCompat.setElevation(mForceStop, 2.0f);
        mForceStop.setOnClickListener(this);

        mUninstall = (Button) findViewById(R.id.app_uninstall);
        ViewCompat.setElevation(mUninstall, 2.0f);
        mUninstall.setOnClickListener(this);

        final Button clearCache = (Button) findViewById(R.id.clear_cache);
        ViewCompat.setElevation(clearCache, 2.0f);
        clearCache.setOnClickListener(this);

        final Button clearData = (Button) findViewById(R.id.clear_data);
        ViewCompat.setElevation(clearData, 2.0f);
        clearData.setOnClickListener(this);
    }

    @Override protected void onResume() {
        super.onResume();
        final String packageName = getTargetPackageName(getIntent());
        if (!TextUtils.isEmpty(packageName)) {
            PackageInfo info = null;
            try {
                info = mPm.getPackageInfo(packageName, 0);
            } catch (Exception ignored) { }
            if (info != null && info.applicationInfo != null) {
                mAppItem = new AppItem(info, String.valueOf(info.applicationInfo.loadLabel(mPm)));
            }
        }

        setupCacheGraph();
        refreshAppDetails();
    }

    @DebugLog @Nullable private String getTargetPackageName(@Nullable Intent intent) {
        String packageName = null;
        Bundle args = null;
        if (intent != null) {
            args = intent.getExtras();
            packageName = (args != null) ? args.getString(ARG_PACKAGE_NAME) : null;
        }

        if (packageName == null) {
            intent = (args == null) ? getIntent() : (Intent) args.getParcelable("intent");
            if (intent != null && intent.getData() != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        return packageName;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_app_details, menu);

        if (mAppItem != null) {
            if (!AppHelper.isPlayStoreInstalled()) {
                menu.removeItem(R.id.menu_action_play_store);
            }
            // prevent disabling Device Control
            if (App.get().getPackageName().equals(mAppItem.getPackageName())) {
                menu.removeItem(R.id.menu_app_disable);
            }

            final MenuItem disable = menu.findItem(R.id.menu_app_disable);
            if (disable != null) {
                disable.setTitle(mAppItem.isEnabled() ? R.string.disable : R.string.enable);
            }
        } else {
            menu.removeItem(R.id.menu_action_play_store);
            menu.removeItem(R.id.menu_app_disable);
        }

        return true;
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case android.R.id.home: {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.menu_action_refresh: {
                refreshAppDetails();
                return true;
            }
            case R.id.menu_action_play_store: {
                final String pkgName = (mAppItem != null ? mAppItem.getPackageName() : null);
                if (!TextUtils.isEmpty(pkgName)) {
                    AppHelper.showInPlayStore(pkgName);
                }
                return true;
            }
            case R.id.menu_app_disable: {
                disableApp();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.app_force_stop: {
                forceStopApp();
                break;
            }
            case R.id.app_uninstall: {
                uninstallApp();
                break;
            }
            case R.id.clear_cache: {
                clearAppCache();
                break;
            }
            case R.id.clear_data: {
                clearAppData();
                break;
            }
            case R.id.app_icon: {
                final boolean success = mAppItem.launchActivity(this);
                if (!success) {
                    Snackbar.make(mAppIcon, R.string.could_not_launch_activity, Snackbar.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private void setupCacheGraph() {
        mCacheGraph.setDescription("");

        mCacheGraph.setHoleRadius(60f);
        mCacheGraph.setDrawHoleEnabled(true);
        mCacheGraph.setTransparentCircleRadius(65f);

        mCacheGraph.setDrawSliceText(false);
        mCacheGraph.setDrawMarkerViews(false);
        mCacheGraph.setDrawCenterText(true);

        mCacheGraph.setTouchEnabled(false);
        mCacheGraph.setBackgroundResource(R.color.graph_background);

        mCacheGraph.animateXY(ANIM_DUR, ANIM_DUR);
    }

    private void refreshAppDetails() {
        if (mAppItem == null) {
            mAppDetailsContainer.setVisibility(View.GONE);
            mAppDetailsError.setVisibility(View.VISIBLE);
            invalidateOptionsMenu();
            return;
        }
        mAppDetailsContainer.setVisibility(View.VISIBLE);
        mAppDetailsError.setVisibility(View.GONE);

        mAppIcon.loadImage(mAppItem);
        mAppLabel.setText(mAppItem.getLabel());
        mAppPackage.setText(mAppItem.getPackageName());
        mAppContainer.setBackgroundResource(mAppItem.isEnabled() ? android.R.color.transparent : R.color.darker_gray);

        final int color = mAppItem.isSystemApp()
                ? ContextCompat.getColor(this, R.color.red_middle)
                : ContextCompat.getColor(this, R.color.graph_text_color);
        mAppLabel.setTextColor(color);
        mAppVersion.setText(mAppItem.getVersion());

        setupPermissionsView();

        // prevent uninstalling of Device Control
        mUninstall.setEnabled(!TextUtils.equals(App.get().getPackageName(), mAppItem.getPackageName()));

        AppHelper.getSize(mPm, this, mAppItem.getPackageName());
        refreshAppControls();
        invalidateOptionsMenu();
    }

    private void setupPermissionsView() {
        final LinearLayout permissionsView = (LinearLayout) findViewById(R.id.permissions_section);
        final boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
        if (!supported) {
            permissionsView.setVisibility(View.GONE);
            return;
        }

        AppSecurityPermissions asp = new AppSecurityPermissions(this, mAppItem.getPackageName());
        // Make the security sections header visible
        LinearLayout securityList = (LinearLayout) findViewById(R.id.security_settings_list);
        securityList.removeAllViews();
        securityList.addView(asp.getPermissionsView());
        // If this app is running under a shared user ID with other apps,
        // update the description to explain this.
        String[] packages = mPm.getPackagesForUid(mAppItem.getApplicationInfo().uid);
        if (packages != null && packages.length > 1) {
            final ArrayList<CharSequence> pnames = new ArrayList<>();
            for (final String pkg : packages) {
                if (mAppItem.getPackageName().equals(pkg)) {
                    continue;
                }
                try {
                    ApplicationInfo ainfo = mPm.getApplicationInfo(pkg, 0);
                    pnames.add(ainfo.loadLabel(mPm));
                } catch (PackageManager.NameNotFoundException ignored) { }
            }
            final int N = pnames.size();
            if (N > 0) {
                final Resources res = getResources();
                String appListStr;
                if (N == 1) {
                    appListStr = pnames.get(0).toString();
                } else if (N == 2) {
                    appListStr = res.getString(R.string.join_two_items,
                            pnames.get(0), pnames.get(1));
                } else {
                    appListStr = pnames.get(N - 2).toString();
                    for (int i = N - 3; i >= 0; i--) {
                        appListStr = res.getString(i == 0 ? R.string.join_many_items_first
                                : R.string.join_many_items_middle, pnames.get(i), appListStr);
                    }
                    appListStr = res.getString(R.string.join_many_items_last, appListStr,
                            pnames.get(N - 1));
                }
                final TextView descr = (TextView) findViewById(R.id.security_settings_desc);
                descr.setText(res.getString(R.string.security_settings_desc_multi,
                        mAppItem.getApplicationInfo().loadLabel(mPm), appListStr));
            }
        }

    }

    private void refreshAppControls() {
        mForceStop.setEnabled(mAppItem.isRunning(this));
    }

    private void forceStopApp() {
        mAppItem.forceStop(this);
        Snackbar.make(mAppContainer, R.string.force_stopped_app, Snackbar.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                refreshAppControls();
            }
        }, 500);
    }

    private void disableApp() {
        showConfirmationDialog(DIALOG_TYPE_DISABLE);
    }

    private void uninstallApp() {
        showConfirmationDialog(DIALOG_TYPE_UNINSTALL);
    }

    private void clearAppCache() {
        mAppItem.clearCache(this);
        Snackbar.make(mAppContainer, R.string.cleared_cache, Snackbar.LENGTH_SHORT).show();
        mHandler.postDelayed(mClearRunnable, 500);
    }

    private void clearAppData() {
        mAppItem.clearData(this);
        Snackbar.make(mAppContainer, R.string.cleared_data, Snackbar.LENGTH_SHORT).show();
        mHandler.postDelayed(mClearRunnable, 500);
    }

    private void updateAppEnabled() {
        if (mAppItem == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override public void run() {
                invalidateOptionsMenu();

                final boolean isEnabled = !mAppItem.isEnabled();
                mAppItem.setEnabled(isEnabled);

                if (mAppContainer != null) {
                    mAppContainer.setBackgroundResource(
                            isEnabled ? android.R.color.transparent : R.color.darker_gray);
                }
            }
        });
    }

    @Override public void onPackageStats(final PackageStats packageStats) {
        if (packageStats == null) {
            return;
        }

        final long totalSize = packageStats.codeSize + packageStats.dataSize
                               + packageStats.externalCodeSize + packageStats.externalDataSize
                               + packageStats.externalMediaSize + packageStats.externalObbSize
                               + packageStats.cacheSize + packageStats.externalCacheSize;

        if (mCacheInfo != null) {
            mCacheInfo.removeAllViews();

            mCacheInfo.addView(addCacheWidget(R.string.total, AppHelper.convertSize(totalSize)));
            mCacheInfo.addView(addCacheWidget(R.string.app, AppHelper.convertSize(packageStats.codeSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_app, AppHelper.convertSize(packageStats.externalCodeSize)));
            mCacheInfo.addView(addCacheWidget(R.string.data, AppHelper.convertSize(packageStats.dataSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_data, AppHelper.convertSize(packageStats.externalDataSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_media, AppHelper.convertSize(packageStats.externalMediaSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_obb, AppHelper.convertSize(packageStats.externalObbSize)));
            mCacheInfo.addView(addCacheWidget(R.string.cache, AppHelper.convertSize(packageStats.cacheSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_cache, AppHelper.convertSize(packageStats.externalCacheSize)));
        }

        if (mCacheGraph != null) {
            final ArrayList<Entry> sliceList = new ArrayList<>();
            final ArrayList<String> textList = new ArrayList<>();
            // App ---------------------------------------------------------------------------------
            textList.add(getString(R.string.app));
            sliceList.add(new Entry(packageStats.codeSize + packageStats.externalCodeSize, 0));
            // Data --------------------------------------------------------------------------------
            textList.add(getString(R.string.data));
            sliceList.add(new Entry(packageStats.dataSize + packageStats.externalDataSize, 1));
            // External ------------------------------------------------------------------------
            textList.add(getString(R.string.ext));
            sliceList.add(new Entry(packageStats.externalMediaSize + packageStats.externalObbSize, 2));
            // Cache -------------------------------------------------------------------------------
            textList.add(getString(R.string.cache));
            sliceList.add(new Entry(packageStats.cacheSize + packageStats.externalCacheSize, 3));

            final PieDataSet dataSet = new PieDataSet(sliceList, getString(R.string.app_size));
            // no space to work around a bug
            dataSet.setSliceSpace(0f);
            dataSet.setColors(ColorTemplate.createColors(ColorTemplate.VORDIPLOM_COLORS));

            final PieData data = new PieData(textList, dataSet);
            data.getDataSet().setDrawValues(false);
            mCacheGraph.setData(data);

            mCacheGraph.highlightValues(null);

            mCacheGraph.setCenterText(String.format("%s\n%s", getString(R.string.total), AppHelper.convertSize(totalSize)));

            // setup legend
            final Legend l = mCacheGraph.getLegend();
            l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(5f);

            final int color = ContextCompat.getColor(this, R.color.graph_text_color);
            l.setTextColor(color);

            // we are ready
            mCacheGraph.animateXY(1000, 1000);
        }
    }

    private View addCacheWidget(final int txtId, final String text) {
        final View v = getLayoutInflater().inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final TextView tvLeft = (TextView) v.findViewById(R.id.widget_app_cache_left);
        tvLeft.setText(txtId);

        final TextView tvRight = (TextView) v.findViewById(R.id.widget_app_cache_right);
        tvRight.setText(text);

        return v;
    }

    private final Runnable mClearRunnable = new Runnable() {
        @Override public void run() {
            try {
                AppHelper.getSize(mPm, AppDetailsActivity.this, mAppItem.getPackageName());
            } catch (Exception e) {
                Timber.e(e, "AppHelper.getSize()");
            }
        }
    };

    private void showConfirmationDialog(final int type) {
        if (mAppItem == null) {
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String message;
        final int positiveButton;
        switch (type) {
            case DIALOG_TYPE_DISABLE: {
                message = getString(mAppItem.isEnabled() ? R.string.disable_msg : R.string.enable_msg, mAppItem.getLabel());
                positiveButton = mAppItem.isEnabled() ? R.string.disable : R.string.enable;
                break;
            }
            case DIALOG_TYPE_UNINSTALL: {
                if (mAppItem.isSystemApp()) {
                    message = String.format("%s\n\n%s",
                            getString(R.string.uninstall_msg, mAppItem.getLabel()),
                            getString(R.string.uninstall_msg_system_app));
                } else {
                    message = getString(R.string.uninstall_msg, mAppItem.getLabel());
                }
                positiveButton = android.R.string.yes;
                break;
            }
            default: {
                return;
            }
        }

        builder.setMessage(message)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (type) {
                                    case DIALOG_TYPE_DISABLE: {
                                        mAppItem.disableOrEnable(
                                                new AppItem.DisableEnableListener() {
                                                    @Override public void OnDisabledOrEnabled() {
                                                        updateAppEnabled();
                                                    }
                                                });
                                        break;
                                    }
                                    case DIALOG_TYPE_UNINSTALL: {
                                        mAppItem.uninstall(AppDetailsActivity.this,
                                                new AppItem.UninstallListener() {
                                                    @Override public void OnUninstallComplete() {
                                                        mAppItem = null;
                                                        AppDetailsActivity.this.finish();
                                                    }
                                                });
                                        break;
                                    }
                                    default: {
                                        dialog.dismiss();
                                        break;
                                    }
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.show();
    }

    class AppPagerAdapter extends PagerAdapter {
        private final String[] mTitles;

        public AppPagerAdapter(final String[] titles) {
            mTitles = titles;
        }

        @Override
        public String getPageTitle(final int position) {
            return mTitles[position];
        }

        @Override public Object instantiateItem(ViewGroup container, int position) {
            final int resId;
            switch (position) {
                default:
                case 0: {
                    resId = R.id.page_app_details;
                    break;
                }
                // TODO: make visible once done
                //case 1: {
                //    resId = R.id.page_app_details;
                //    break;
                //}
            }
            return findViewById(resId);
        }

        @Override public int getCount() {
            return mTitles.length;
        }

        @Override public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

}
