package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachListFragment;
import org.namelessrom.devicecontrol.widgets.adapters.AppListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListFragment extends AttachListFragment implements DeviceConstants {

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
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        new LoadApps().execute();
    }

    private class LoadApps extends AsyncTask<Void, Void, List<AppItem>> {
        @Override
        protected List<AppItem> doInBackground(Void... params) {
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
            Collections.sort(appList, SortHelper.sAppcomparator);

            return appList;
        }

        @Override
        protected void onPostExecute(final List<AppItem> appItems) {
            if (appItems != null && isAdded()) {
                setListAdapter(new AppListAdapter(getActivity(), appItems));
            }
        }
    }

}
