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
