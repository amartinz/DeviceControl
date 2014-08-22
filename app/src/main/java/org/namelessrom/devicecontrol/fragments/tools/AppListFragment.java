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
package org.namelessrom.devicecontrol.fragments.tools;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.AppDetailsActivity;
import org.namelessrom.devicecontrol.adapters.AppListAdapter;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.events.listeners.OnAppChoosenListener;
import org.namelessrom.devicecontrol.events.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.AnimationHelper;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static butterknife.ButterKnife.findById;

public class AppListFragment extends AttachFragment implements DeviceConstants,
        View.OnClickListener, OnAppChoosenListener, OnBackPressedListener {

    private final Handler mHandler            = new Handler();
    private       boolean mDetailsShowing     = false;
    private       boolean startedFromActivity = false;

    private AppItem             mAppItem;
    private RecyclerView        mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private AppListAdapter      mAdapter;
    //==============================================================================================
    private FrameLayout         mAppDetails;
    private View                mAppDetailsContainer;
    private View                mAppDetailsError;
    private LinearLayout        mProgressContainer;
    //==============================================================================================
    private ImageView           mAppIcon;
    private TextView            mAppLabel;
    private TextView            mAppPackage;
    //----------------------------------------------------------------------------------------------
    private TextView            mStatus;
    private Button              mKillApp;
    private Button              mDisabler;
    private TextView            mAppCode;
    private TextView            mAppVersion;
    private PieChart            mCacheGraph;
    private LinearLayout        mCacheInfo;
    private Button              mClearData;
    private Button              mClearCache;
    //==============================================================================================


    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_APP_MANAGER);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (!startedFromActivity && mDetailsShowing && AppHelper.isPlayStoreInstalled()
                && mAppItem != null) {
            inflater.inflate(R.menu.menu_app_details, menu);
        }
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            case R.id.menu_action_play_store: {
                if (mAppItem != null) {
                    AppHelper.showInPlaystore("market://details?id=" + mAppItem.getPackageName());
                }
                return true;
            }
            default: {
                break;
            }
        }

        return false;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        if (bundle != null) {
            final String packageName = bundle.getString(AppDetailsActivity.ARG_PACKAGE_NAME);
            startedFromActivity = (packageName != null && !packageName.isEmpty());
            if (startedFromActivity) {
                PackageInfo info = null;
                try {
                    info = Application.getPm().getPackageInfo(packageName, 0);
                } catch (Exception ignored) { }
                if (info != null && info.applicationInfo != null) {
                    mAppItem = new AppItem(info,
                            String.valueOf(info.applicationInfo.loadLabel(Application.getPm())),
                            info.applicationInfo.loadIcon(Application.getPm()));
                }
            }
        }

        final View appDetails = inflater.inflate(R.layout.fragment_app_details, container, false);

        assert (appDetails != null);

        mAppDetailsContainer = findById(appDetails, R.id.app_details_container);
        mAppDetailsError = findById(appDetails, R.id.app_details_error);

        mAppIcon = findById(appDetails, R.id.app_icon);
        mAppLabel = findById(appDetails, R.id.app_label);
        mAppPackage = findById(appDetails, R.id.app_package);
        mStatus = findById(appDetails, R.id.app_status);
        mKillApp = findById(appDetails, R.id.app_kill);
        mDisabler = findById(appDetails, R.id.app_disabler);
        mAppCode = findById(appDetails, R.id.app_version_code);
        mAppVersion = findById(appDetails, R.id.app_version_name);
        mCacheGraph = findById(appDetails, R.id.app_cache_graph);
        mCacheInfo = findById(appDetails, R.id.app_cache_info_container);
        mClearData = findById(appDetails, R.id.app_data_clear);
        mClearCache = findById(appDetails, R.id.app_cache_clear);

        mKillApp.setOnClickListener(this);
        mDisabler.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mClearData.setOnClickListener(this);

        if (startedFromActivity) {
            return appDetails;
        } else {
            final View space = findById(appDetails, R.id.app_space);
            if (space != null) space.setVisibility(View.VISIBLE);
        }

        final View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
        mRecyclerView = findById(rootView, android.R.id.list);
        mAppDetails = findById(rootView, R.id.app_details);
        mProgressContainer = findById(rootView, R.id.progressContainer);
        if (startedFromActivity) mProgressContainer.setVisibility(View.GONE);
        mAppDetails.addView(appDetails);
        return rootView;
    }

    private void setupCacheGraph() {
        mCacheGraph.setDescription("");

        mCacheGraph.setHoleRadius(60f);
        mCacheGraph.setDrawHoleEnabled(true);
        mCacheGraph.setTransparentCircleRadius(65f);

        mCacheGraph.setDrawXValues(false);
        mCacheGraph.setDrawYValues(false);
        mCacheGraph.setDrawCenterText(true);

        mCacheGraph.setTouchEnabled(false);

        final int grey = Application.getColor(R.color.grey_dark);
        mCacheGraph.setBackgroundColor(grey);
        mCacheGraph.setDrawPaintColor(grey);
        mCacheGraph.setHolePaintColor(grey);
        mCacheGraph.setValuePaintColor(grey);

        mCacheGraph.invalidate();
    }

    @Override public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        setupCacheGraph();

        if (startedFromActivity) {
            refreshAppDetails();
        } else {
            mRecyclerView.setHasFixedSize(true);
            mLinearLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
            new LoadApps().execute();
        }
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.app_kill:
                killApp();
                break;
            case R.id.app_disabler:
                disableApp();
                break;
            case R.id.app_cache_clear:
                clearAppCache();
                break;
            case R.id.app_data_clear:
                clearAppData();
                break;
        }
    }

    @Override public boolean onBackPressed() {
        if (!startedFromActivity && mDetailsShowing) {
            // animate the details out
            final AnimatorSet animatorSet = new AnimatorSet();
            final ObjectAnimator outAnim = ObjectAnimator.ofFloat(mAppDetails, "x",
                    mAppIcon.getWidth() + 2 * AnimationHelper.getDp(R.dimen.app_margin),
                    mAppDetails.getWidth());
            outAnim.setDuration(500);
            final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mAppDetails, "alpha", 1f, 0f);
            alphaAnim.setDuration(500);
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(outAnim, alphaAnim);
            animatorSet.start();
            mDetailsShowing = false;
            if (getActivity() != null) getActivity().invalidateOptionsMenu();
            return true;
        }
        return false;
    }

    @Override public void onAppChoosen(final AppItem appItem) {
        mAppItem = appItem;
        refreshAppDetails();
    }

    private void killApp() {
        mKillApp.setEnabled(false);
        AppHelper.killProcess(mAppItem.getPackageName());
        mHandler.postDelayed(mKillRunnable, 500);
    }

    private void disableApp() {
        final Activity activity = getActivity();
        if (activity == null) return;
        showConfirmationDialog(activity);
    }

    private void clearAppData() {
        mClearCache.setEnabled(false);
        mClearData.setEnabled(false);
        AppHelper.clearData(mAppItem.getPackageName());
        mHandler.postDelayed(mClearRunnable, 500);
    }

    private void clearAppCache() {
        mClearCache.setEnabled(false);
        mClearData.setEnabled(false);
        AppHelper.clearCache(mAppItem.getPackageName());
        mHandler.postDelayed(mClearRunnable, 500);
    }

    private void refreshAppDetails() {
        if (mAppItem == null) {
            mAppDetailsContainer.setVisibility(View.GONE);
            mAppDetailsError.setVisibility(View.VISIBLE);
        } else {
            mAppDetailsContainer.setVisibility(View.VISIBLE);
            mAppDetailsError.setVisibility(View.GONE);
            String tmp;

            mAppIcon.setImageDrawable(mAppItem.getIcon());
            mAppLabel.setText(mAppItem.getLabel());
            mAppPackage.setText(mAppItem.getPackageName());

            if (mAppItem.isSystemApp()) {
                tmp = getString(R.string.app_system, mAppItem.getLabel());
                mStatus.setTextColor(getResources().getColor(R.color.red_middle));
            } else {
                tmp = getString(R.string.app_user, mAppItem.getLabel());
                mStatus.setTextColor(getResources().getColor(R.color.default_color));
            }
            mStatus.setText(Html.fromHtml(tmp));

            if (!AppHelper.isAppRunning(mAppItem.getPackageName())) {
                mKillApp.setEnabled(false);
            } else {
                mKillApp.setEnabled(true);
            }

            if (mAppItem.getPackageName().contains("org.namelessrom")) {
                mDisabler.setEnabled(false);
            } else {
                mDisabler.setEnabled(true);
            }
            mDisabler.setText(mAppItem.isEnabled() ? R.string.disable : R.string.enable);

            mAppCode.setText(
                    getString(R.string.app_version_code, mAppItem.getPackageInfo().versionCode));

            mAppVersion.setText(
                    getString(R.string.app_version_name, mAppItem.getPackageInfo().versionName));

            try {
                AppHelper.getSize(mAppItem.getPackageName());
            } catch (Exception e) { Logger.e(this, "AppHelper.getSize(): " + e); }
        }

        if (!startedFromActivity && !mDetailsShowing) {
            mAppDetails.bringToFront();
            // animate the details in
            final AnimatorSet animatorSet = new AnimatorSet();
            final ObjectAnimator outAnim = ObjectAnimator.ofFloat(mAppDetails, "x",
                    mAppDetails.getWidth(),
                    mAppIcon.getWidth() + 2 * AnimationHelper.getDp(R.dimen.app_margin));
            outAnim.setDuration(500);
            final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mAppDetails, "alpha", 0f, 1f);
            alphaAnim.setDuration(500);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.playTogether(outAnim, alphaAnim);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animator) {
                    mCacheGraph.setVisibility(View.INVISIBLE);
                }

                @Override public void onAnimationEnd(Animator animator) {
                    mCacheGraph.setVisibility(View.VISIBLE);
                    mCacheGraph.animateXY(700, 700);
                }

                @Override public void onAnimationCancel(Animator animator) { }

                @Override public void onAnimationRepeat(Animator animator) { }
            });
            animatorSet.start();
            mDetailsShowing = true;
            if (getActivity() != null) getActivity().invalidateOptionsMenu();
        } else {
            mCacheGraph.animateXY(700, 700);
        }
    }

    private void showConfirmationDialog(final Activity activity) {
        if (mAppItem == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(getString(mAppItem.isEnabled()
                ? R.string.disable_msg : R.string.enable_msg, mAppItem.getLabel()))
                .setPositiveButton(mAppItem.isEnabled() ? R.string.disable : R.string.enable,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                disable();
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

    private void disable() {
        if (mAppItem == null) return;

        if (mDisabler != null) {
            mDisabler.setEnabled(false);
        }

        String cmd;
        if (mAppItem.isEnabled()) {
            cmd = "pm disable " + mAppItem.getPackageName() + " 2> /dev/null";
        } else {
            cmd = "pm enable " + mAppItem.getPackageName() + " 2> /dev/null";
        }

        final CommandCapture commandCapture =
                new CommandCapture(new DisableHandler(mAppItem), cmd) {
                    @Override
                    public void commandCompleted(int id, int exitcode) {
                        super.commandCompleted(id, exitcode);
                    }

                    @Override
                    public void commandTerminated(int id, String reason) {
                        BusProvider.getBus().post(new ShellOutputEvent(-1, "", ""));
                    }
                };

        try {
            RootTools.getShell(true).add(commandCapture);
        } catch (Exception ignored) { /* ignored */ }
    }

    @Subscribe public void onPackageStats(final PackageStats packageStats) {
        Logger.i(this, "onAppSizeEvent()");

        if (packageStats == null) return;

        final long totalSize = packageStats.codeSize + packageStats.dataSize
                + packageStats.externalCodeSize + packageStats.externalDataSize
                + packageStats.externalMediaSize + packageStats.externalObbSize
                + packageStats.cacheSize + packageStats.externalCacheSize;

        if (mCacheInfo != null) {
            mCacheInfo.removeAllViews();

            mCacheInfo.addView(addCacheWidget(R.string.total,
                    AppHelper.convertSize(totalSize)));
            mCacheInfo.addView(addCacheWidget(R.string.app,
                    AppHelper.convertSize(packageStats.codeSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_app,
                    AppHelper.convertSize(packageStats.externalCodeSize)));
            mCacheInfo.addView(addCacheWidget(R.string.data,
                    AppHelper.convertSize(packageStats.dataSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_data,
                    AppHelper.convertSize(packageStats.externalDataSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_media,
                    AppHelper.convertSize(packageStats.externalMediaSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_obb,
                    AppHelper.convertSize(packageStats.externalObbSize)));
            mCacheInfo.addView(addCacheWidget(R.string.cache,
                    AppHelper.convertSize(packageStats.cacheSize)));
            mCacheInfo.addView(addCacheWidget(R.string.ext_cache,
                    AppHelper.convertSize(packageStats.externalCacheSize)));
        }

        if (mCacheGraph != null) {
            final ArrayList<Entry> sliceList = new ArrayList<Entry>();
            final ArrayList<String> textList = new ArrayList<String>();
            // App ---------------------------------------------------------------------------------
            textList.add(getString(R.string.app));
            sliceList.add(new Entry(packageStats.codeSize + packageStats.externalCodeSize, 0));
            // Data --------------------------------------------------------------------------------
            textList.add(getString(R.string.data));
            sliceList.add(new Entry(packageStats.dataSize + packageStats.externalDataSize, 1));
            // External ------------------------------------------------------------------------
            textList.add(getString(R.string.ext));
            sliceList.add(new Entry(
                    packageStats.externalMediaSize + packageStats.externalObbSize, 2));
            // Cache -------------------------------------------------------------------------------
            textList.add(getString(R.string.cache));
            sliceList.add(new Entry(packageStats.cacheSize + packageStats.externalCacheSize, 3));

            final PieDataSet dataSet = new PieDataSet(sliceList, getString(R.string.app_size));
            dataSet.setSliceSpace(5f);
            dataSet.setColors(ColorTemplate.createColors(Application.applicationContext,
                    ColorTemplate.VORDIPLOM_COLORS));

            final PieData data = new PieData(textList, dataSet);
            mCacheGraph.setData(data);

            mCacheGraph.highlightValues(null);

            mCacheGraph.setCenterText(String.format("%s\n%s", getString(R.string.total),
                    AppHelper.convertSize(totalSize)));

            // we are ready
            mCacheGraph.invalidate();

            // setup legend
            final Legend l = mCacheGraph.getLegend();
            l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(5f);
        }
    }

    private View addCacheWidget(final int txtId, final String text) {
        final View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final TextView tvLeft = findById(v, R.id.widget_app_cache_left);
        tvLeft.setTextColor(getResources().getColor(R.color.default_color));
        final TextView tvRight = findById(v, R.id.widget_app_cache_right);
        tvRight.setTextColor(getResources().getColor(R.color.default_color));

        tvLeft.setText(getString(txtId) + ':');
        tvRight.setText(text);

        return v;
    }

    private final Runnable mClearRunnable = new Runnable() {
        @Override
        public void run() {
            if (mClearCache != null) {
                mClearCache.setEnabled(true);
            }
            if (mClearData != null) {
                mClearData.setEnabled(true);
            }
            if (mKillApp != null) {
                mKillApp.setEnabled(AppHelper.isAppRunning(mAppItem.getPackageName()));
            }
            try {
                AppHelper.getSize(mAppItem.getPackageName());
            } catch (Exception e) { Logger.e(this, "AppHelper.getSize(): " + e); }
        }
    };

    private final Runnable mKillRunnable = new Runnable() {
        @Override
        public void run() {
            if (mKillApp != null) {
                mKillApp.setEnabled(AppHelper.isAppRunning(mAppItem.getPackageName()));
            }
        }
    };

    private class DisableHandler extends Handler {
        private static final int COMMAND_OUTPUT     = 0x01;
        private static final int COMMAND_COMPLETED  = 0x02;
        private static final int COMMAND_TERMINATED = 0x03;

        private final AppItem appItem;

        public DisableHandler(final AppItem appItem) {
            this.appItem = appItem;
        }

        @Override
        public void handleMessage(final Message msg) {
            final Bundle data = msg.getData();
            final int action;
            if (data != null) {
                action = msg.getData().getInt("action");
            } else {
                action = 0x00;
            }
            switch (action) {
                case COMMAND_COMPLETED:
                case COMMAND_TERMINATED:
                    if (mDisabler != null) {
                        appItem.setEnabled(!appItem.isEnabled());
                        mDisabler.setEnabled(true);
                        mDisabler.setText(appItem.isEnabled()
                                ? R.string.disable : R.string.enable);
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                case COMMAND_OUTPUT:
                    break;
            }
        }
    }

    private class LoadApps extends AsyncTask<Void, Void, List<AppItem>> {
        @Override protected List<AppItem> doInBackground(Void... params) {
            if (startedFromActivity) return null;
            final PackageManager pm = Application.getPm();
            final List<AppItem> appList = new ArrayList<AppItem>();
            final List<PackageInfo> pkgInfos = pm.getInstalledPackages(0);

            ApplicationInfo appInfo;
            for (final PackageInfo pkgInfo : pkgInfos) {
                appInfo = pkgInfo.applicationInfo;
                if (appInfo == null) { continue; }
                appList.add(new AppItem(
                        pkgInfo, String.valueOf(appInfo.loadLabel(pm)), appInfo.loadIcon(pm)));
            }
            Collections.sort(appList, SortHelper.sAppComparator);

            return appList;
        }

        @Override protected void onPostExecute(final List<AppItem> appItems) {
            if (appItems != null && isAdded()) {
                if (mProgressContainer != null) {
                    mProgressContainer.setVisibility(View.GONE);
                }
                mAdapter = new AppListAdapter(AppListFragment.this, appItems);
                mRecyclerView.setAdapter(mAdapter);
                AnimationHelper.animateX(mAppDetails, 0, 0, mAppDetails.getWidth());
            }
        }
    }

}
