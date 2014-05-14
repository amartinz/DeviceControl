package org.namelessrom.devicecontrol;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class AppDetailActivity extends Activity {

    private final Handler        mHandler = new Handler();
    private final PackageManager mPm      = Application.getPm();

    @InjectView(R.id.app_icon)    ImageView appIcon;
    @InjectView(R.id.app_label)   TextView  appLabel;
    @InjectView(R.id.app_package) TextView  appPackage;

    @InjectView(R.id.app_status)               TextView     mStatus;
    @InjectView(R.id.app_kill)                 Button       mKillApp;
    @InjectView(R.id.app_disabler)             Button       mDisabler;
    @InjectView(R.id.app_cache_graph)          BarGraph     mCacheGraph;
    @InjectView(R.id.app_cache_info_container) LinearLayout mCacheInfo;
    @InjectView(R.id.app_data_clear)           Button       mClearData;
    @InjectView(R.id.app_cache_clear)          Button       mClearCache;

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
        ButterKnife.inject(this);

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

        appIcon.setImageDrawable(mAppItem.getIcon());
        appLabel.setText(mAppItem.getLabel());
        appPackage.setText(mAppItem.getPackageName());

        if (mAppItem.isSystemApp()) {
            tmp = getString(R.string.app_system, mAppItem.getLabel());
            mStatus.setTextColor(getResources().getColor(R.color.red_middle));
        } else {
            tmp = getString(R.string.app_user, mAppItem.getLabel());
        }
        mStatus.setText(Html.fromHtml(tmp));

        if (!AppHelper.isAppRunning(this, mAppItem.getPackageName())) {
            mKillApp.setEnabled(false);
        }

        if (mAppItem.getPackageName().contains("org.namelessrom")) {
            mDisabler.setEnabled(false);
        }
        mDisabler.setText(mAppItem.isEnabled() ? R.string.disable : R.string.enable);

        try {
            AppHelper.getSize(mPm, mAppItem.getPackageName());
        } catch (Exception e) { logDebug("AppHelper.getSize(): " + e); }
    }

    @OnClick(R.id.app_kill) void killApp() {
        mKillApp.setEnabled(false);
        AppHelper.killApp(mAppItem.getPackageName());
        mHandler.postDelayed(mKillRunnable, 500);
    }

    @OnClick(R.id.app_disabler) void disableApp() {
        showConfirmationDialog();
    }

    @OnClick(R.id.app_data_clear) void clearAppData() {
        mClearCache.setEnabled(false);
        mClearData.setEnabled(false);
        AppHelper.clearData(mAppItem.getPackageName());
        mHandler.postDelayed(mClearRunnable, 500);
    }

    @OnClick(R.id.app_cache_clear) void cleanAppCache() {
        mClearCache.setEnabled(false);
        mClearData.setEnabled(false);
        AppHelper.clearCache(mAppItem.getPackageName());
        mHandler.postDelayed(mClearRunnable, 500);
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
        final View v = getLayoutInflater()
                .inflate(R.layout.widget_app_cache, mCacheInfo, false);

        final TextView tvLeft = ButterKnife.findById(v, R.id.widget_app_cache_left);
        final TextView tvRight = ButterKnife.findById(v, R.id.widget_app_cache_right);

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
