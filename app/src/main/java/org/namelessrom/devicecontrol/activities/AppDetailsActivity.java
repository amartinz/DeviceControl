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
package org.namelessrom.devicecontrol.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.objects.PackageObserver;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class AppDetailsActivity extends BaseActivity implements PackageObserver.OnPackageStatsListener {
    public static final String ARG_FROM_ACTIVITY = "arg_from_activity";
    public static final String ARG_PACKAGE_NAME = "arg_package_name";

    private static final int DIALOG_TYPE_DISABLE = 0;
    private static final int DIALOG_TYPE_UNINSTALL = 1;

    private static final Handler mHandler = new Handler();

    private AppItem mAppItem;

    private View mAppDetailsContainer;
    private View mAppDetailsError;

    private ImageView mAppIcon;
    private TextView mAppLabel;
    private TextView mAppPackage;
    private View mAppLayer;

    private TextView mStatus;
    private TextView mAppCode;
    private TextView mAppVersion;
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

        mAppDetailsContainer = findViewById(R.id.app_details_container);
        mAppDetailsError = findViewById(R.id.app_details_error);

        findViewById(R.id.item_app).setSelected(false);

        mAppIcon = (ImageView) findViewById(R.id.app_icon);
        mAppLabel = (TextView) findViewById(R.id.app_label);
        mAppPackage = (TextView) findViewById(R.id.app_package);
        mAppLayer = findViewById(R.id.app_layer);
        mStatus = (TextView) findViewById(R.id.app_status);
        mAppCode = (TextView) findViewById(R.id.app_version_code);
        mAppVersion = (TextView) findViewById(R.id.app_version_name);
        mCacheGraph = (PieChart) findViewById(R.id.app_cache_graph);
        mCacheInfo = (LinearLayout) findViewById(R.id.app_cache_info_container);
    }

    @Override protected void onResume() {
        super.onResume();
        final String packageName = getTargetPackageName(getIntent());
        if (!TextUtils.isEmpty(packageName)) {
            final PackageManager pm = Application.get().getPackageManager();
            PackageInfo info = null;
            try {
                info = pm.getPackageInfo(packageName, 0);
            } catch (Exception ignored) { }
            if (info != null && info.applicationInfo != null) {
                mAppItem = new AppItem(info,
                        String.valueOf(info.applicationInfo.loadLabel(pm)),
                        info.applicationInfo.loadIcon(pm));
            }
        }

        setupCacheGraph();
        refreshAppDetails();
    }

    @Nullable private String getTargetPackageName(@Nullable Intent intent) {
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
        Logger.i(this, "packageName: %s", String.valueOf(packageName));
        return packageName;
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        new MenuInflater(this).inflate(R.menu.menu_app_details, menu);

        if (mAppItem != null) {
            if (!AppHelper.isPlayStoreInstalled()) {
                menu.removeItem(R.id.menu_action_play_store);
            }
            // prevent disabling and uninstalling Device Control
            if (Application.get().getPackageName().equals(mAppItem.getPackageName())) {
                menu.removeItem(R.id.menu_app_uninstall);
                menu.removeItem(R.id.menu_app_disable);
            }

            final MenuItem appKill = menu.findItem(R.id.menu_app_kill);
            if (appKill != null) {
                appKill.setEnabled(AppHelper.isAppRunning(mAppItem.getPackageName()));
            }

            final MenuItem disable = menu.findItem(R.id.menu_app_disable);
            if (disable != null) {
                disable.setTitle(mAppItem.isEnabled() ? R.string.disable : R.string.enable);
            }
        } else {
            menu.removeItem(R.id.menu_app_kill);
            menu.removeItem(R.id.menu_app_clear_cache);
            menu.removeItem(R.id.menu_app_clear_data);
            menu.removeItem(R.id.menu_action_play_store);
            menu.removeItem(R.id.menu_app_disable);
            menu.removeItem(R.id.menu_app_uninstall);
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
            case R.id.menu_action_play_store: {
                AppHelper.showInPlaystore("market://details?id=" + mAppItem.getPackageName());
                return true;
            }
            case R.id.menu_action_refresh: {
                refreshAppDetails();
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
        return super.onOptionsItemSelected(item);
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

        final int color = Application.get().isDarkTheme()
                ? Application.get().getColor(R.color.dark_background)
                : Application.get().getColor(R.color.light_background);
        mCacheGraph.setBackgroundColor(color);
        mCacheGraph.setDrawPaintColor(color);
        mCacheGraph.setHolePaintColor(color);
        mCacheGraph.setValuePaintColor(color);

        mCacheGraph.invalidate();
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
                final int color = Application.get().isDarkTheme() ? Color.WHITE : Color.BLACK;
                mStatus.setTextColor(color);
            }
            mStatus.setText(Html.fromHtml(tmp));

            mAppCode.setText(
                    getString(R.string.app_version_code, mAppItem.getPackageInfo().versionCode));

            mAppVersion.setText(
                    getString(R.string.app_version_name, mAppItem.getPackageInfo().versionName));

            AppHelper.getSize(this, mAppItem.getPackageName());
        }

        mCacheGraph.animateXY(700, 700);

        invalidateOptionsMenu();
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
        final ProgressDialog dialog;
        dialog = new ProgressDialog(this);
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
                Toast.makeText(AppDetailsActivity.this,
                        getString(R.string.uninstall_success, mAppItem.getLabel()),
                        Toast.LENGTH_SHORT).show();
                mAppItem = null;
                finish();

            }
        }.execute();
    }

    private class DisableHandler extends Handler {
        private static final int COMMAND_OUTPUT = 0x01;
        private static final int COMMAND_COMPLETED = 0x02;
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
            dataSet.setColors(ColorTemplate.createColors(this, ColorTemplate.VORDIPLOM_COLORS));

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

            final int color = Application.get().isDarkTheme() ? Color.WHITE : Color.BLACK;
            l.setTextColor(color);

            // we are ready
            mCacheGraph.invalidate();
        }
    }

    private View addCacheWidget(final int txtId, final String text) {
        final View v;
        v = LayoutInflater.from(this).inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final int color = Application.get().isDarkTheme() ? Color.WHITE : Color.BLACK;

        final TextView tvLeft = (TextView) v.findViewById(R.id.widget_app_cache_left);
        tvLeft.setTextColor(color);
        final TextView tvRight = (TextView) v.findViewById(R.id.widget_app_cache_right);
        tvRight.setTextColor(color);

        tvLeft.setText(Application.get().getString(txtId) + ':');
        tvRight.setText(text);

        return v;
    }

    private final Runnable mClearRunnable = new Runnable() {
        @Override public void run() {
            try {
                AppHelper.getSize(AppDetailsActivity.this, mAppItem.getPackageName());
            } catch (Exception e) { Logger.e(this, "AppHelper.getSize(): " + e); }
        }
    };

    private void showConfirmationDialog(final int type) {
        if (mAppItem == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

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

}
