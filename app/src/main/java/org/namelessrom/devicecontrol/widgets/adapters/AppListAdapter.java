package org.namelessrom.devicecontrol.widgets.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.AppDetailActivity;
import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private final Context        mContext;
    private final PackageManager mPm;
    private final List<AppItem> mAppList = new ArrayList<AppItem>();

    public AppListAdapter(final Context context) {
        mContext = context;
        mPm = context.getPackageManager();
        loadItems();
    }

    private void loadItems() {
        mAppList.clear();
        if (mPm != null) {
            final List<PackageInfo> pkgInfos = mPm.getInstalledPackages(0);
            ApplicationInfo appInfo;
            for (final PackageInfo pkgInfo : pkgInfos) {
                appInfo = pkgInfo.applicationInfo;
                if (appInfo != null) {
                    mAppList.add(new AppItem(pkgInfo, String.valueOf(appInfo.loadLabel(mPm)),
                            appInfo.loadIcon(mPm)));
                }
            }
        }
        Collections.sort(mAppList, SortHelper.sAppcomparator);
    }

    @Override
    public void notifyDataSetChanged() {
        loadItems();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() { return mAppList.size(); }

    @Override
    public Object getItem(int position) { return mAppList.get(position); }

    @Override
    public long getItemId(int position) { return 0; }

    private static class ViewHolder {
        private ImageView appIcon;
        private TextView  appLabel;
        private TextView  packageName;
        private View      layer;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = ((LayoutInflater) Application.applicationContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_app, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            viewHolder.appLabel = (TextView) convertView.findViewById(R.id.app_label);
            viewHolder.packageName = (TextView) convertView.findViewById(R.id.app_package);
            viewHolder.layer = convertView.findViewById(R.id.app_layer);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppItem appItem = mAppList.get(position);
        viewHolder.appIcon.setImageDrawable(appItem.getIcon());
        viewHolder.appLabel.setText(appItem.getLabel());
        viewHolder.packageName.setText(appItem.getPackageName());

        final Resources res = Application.applicationContext.getResources();
        if (appItem.isSystemApp()) {
            viewHolder.appLabel.setTextColor(res.getColor(R.color.red_middle));
        } else {
            viewHolder.appLabel.setTextColor(res.getColor(android.R.color.white));
        }

        if (appItem.isEnabled()) {
            viewHolder.layer.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.layer.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(mContext, AppDetailActivity.class);
                i.putExtra("app_item", appItem.getPackageName());
                mContext.startActivity(i);
            }
        });

        return convertView;
    }

}
