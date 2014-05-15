package org.namelessrom.devicecontrol.widgets.adapters;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import static butterknife.ButterKnife.findById;

public class AppListAdapter extends BaseAdapter {

    private final Context       mContext;
    private final List<AppItem> mAppList;

    public AppListAdapter(final Context context, final List<AppItem> appList) {
        mContext = context;
        mAppList = appList;
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

            viewHolder.appIcon = findById(convertView, R.id.app_icon);
            viewHolder.appLabel = findById(convertView, R.id.app_label);
            viewHolder.packageName = findById(convertView, R.id.app_package);
            viewHolder.layer = findById(convertView, R.id.app_layer);

            assert (convertView != null);

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

        /*convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent i = new Intent(mContext, AppDetailActivity.class);
                i.putExtra("app_item", appItem.getPackageName());
                mContext.startActivity(i);
            }
        });*/

        return convertView;
    }

}
