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
package org.namelessrom.devicecontrol.ui.fragments.tools;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Legend;
import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.AppDetailsActivity;
import org.namelessrom.devicecontrol.listeners.OnAppChoosenListener;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.objects.PackageObserver;
import org.namelessrom.devicecontrol.ui.adapters.AppListAdapter;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.AnimationHelper;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends AttachFragment implements DeviceConstants,
        OnAppChoosenListener, PackageObserver.OnPackageStatsListener {

    private static final int DIALOG_TYPE_DISABLE   = 0;
    private static final int DIALOG_TYPE_UNINSTALL = 1;

    private final Handler mHandler = new Handler();

    private boolean mDetailsShowing     = false;
    private boolean startedFromActivity = false;

    //==============================================================================================
    private AppItem             mAppItem;
    private RecyclerView        mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private AppListAdapter      mAdapter;

    //==============================================================================================
    private FrameLayout  mAppDetails;
    private View         mAppDetailsContainer;
    private View         mAppDetailsError;
    private LinearLayout mProgressContainer;

    //==============================================================================================
    private ImageView mAppIcon;
    private TextView  mAppLabel;
    private TextView  mAppPackage;
    private View      mAppLayer;

    //----------------------------------------------------------------------------------------------
    private TextView     mStatus;
    private TextView     mAppCode;
    private TextView     mAppVersion;
    private PieChart     mCacheGraph;
    private LinearLayout mCacheInfo;

    @Override protected int getFragmentId() { return ID_TOOLS_APP_MANAGER; }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_app_details, menu);

        if (mAppItem == null || (!startedFromActivity && !mDetailsShowing)) {
            menu.removeItem(R.id.menu_app_kill);
            menu.removeItem(R.id.menu_app_clear_cache);
            menu.removeItem(R.id.menu_app_clear_data);
            menu.removeItem(R.id.menu_action_play_store);
            menu.removeItem(R.id.menu_app_disable);
            menu.removeItem(R.id.menu_app_uninstall);
            return;
        } else {
            if (!AppHelper.isPlayStoreInstalled()) {
                menu.removeItem(R.id.menu_action_play_store);
            }
            // prevent uninstalling lovely packages
            if (mAppItem.getPackageName().contains("org.namelessrom")) {
                menu.removeItem(R.id.menu_app_uninstall);
                // do not allow to disable our own app
                if (TextUtils.equals(mAppItem.getPackageName(),
                        Application.get().getPackageName())) {
                    menu.removeItem(R.id.menu_app_disable);
                }
            }
        }

        final MenuItem appKill = menu.findItem(R.id.menu_app_kill);
        if (appKill != null) {
            appKill.setEnabled(AppHelper.isAppRunning(mAppItem.getPackageName()));
        }

        final MenuItem disable = menu.findItem(R.id.menu_app_disable);
        if (disable != null) {
            disable.setTitle(mAppItem.isEnabled() ? R.string.disable : R.string.enable);
        }
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        // get the id of our item
        final int id = item.getItemId();

        // if the user hit refresh
        if (id == R.id.menu_action_refresh) {
            // TODO: animate?
            if (mDetailsShowing || startedFromActivity) {
                refreshAppDetails();
            } else {
                new LoadApps().execute();
            }
            return true;
        }

        // the below code requires mAppItem to be NOT NULL, so return early if it is null
        if (mAppItem == null) { return false; }

        switch (id) {
            case R.id.menu_action_play_store: {
                AppHelper.showInPlaystore("market://details?id=" + mAppItem.getPackageName());
                return true;
            }
            case R.id.menu_app_kill: {
                killApp();
                return true;
            }
            case R.id.menu_app_clear_cache: {
                clearAppCache();
                return true;
            }
            case R.id.menu_app_clear_data: {
                clearAppData();
                return true;
            }
            case R.id.menu_app_disable: {
                disableApp();
                return true;
            }
            case R.id.menu_app_uninstall: {
                uninstallApp();
                return true;
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
                final PackageManager pm = Application.get().getPackageManager();
                PackageInfo info = null;
                try {
                    info = pm.getPackageInfo(packageName, 0);
                } catch (Exception ignored) { }
                if (info != null && info.applicationInfo != null) {
                    mAppItem = new AppItem(info, String.valueOf(info.applicationInfo.loadLabel(pm)),
                            info.applicationInfo.loadIcon(pm));
                }
            }
        }

        final View appDetails = inflater.inflate(R.layout.fragment_app_details, container, false);

        assert (appDetails != null);

        mAppDetailsContainer = appDetails.findViewById(R.id.app_details_container);
        mAppDetailsError = appDetails.findViewById(R.id.app_details_error);

        mAppIcon = (ImageView) appDetails.findViewById(R.id.app_icon);
        mAppLabel = (TextView) appDetails.findViewById(R.id.app_label);
        mAppPackage = (TextView) appDetails.findViewById(R.id.app_package);
        mAppLayer = appDetails.findViewById(R.id.app_layer);
        mStatus = (TextView) appDetails.findViewById(R.id.app_status);
        mAppCode = (TextView) appDetails.findViewById(R.id.app_version_code);
        mAppVersion = (TextView) appDetails.findViewById(R.id.app_version_name);
        mCacheGraph = (PieChart) appDetails.findViewById(R.id.app_cache_graph);
        mCacheInfo = (LinearLayout) appDetails.findViewById(R.id.app_cache_info_container);

        if (startedFromActivity) {
            return appDetails;
        } else {
            final View space = appDetails.findViewById(R.id.app_space);
            if (space != null) space.setVisibility(View.VISIBLE);
        }

        final View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);
        mAppDetails = (FrameLayout) rootView.findViewById(R.id.app_details);
        mProgressContainer = (LinearLayout) rootView.findViewById(R.id.progressContainer);
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

        final int color = PreferenceHelper.getBoolean("dark_theme", true)
                ? Application.get().getColor(R.color.dark_background)
                : Application.get().getColor(R.color.light_background);
        mCacheGraph.setBackgroundColor(color);
        mCacheGraph.setDrawPaintColor(color);
        mCacheGraph.setHolePaintColor(color);
        mCacheGraph.setValuePaintColor(color);

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

    @Override public boolean onBackPressed() {
        if (!startedFromActivity && mDetailsShowing) {
            // animate the details out
            hideAppDetails(null);
            return true;
        }
        return super.onBackPressed();
    }

    private void hideAppDetails(final Animator.AnimatorListener animationListener) {
        final ArrayList<ObjectAnimator> animators = new ArrayList<>();
        final AnimatorSet animatorSet = new AnimatorSet();
        final ObjectAnimator outAnim = ObjectAnimator.ofFloat(mAppDetails, "x",
                mAppIcon.getWidth() + 2 * AnimationHelper.getDp(R.dimen.app_margin),
                mAppDetails.getWidth());
        outAnim.setDuration(500);
        animators.add(outAnim);
        final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mAppDetails, "alpha", 1f, 0f);
        alphaAnim.setDuration(500);
        animators.add(alphaAnim);
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(animators.toArray(new ObjectAnimator[animators.size()]));
        if (animationListener != null) {
            animatorSet.addListener(animationListener);
        }
        animatorSet.start();
        mDetailsShowing = false;
        invalidateOptionsMenu();
    }

    @Override public void onAppChoosen(final AppItem appItem) {
        mAppItem = appItem;
        refreshAppDetails();
    }

    private void killApp() {
        AppHelper.killProcess(mAppItem.getPackageName());
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                invalidateOptionsMenu();
            }
        }, 500);
    }

    private void disableApp() {
        showConfirmationDialog(DIALOG_TYPE_DISABLE);
    }

    private void uninstallApp() {
        showConfirmationDialog(DIALOG_TYPE_UNINSTALL);
    }

    private void clearAppData() {
        AppHelper.clearData(mAppItem.getPackageName());
        mHandler.postDelayed(mClearRunnable, 500);
    }

    private void clearAppCache() {
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
            mAppLayer.setVisibility(mAppItem.isEnabled() ? View.INVISIBLE : View.VISIBLE);

            if (mAppItem.isSystemApp()) {
                tmp = getString(R.string.app_system, mAppItem.getLabel());
                mStatus.setTextColor(getResources().getColor(R.color.red_middle));
            } else {
                tmp = getString(R.string.app_user, mAppItem.getLabel());
                final int color = PreferenceHelper.getBoolean("dark_theme", true)
                        ? Color.WHITE : Color.BLACK;
                mStatus.setTextColor(color);
            }
            mStatus.setText(Html.fromHtml(tmp));

            mAppCode.setText(
                    getString(R.string.app_version_code, mAppItem.getPackageInfo().versionCode));

            mAppVersion.setText(
                    getString(R.string.app_version_name, mAppItem.getPackageInfo().versionName));

            AppHelper.getSize(this, mAppItem.getPackageName());
        }

        if (!startedFromActivity && !mDetailsShowing) {
            mAppDetails.bringToFront();
            // animate the details in
            final ArrayList<ObjectAnimator> animators = new ArrayList<>();
            final AnimatorSet animatorSet = new AnimatorSet();
            final ObjectAnimator outAnim = ObjectAnimator.ofFloat(mAppDetails, "x",
                    mAppDetails.getWidth(),
                    mAppIcon.getWidth() + 2 * AnimationHelper.getDp(R.dimen.app_margin));
            outAnim.setDuration(500);
            animators.add(outAnim);
            final ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mAppDetails, "alpha", 0f, 1f);
            alphaAnim.setDuration(500);
            animators.add(alphaAnim);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.playTogether(animators.toArray(new ObjectAnimator[animators.size()]));
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
            invalidateOptionsMenu();
        } else {
            mCacheGraph.animateXY(700, 700);
        }

        invalidateOptionsMenu();
    }

    private void showConfirmationDialog(final int type) {
        if (mAppItem == null || getActivity() == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String message;
        final int positiveButton;
        switch (type) {
            case DIALOG_TYPE_DISABLE: {
                message = getString(mAppItem.isEnabled()
                        ? R.string.disable_msg : R.string.enable_msg, mAppItem.getLabel());
                positiveButton = mAppItem.isEnabled() ? R.string.disable : R.string.enable;
                break;
            }
            case DIALOG_TYPE_UNINSTALL: {
                message = getString(R.string.uninstall_msg, mAppItem.getLabel());
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
                                        disable();
                                        break;
                                    }
                                    case DIALOG_TYPE_UNINSTALL: {
                                        uninstall();
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

    private void disable() {
        if (mAppItem == null) return;

        String cmd;
        if (mAppItem.isEnabled()) {
            cmd = "pm disable " + mAppItem.getPackageName() + " 2> /dev/null";
        } else {
            cmd = "pm enable " + mAppItem.getPackageName() + " 2> /dev/null";
        }

        final CommandCapture commandCapture =
                new CommandCapture(new DisableHandler(mAppItem), cmd) {
                    @Override public void commandCompleted(int id, int exitcode) {
                        super.commandCompleted(id, exitcode);
                    }

                    @Override public void commandTerminated(int id, String reason) {
                        super.commandTerminated(id, reason);
                    }
                };

        try {
            RootTools.getShell(true).add(commandCapture);
        } catch (Exception ignored) { /* ignored */ }
    }

    private void uninstall() {
        // build our command
        final StringBuilder sb = new StringBuilder();
        if (mAppItem.isSystemApp()) {
            sb.append("busybox mount -o rw,remount /system;");
        } else {
            sb.append(String.format("pm uninstall %s;", mAppItem.getPackageName()));
        }

        sb.append(String.format("rm -rf %s;", mAppItem.getApplicationInfo().sourceDir));
        sb.append(String.format("rm -rf %s;", mAppItem.getApplicationInfo().dataDir));

        if (mAppItem.isSystemApp()) {
            sb.append(String.format("pm uninstall %s;", mAppItem.getPackageName()));
            sb.append("busybox mount -o ro,remount /system;");
        }

        final String cmd = sb.toString();
        Logger.v(this, cmd);

        // create the dialog (will not be shown for a long amount of time though)
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.uninstalling);
        dialog.setMessage(getString(R.string.applying_wait));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        new AsyncTask<Void, Void, Void>() {

            @Override protected void onPreExecute() {
                dialog.show();
            }

            @Override protected Void doInBackground(Void... voids) {
                Utils.runRootCommand(cmd, true);
                return null;
            }

            @Override protected void onPostExecute(Void aVoid) {
                dialog.dismiss();

                if (getActivity() != null) {
                    Toast.makeText(getActivity(),
                            getString(R.string.uninstall_success, mAppItem.getLabel()),
                            Toast.LENGTH_SHORT).show();
                }
                // uninstalled, so lets tell it our app item is null
                mAppItem = null;

                // if we are started by the activity, refresh app details
                if (startedFromActivity) {
                    refreshAppDetails();
                } else {
                    // else hide the app details and refresh once hidden
                    hideAppDetails(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animator) { }

                        @Override public void onAnimationEnd(Animator animator) {
                            new LoadApps().execute();
                        }

                        @Override public void onAnimationCancel(Animator animator) { }

                        @Override public void onAnimationRepeat(Animator animator) { }
                    });
                }
            }
        }.execute();
    }

    @Override public void onPackageStats(final PackageStats packageStats) {
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
            sliceList.add(new Entry(
                    packageStats.externalMediaSize + packageStats.externalObbSize, 2));
            // Cache -------------------------------------------------------------------------------
            textList.add(getString(R.string.cache));
            sliceList.add(new Entry(packageStats.cacheSize + packageStats.externalCacheSize, 3));

            final PieDataSet dataSet = new PieDataSet(sliceList, getString(R.string.app_size));
            dataSet.setSliceSpace(5f);
            dataSet.setColors(ColorTemplate.createColors(getActivity(),
                    ColorTemplate.VORDIPLOM_COLORS));

            final PieData data = new PieData(textList, dataSet);
            mCacheGraph.setData(data);

            mCacheGraph.highlightValues(null);

            mCacheGraph.setCenterText(String.format("%s\n%s", getString(R.string.total),
                    AppHelper.convertSize(totalSize)));

            // setup legend
            final Legend l = mCacheGraph.getLegend();
            l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
            l.setXEntrySpace(7f);
            l.setYEntrySpace(5f);

            final int color = PreferenceHelper.getBoolean("dark_theme", true)
                    ? Color.WHITE : Color.BLACK;
            l.setTextColor(color);

            // we are ready
            mCacheGraph.invalidate();
        }
    }

    private View addCacheWidget(final int txtId, final String text) {
        final View v;
        if (getActivity() != null) {
            v = LayoutInflater.from(getActivity())
                    .inflate(R.layout.widget_app_cache, mCacheInfo, false);
        } else {
            v = ((LayoutInflater) Application.get()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.widget_app_cache, mCacheInfo, false);
        }

        final int color = PreferenceHelper.getBoolean("dark_theme", true)
                ? Color.WHITE : Color.BLACK;

        final TextView tvLeft = (TextView) v.findViewById(R.id.widget_app_cache_left);
        tvLeft.setTextColor(color);
        final TextView tvRight = (TextView) v.findViewById(R.id.widget_app_cache_right);
        tvRight.setTextColor(color);

        tvLeft.setText(Application.get().getString(txtId) + ':');
        tvRight.setText(text);

        return v;
    }

    private final Runnable mClearRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                AppHelper.getSize(AppListFragment.this, mAppItem.getPackageName());
            } catch (Exception e) { Logger.e(this, "AppHelper.getSize(): " + e); }
        }
    };

    private void invalidateOptionsMenu() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private class DisableHandler extends Handler {
        private static final int COMMAND_OUTPUT     = 0x01;
        private static final int COMMAND_COMPLETED  = 0x02;
        private static final int COMMAND_TERMINATED = 0x03;

        private final AppItem item;

        public DisableHandler(final AppItem appItem) {
            this.item = appItem;
        }

        @Override public void handleMessage(final Message msg) {
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
                    item.setEnabled(!item.isEnabled());
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    if (mAppLayer != null) {
                        mAppLayer.setVisibility(item.isEnabled() ? View.INVISIBLE : View.VISIBLE);
                    }
                    invalidateOptionsMenu();
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
            final PackageManager pm = Application.get().getPackageManager();
            final List<AppItem> appList = new ArrayList<>();
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
            invalidateOptionsMenu();
        }
    }

}
