package org.namelessrom.devicecontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.ArrayList;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class AppDetailActivity extends Activity {

    private final Handler        mHandler = new Handler();
    private final PackageManager mPm      = Application.getPm();

    private TextView     mStatus;
    private Button       mDisabler;
    private Button       mKillApp;
    private BarGraph     mCacheGraph;
    private LinearLayout mCacheInfo;
    private Button       mClearData;
    private Button       mClearCache;

    private AppItem mAppItem;

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
        setContentView(R.layout.activity_app_details);

        String tmp;
        PackageInfo pkgInfo;
        ApplicationInfo appInfo;
        try {
            final String pkgName = getIntent().getStringExtra("app_item");
            pkgInfo = mPm.getPackageInfo(pkgName, 0);
            appInfo = pkgInfo.applicationInfo;
        } catch (Exception e) {
            pkgInfo = null;
            appInfo = null;
        }

        assert (pkgInfo != null && appInfo != null);

        mAppItem = new AppItem(pkgInfo, String.valueOf(appInfo.loadLabel(mPm)),
                appInfo.loadIcon(mPm));

        setupActionBar();

        final ImageView appIcon = (ImageView) findViewById(R.id.app_icon);
        appIcon.setImageDrawable(mAppItem.getIcon());

        final TextView appLabel = (TextView) findViewById(R.id.app_label);
        appLabel.setText(mAppItem.getLabel());

        final TextView appPackage = (TextView) findViewById(R.id.app_package);
        appPackage.setText(mAppItem.getPackageName());

        mStatus = (TextView) findViewById(R.id.app_status);
        if (mAppItem.isSystemApp()) {
            tmp = getString(R.string.app_system, mAppItem.getLabel());
            mStatus.setTextColor(getResources().getColor(R.color.red_middle));
        } else {
            tmp = getString(R.string.app_user, mAppItem.getLabel());
        }
        mStatus.setText(Html.fromHtml(tmp));

        mKillApp = (Button) findViewById(R.id.app_kill);
        if (!AppHelper.isAppRunning(this, mAppItem.getPackageName())) {
            mKillApp.setEnabled(false);
        }
        mKillApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKillApp.setEnabled(false);
                AppHelper.killApp(mAppItem.getPackageName());
                mHandler.postDelayed(mKillRunnable, 500);
            }
        });

        mDisabler = (Button) findViewById(R.id.app_disabler);
        if (mAppItem.getPackageName().contains("org.namelessrom")) {
            mDisabler.setEnabled(false);
        }
        mDisabler.setText(mAppItem.isEnabled() ? R.string.disable : R.string.enable);
        mDisabler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog();
            }
        });

        mCacheGraph = (BarGraph) findViewById(R.id.app_cache_graph);
        mCacheInfo = (LinearLayout) findViewById(R.id.app_cache_info_container);

        mClearData = (Button) findViewById(R.id.app_data_clear);
        mClearData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearCache.setEnabled(false);
                mClearData.setEnabled(false);
                AppHelper.clearData(mAppItem.getPackageName());
                mHandler.postDelayed(mClearRunnable, 500);
            }
        });

        mClearCache = (Button) findViewById(R.id.app_cache_clear);
        mClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearCache.setEnabled(false);
                mClearData.setEnabled(false);
                AppHelper.clearCache(mAppItem.getPackageName());
                mHandler.postDelayed(mClearRunnable, 500);
            }
        });

        try {
            AppHelper.getSize(mPm, mAppItem.getPackageName());
        } catch (Exception e) { logDebug("AppHelper.getSize(): " + e); }
    }

    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setIcon(mAppItem.getIcon());
        }
    }

    private void showConfirmationDialog() {
        if (mAppItem == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
        final View v = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final TextView tvLeft = (TextView) v.findViewById(R.id.widget_app_cache_left);
        final TextView tvRight = (TextView) v.findViewById(R.id.widget_app_cache_right);

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
                mKillApp.setEnabled(AppHelper.isAppRunning(AppDetailActivity.this,
                        mAppItem.getPackageName()));
            }
            try {
                AppHelper.getSize(mPm, mAppItem.getPackageName());
            } catch (Exception e) { logDebug("AppHelper.getSize(): " + e); }
        }
    };

    private final Runnable mKillRunnable = new Runnable() {
        @Override
        public void run() {
            if (mKillApp != null) {
                mKillApp.setEnabled(AppHelper.isAppRunning(AppDetailActivity.this,
                        mAppItem.getPackageName()));
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
                    break;
                default:
                case COMMAND_OUTPUT:
                    break;
            }
        }
    }

}
