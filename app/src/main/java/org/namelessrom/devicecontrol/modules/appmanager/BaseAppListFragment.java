/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.views.CustomRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import at.amartinz.execution.NormalShell;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public abstract class BaseAppListFragment extends Fragment implements SearchView.OnQueryTextListener, SearchView.OnCloseListener, View.OnClickListener {
    private static final int ANIM_DURATION = 450;

    private AppListAdapter mAdapter;

    private CustomRecyclerView mRecyclerView;
    private TextView mEmptyView;
    private LinearLayout mProgressContainer;

    private final HashSet<AppItem> mSelectedApps = new HashSet<>();
    private HorizontalScrollView mAppListBar;

    private boolean mIsLoading;

    public interface AppSelectedListener {
        void onAppSelected(String packageName, ArrayList<AppItem> selectedApps);
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_app_list, menu);

        // setup search
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (searchItem != null ? (SearchView) searchItem.getActionView() : null);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override public boolean onQueryTextChange(String s) {
        if (mAdapter != null) {
            mAdapter.filter(s);
            updateVisibility(mAdapter.getItemCount() <= 0);
        }
        return true;
    }

    @Override public boolean onClose() {
        if (mAdapter != null) {
            mAdapter.filter(null);
            updateVisibility(mAdapter.getItemCount() <= 0);
        }
        return false;
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.menu_action_refresh) {
            loadApps(true);
            return true;
        }

        return false;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
        mRecyclerView = (CustomRecyclerView) rootView.findViewById(android.R.id.list);
        mEmptyView = (TextView) rootView.findViewById(android.R.id.empty);
        mProgressContainer = (LinearLayout) rootView.findViewById(R.id.progressContainer);

        mAppListBar = (HorizontalScrollView) rootView.findViewById(R.id.app_bar);
        rootView.findViewById(R.id.app_bar_uninstall).setOnClickListener(this);
        rootView.findViewById(R.id.app_bar_enable).setOnClickListener(this);
        rootView.findViewById(R.id.app_bar_disable).setOnClickListener(this);
        return rootView;
    }

    @Override public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        mRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override public void onResume() {
        super.onResume();
        loadApps(false);
    }

    @Override public void onDestroy() {
        super.onDestroy();
    }

    private void invalidateOptionsMenu() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.app_bar_uninstall:
            case R.id.app_bar_enable:
            case R.id.app_bar_disable: {
                showActionDialog(id);
                break;
            }
        }
    }

    private void showActionDialog(final int type) {
        int title;
        String message;

        switch (type) {
            default:
            case R.id.app_bar_uninstall: {
                title = R.string.uninstall;
                message = getString(R.string.uninstall_msg_multi, mSelectedApps.size());
                break;
            }
            case R.id.app_bar_enable: {
                title = R.string.enable;
                if (mSelectedApps.size() > 1) {
                    message = getString(R.string.enable_msg_multi, mSelectedApps.size());
                } else {
                    message = getString(R.string.enable_msg_single);
                }
                break;
            }
            case R.id.app_bar_disable: {
                title = R.string.disable;
                if (mSelectedApps.size() > 1) {
                    message = getString(R.string.disable_msg_multi, mSelectedApps.size());
                } else {
                    message = getString(R.string.disable_msg_single);
                }
                break;
            }
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                showProcessingDialog(type);
            }
        });
        builder.show();
    }

    private void showProcessingDialog(final int type) {
        int titleResId;
        int messageResId;

        switch (type) {
            default:
            case R.id.app_bar_uninstall: {
                titleResId = R.string.uninstall;
                messageResId = R.string.uninstall_msg_multi_action;
                break;
            }
            case R.id.app_bar_enable: {
                titleResId = R.string.enable;
                messageResId = R.string.enable_msg_multi_action;
                break;
            }
            case R.id.app_bar_disable: {
                titleResId = R.string.disable;
                messageResId = R.string.disable_msg_multi_action;
                break;
            }
        }

        new ProcessTask(getActivity(), type, titleResId, messageResId, mSelectedApps).execute();
    }

    private class ProcessTask extends AsyncTask<Void, Integer, Void> {
        private final Activity activity;
        private final int type;
        private final int messageResId;
        private final HashSet<AppItem> selectedApps;
        private final int length;
        private final ProgressDialog progressDialog;

        public ProcessTask(Activity activity, int type, int titleResId, int messageResId, HashSet<AppItem> selectedApps) {
            this.activity = activity;
            this.type = type;
            this.messageResId = messageResId;
            this.selectedApps = selectedApps;
            this.length = this.selectedApps.size();

            progressDialog = new ProgressDialog(this.activity);
            progressDialog.setTitle(titleResId);
            progressDialog.setMessage(activity.getString(messageResId, 0, this.length));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMax(this.length);
            progressDialog.setProgress(0);
        }

        @Override protected void onPreExecute() {
            progressDialog.show();
        }

        @Override protected void onProgressUpdate(Integer... values) {
            // increase current app counter by one as we deliver indexes
            final int currentApp = values[0] + 1;
            progressDialog.setMessage(activity.getString(messageResId, currentApp, this.length));
            progressDialog.setProgress(currentApp);
        }

        @Override protected Void doInBackground(Void... params) {
            int counter = 0;
            for (AppItem appItem : selectedApps) {
                publishProgress(counter);
                switch (type) {
                    case R.id.app_bar_uninstall: {
                        appItem.uninstall(activity, null, false);
                        break;
                    }
                    case R.id.app_bar_enable: {
                        appItem.enable(null);
                        break;
                    }
                    case R.id.app_bar_disable: {
                        appItem.disable(null);
                        break;
                    }
                }
                counter++;
            }

            // wait for 750 ms to let everything update its state
            try {
                Thread.sleep(750);
            } catch (Exception ignored) { }
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            progressDialog.hide();
            loadApps(true);
            Snackbar.make(BaseAppListFragment.this.mAppListBar, R.string.action_completed, Snackbar.LENGTH_LONG).show();
        }
    }

    public void loadApps(final boolean animate) {
        if (mIsLoading) {
            return;
        }

        mIsLoading = true;
        mProgressContainer.post(new Runnable() {
            @Override public void run() {
                mProgressContainer.setVisibility(View.VISIBLE);

                if (animate) {
                    final ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressContainer, "alpha", 0f, 1f);
                    anim.addListener(new Animator.AnimatorListener() {
                        @Override public void onAnimationStart(Animator animation) { }

                        @Override public void onAnimationEnd(Animator animation) {
                            new LoadApps().execute();
                        }

                        @Override public void onAnimationCancel(Animator animation) { }

                        @Override public void onAnimationRepeat(Animator animation) { }
                    });
                    anim.setDuration(ANIM_DURATION);
                    anim.start();
                } else {
                    mProgressContainer.setAlpha(1f);
                    new LoadApps().execute();
                }
            }
        });
    }

    private void updateVisibility(boolean isEmpty) {
        mRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        mEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    protected abstract boolean isFiltered(@NonNull ApplicationInfo applicationInfo);

    private class LoadApps extends AsyncTask<Void, Void, ArrayList<AppItem>> {
        @Override protected ArrayList<AppItem> doInBackground(Void... params) {
            final PackageManager pm = App.get().getPackageManager();
            final ArrayList<AppItem> appList = new ArrayList<>();
            final List<PackageInfo> pkgInfos = new ArrayList<>();

            List<PackageInfo> tmp = getInstalledPackages(pm);
            if (tmp == null || tmp.isEmpty()) {
                tmp = getInstalledPackagesShell(pm);
            }
            pkgInfos.addAll(tmp);

            for (final PackageInfo pkgInfo : pkgInfos) {
                if (pkgInfo.applicationInfo == null || isFiltered(pkgInfo.applicationInfo)) {
                    continue;
                }
                appList.add(new AppItem(pkgInfo, String.valueOf(pkgInfo.applicationInfo.loadLabel(pm))));
            }
            Collections.sort(appList, SortHelper.sAppComparator);

            return appList;
        }

        @DebugLog @Nullable private List<PackageInfo> getInstalledPackages(PackageManager pm) {
            try {
                return pm.getInstalledPackages(0);
            } catch (Exception exc) {
                Timber.e(exc, "Could not get installed packages via package manager, falling back...");
            }
            return null;
        }

        @DebugLog @NonNull private List<PackageInfo> getInstalledPackagesShell(PackageManager pm) {
            final List<PackageInfo> pkgInfos = new ArrayList<>();
            final List<String> cmdResultList = NormalShell.fireAndBlockList("pm list packages");
            if (cmdResultList != null && !cmdResultList.isEmpty()) {
                for (final String cmdResult : cmdResultList) {
                    if (TextUtils.isEmpty(cmdResult)) {
                        continue;
                    }
                    final String pkgName = cmdResult.substring(cmdResult.indexOf(":") + 1);
                    try {
                        final PackageInfo pkgInfo = pm.getPackageInfo(pkgName, 0);
                        pkgInfos.add(pkgInfo);
                    } catch (Exception ignored) { }
                }
            }
            return pkgInfos;
        }

        @Override protected void onPostExecute(final ArrayList<AppItem> appItems) {
            final ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressContainer, "alpha", 1f, 0f);
            anim.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {
                    final boolean isEmpty = (appItems == null || appItems.size() <= 0);
                    updateVisibility(isEmpty);
                }

                @Override public void onAnimationEnd(Animator animation) {
                    if (appItems != null) {
                        if (mAdapter == null) {
                            final AppListAdapter adapter = new AppListAdapter(getActivity(), BaseAppListFragment.this,
                                    appItems, mUninstallListener, mAppSelectedListener);
                            mRecyclerView.setAdapter(adapter);
                            mAdapter = adapter;
                        } else {
                            mAdapter.refill(appItems);
                        }
                    }

                    mProgressContainer.setVisibility(View.GONE);
                    mIsLoading = false;
                }

                @Override public void onAnimationCancel(Animator animation) { }

                @Override public void onAnimationRepeat(Animator animation) { }
            });
            anim.setDuration(ANIM_DURATION);
            anim.start();

            invalidateOptionsMenu();
        }
    }

    private final AppItem.UninstallListener mUninstallListener = new AppItem.UninstallListener() {
        @Override public void OnUninstallComplete() {
            loadApps(true);
        }
    };

    private final AppSelectedListener mAppSelectedListener = new AppSelectedListener() {
        @Override public void onAppSelected(String packageName, ArrayList<AppItem> selectedApps) {
            mSelectedApps.clear();
            mSelectedApps.addAll(selectedApps);
            if (mSelectedApps.size() == 0) {
                mAppListBar.setVisibility(View.GONE);
            } else {
                mAppListBar.setVisibility(View.VISIBLE);
            }
        }
    };

}
