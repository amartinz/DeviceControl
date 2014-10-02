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
package org.namelessrom.devicecontrol.ui.adapters;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.listeners.OnAppChoosenListener;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private final Resources res = Application.get().getResources();
    private final List<AppItem>        mAppList;
    private final OnAppChoosenListener mAppChoosenListener;
    private final boolean              isDarkTheme;

    public AppListAdapter(final OnAppChoosenListener listener, final List<AppItem> appList) {
        this.mAppChoosenListener = listener;
        this.mAppList = appList;
        this.isDarkTheme = PreferenceHelper.getBoolean("dark_theme", false);
    }

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        private View      rootView;
        private ImageView appIcon;
        private TextView  appLabel;
        private TextView  packageName;
        private View      layer;

        public ViewHolder(final View v) {
            super(v);
            rootView = v;
            layer = v.findViewById(R.id.app_layer);
            appIcon = (ImageView) v.findViewById(R.id.app_icon);
            appLabel = (TextView) v.findViewById(R.id.app_label);
            packageName = (TextView) v.findViewById(R.id.app_package);
        }
    }

    @Override public int getItemCount() { return mAppList.size(); }

    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int type) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false));
    }

    @Override public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final AppItem appItem = mAppList.get(position);
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                mAppChoosenListener.onAppChoosen(appItem);
            }
        });
        viewHolder.appIcon.setImageDrawable(appItem.getIcon());
        viewHolder.appLabel.setText(appItem.getLabel());
        viewHolder.packageName.setText(appItem.getPackageName());

        final int color = isDarkTheme ? Color.WHITE : Color.BLACK;
        viewHolder.appLabel.setTextColor(appItem.isSystemApp() ? res.getColor(R.color.red_middle)
                : color);
        viewHolder.layer.setVisibility(appItem.isEnabled() ? View.INVISIBLE : View.VISIBLE);
    }

}
