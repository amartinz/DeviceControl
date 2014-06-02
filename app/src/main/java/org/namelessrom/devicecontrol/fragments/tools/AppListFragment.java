package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.AppDetailsActivity;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.events.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.AnimationHelper;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachListFragment;
import org.namelessrom.devicecontrol.widgets.adapters.AppListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.devicecontrol.Application.logDebug;

public class AppListFragment extends AttachListFragment implements DeviceConstants,
        View.OnClickListener, OnBackPressedListener {

    private final Handler mHandler            = new Handler();
    private       boolean mDetailsShowing     = false;
    private       boolean startedFromActivity = false;

    private AppItem        mAppItem;
    private AppListAdapter mAdapter;
    //==============================================================================================
    private FrameLayout    mAppDetails;
    private LinearLayout   mProgressContainer;
    //==============================================================================================
    private ImageView      mAppIcon;
    private TextView       mAppLabel;
    private TextView       mAppPackage;
    //----------------------------------------------------------------------------------------------
    private TextView       mStatus;
    private Button         mKillApp;
    private Button         mDisabler;
    private BarGraph       mCacheGraph;
    private LinearLayout   mCacheInfo;
    private Button         mClearData;
    private Button         mClearCache;
    //==============================================================================================


    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_TOOLS_APP_MANAGER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            default: {
                break;
            }
        }

        return false;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
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

        mAppIcon = findById(appDetails, R.id.app_icon);
        mAppLabel = findById(appDetails, R.id.app_label);
        mAppPackage = findById(appDetails, R.id.app_package);
        mStatus = findById(appDetails, R.id.app_status);
        mKillApp = findById(appDetails, R.id.app_kill);
        mDisabler = findById(appDetails, R.id.app_disabler);
        mCacheGraph = findById(appDetails, R.id.app_cache_graph);
        mCacheInfo = findById(appDetails, R.id.app_cache_info_container);
        mClearData = findById(appDetails, R.id.app_data_clear);
        mClearCache = findById(appDetails, R.id.app_cache_clear);

        mKillApp.setOnClickListener(this);
        mDisabler.setOnClickListener(this);
        mClearCache.setOnClickListener(this);
        mClearData.setOnClickListener(this);

        if (!startedFromActivity) {
            findById(appDetails, R.id.app_space).setVisibility(View.VISIBLE);
        }

        final View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
        mAppDetails = findById(rootView, R.id.app_details);
        mProgressContainer = findById(rootView, R.id.progressContainer);
        if (startedFromActivity) mProgressContainer.setVisibility(View.GONE);
        mAppDetails.addView(appDetails);
        return rootView;
    }

    @Override
    public void onClick(View v) {
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

    @Override
    public boolean onBackPressed() {
        if (!startedFromActivity && mDetailsShowing) {
            AnimationHelper.animateX(mAppDetails, 500, mAppIcon.getWidth() +
                    AnimationHelper.getDp(R.dimen.app_margin), mAppDetails.getWidth());
            mDetailsShowing = false;
            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        if (startedFromActivity) {
            refreshAppDetails();
        } else {
            new LoadApps().execute();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (startedFromActivity || getListAdapter() == null) return;
        mAppItem = (AppItem) getListAdapter().getItem(position);

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
        String tmp;

        mAppIcon.setImageDrawable(mAppItem.getIcon());
        mAppLabel.setText(mAppItem.getLabel());
        mAppPackage.setText(mAppItem.getPackageName());

        if (mAppItem.isSystemApp()) {
            tmp = getString(R.string.app_system, mAppItem.getLabel());
            mStatus.setTextColor(getResources().getColor(R.color.red_middle));
        } else {
            tmp = getString(R.string.app_user, mAppItem.getLabel());
            mStatus.setTextColor(Color.parseColor("#ffffff"));
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

        try {
            AppHelper.getSize(mAppItem.getPackageName());
        } catch (Exception e) { logDebug("AppHelper.getSize(): " + e); }

        if (!startedFromActivity && !mDetailsShowing) {
            mAppDetails.bringToFront();
            AnimationHelper.animateX(mAppDetails, 500, mAppDetails.getWidth(), mAppIcon.getWidth() +
                    2 * AnimationHelper.getDp(R.dimen.app_margin));
            mDetailsShowing = true;
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

    @Subscribe
    public void onPackageStats(final PackageStats packageStats) {
        logDebug("onAppSizeEvent()");

        if (packageStats == null) return;

        if (mCacheInfo != null) {
            mCacheInfo.removeAllViews();

            mCacheInfo.addView(addCacheWidget(R.string.total,
                    AppHelper.convertSize(packageStats.codeSize + packageStats.dataSize
                            + packageStats.externalCodeSize + packageStats.externalDataSize
                            + packageStats.externalMediaSize + packageStats.externalObbSize
                            + packageStats.externalCacheSize)
            ));
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
            final ArrayList<Bar> barList = new ArrayList<Bar>();
            final Resources r = getResources();
            // Total -------------------------------------------------------------------------------
            String text = getString(R.string.total);
            barList.add(createBar(packageStats.codeSize + packageStats.dataSize
                            + packageStats.externalCodeSize + packageStats.externalDataSize
                            + packageStats.externalMediaSize + packageStats.externalObbSize,
                    text, r.getColor(R.color.greenish)
            ));
            // App ---------------------------------------------------------------------------------
            text = getString(R.string.app);
            barList.add(createBar(packageStats.codeSize + packageStats.externalCodeSize,
                    text, r.getColor(R.color.red_middle)));
            // Data --------------------------------------------------------------------------------
            text = getString(R.string.data);
            barList.add(createBar(packageStats.dataSize + packageStats.externalDataSize,
                    text, r.getColor(R.color.orange)));
            // External ------------------------------------------------------------------------
            text = getString(R.string.ext);
            barList.add(createBar(packageStats.externalMediaSize + packageStats.externalObbSize,
                    text, r.getColor(R.color.blueish)));
            // Cache -------------------------------------------------------------------------------
            text = getString(R.string.cache);
            barList.add(createBar(packageStats.cacheSize + packageStats.externalCacheSize,
                    text, r.getColor(R.color.review_green)));
            mCacheGraph.setBars(barList);
        }
    }

    private Bar createBar(final long value, final String text, final int color) {
        final Bar bar = new Bar();
        bar.setName(text);
        bar.setValue(value);
        bar.setValueString(AppHelper.convertSize(value));
        bar.setColor(color);
        return bar;
    }

    private View addCacheWidget(final int txtId, final String text) {
        final View v = Application.getLayoutInflater()
                .inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final TextView tvLeft = findById(v, R.id.widget_app_cache_left);
        final TextView tvRight = findById(v, R.id.widget_app_cache_right);

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
            } catch (Exception e) { logDebug("AppHelper.getSize(): " + e); }
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
        @Override
        protected List<AppItem> doInBackground(Void... params) {
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

        @Override
        protected void onPostExecute(final List<AppItem> appItems) {
            if (appItems != null && isAdded()) {
                if (mProgressContainer != null) {
                    mProgressContainer.setVisibility(View.GONE);
                }
                mAdapter = new AppListAdapter(appItems);
                setListAdapter(mAdapter);
                AnimationHelper.animateX(mAppDetails, 0, 0, mAppDetails.getWidth());
            }
        }
    }

}
