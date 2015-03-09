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
package org.namelessrom.devicecontrol.appmanager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.ui.adapters.AppListAdapter;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends AttachFragment implements DeviceConstants {
    private static final int ANIM_DURATION = 250;

    private RecyclerView mRecyclerView;
    private LinearLayout mProgressContainer;

    private boolean mIsLoading;

    @Override protected int getFragmentId() { return ID_TOOLS_APP_MANAGER; }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        // get the id of our item
        final int id = item.getItemId();

        // if the user hit refresh
        if (id == R.id.menu_action_refresh) {
            loadApps(true);
            return true;
        }

        return false;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_app_list, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);
        mProgressContainer = (LinearLayout) rootView.findViewById(R.id.progressContainer);
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

    private void invalidateOptionsMenu() {
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void loadApps(boolean animate) {
        if (mIsLoading) return;

        mIsLoading = true;
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

    private class LoadApps extends AsyncTask<Void, Void, List<AppItem>> {
        @Override protected List<AppItem> doInBackground(Void... params) {
            final PackageManager pm = getActivity().getPackageManager();
            final List<AppItem> appList = new ArrayList<>();
            final List<PackageInfo> pkgInfos = pm.getInstalledPackages(0);

            for (final PackageInfo pkgInfo : pkgInfos) {
                if (pkgInfo.applicationInfo == null) {
                    continue;
                }
                appList.add(new AppItem(pkgInfo,
                        String.valueOf(pkgInfo.applicationInfo.loadLabel(pm)),
                        pkgInfo.applicationInfo.loadIcon(pm)));
            }
            Collections.sort(appList, SortHelper.sAppComparator);

            return appList;
        }

        @Override protected void onPostExecute(final List<AppItem> appItems) {
            final ObjectAnimator anim = ObjectAnimator.ofFloat(mProgressContainer, "alpha", 1f, 0f);
            anim.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) { }

                @Override public void onAnimationEnd(Animator animation) {
                    if (appItems != null) {
                        final AppListAdapter adapter = new AppListAdapter(getActivity(), appItems);
                        mRecyclerView.setAdapter(adapter);
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

}
