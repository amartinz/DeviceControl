package org.namelessrom.devicecontrol.widgets.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.AppItem;

import java.util.List;

import static butterknife.ButterKnife.findById;

public class AppListAdapter extends BaseAdapter {

    private final List<AppItem> mAppList;

    public AppListAdapter(final List<AppItem> appList) { mAppList = appList; }

    @Override
    public int getCount() { return mAppList.size(); }

    @Override
    public Object getItem(final int position) { return mAppList.get(position); }

    @Override
    public long getItemId(final int position) { return 0; }

    private static final class ViewHolder {
        private ImageView appIcon;
        private TextView  appLabel;
        private TextView  packageName;
        private View      layer;

        public ViewHolder(final View v) {
            appIcon = findById(v, R.id.app_icon);
            appLabel = findById(v, R.id.app_label);
            packageName = findById(v, R.id.app_package);
            layer = findById(v, R.id.app_layer);
        }
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = ((LayoutInflater) Application.applicationContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_app, parent, false);
            assert (convertView != null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppItem appItem = mAppList.get(position);
        viewHolder.appIcon.setImageDrawable(appItem.getIcon());
        viewHolder.appLabel.setText(appItem.getLabel());
        viewHolder.packageName.setText(appItem.getPackageName());

        final Resources res = Application.applicationContext.getResources();
        viewHolder.appLabel.setTextColor(appItem.isSystemApp() ? res.getColor(R.color.red_middle)
                : res.getColor(android.R.color.white));
        viewHolder.layer.setVisibility(appItem.isEnabled() ? View.INVISIBLE : View.VISIBLE);

        return convertView;
    }

}
