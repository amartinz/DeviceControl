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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

import java.util.ArrayList;

public class MenuListArrayAdapter extends BaseAdapter {

    private final Context mContext;
    private final int mLayoutResourceId;
    private final ArrayList<MenuItem> mItems;

    public MenuListArrayAdapter(final Context context, final int layoutResourceId,
            final ArrayList<MenuItem> items) {
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mItems = items;
    }

    @Override public int getCount() { return mItems.size(); }

    @Override public Object getItem(final int position) { return mItems.get(position); }

    @Override public long getItemId(final int arg0) { return 0; }

    @Override public int getViewTypeCount() { return 2; }

    @Override public int getItemViewType(final int pos) {
        return (mItems.get(pos).icon == -1) ? 1 : 0;
    }

    @Override public boolean isEnabled(final int pos) { return getItemViewType(pos) != 1; }

    private static final class ViewHolder {
        private final TextView header;
        private final TextView title;
        private final ImageView image;

        public ViewHolder(final View v, final int type) {
            if (type == 0) {
                header = null;
                title = (TextView) v.findViewById(android.R.id.text1);
                image = (ImageView) v.findViewById(R.id.image);
            } else if (type == 1) {
                header = (TextView) v.findViewById(R.id.menu_header);
                title = null;
                image = null;
            } else {
                header = null;
                title = null;
                image = null;
            }
        }
    }

    @Override public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        final int type = getItemViewType(position);
        if (v == null) {
            final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (type == 0) {
                v = inflater.inflate(mLayoutResourceId, parent, false);
            } else if (type == 1) {
                v = inflater.inflate(R.layout.menu_header, parent, false);
            }
            assert (v != null);
            viewHolder = new ViewHolder(v, type);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        if (type == 0) {
            viewHolder.title.setText(mItems.get(position).title);

            final int imageRes = mItems.get(position).icon;
            if (imageRes == 0) {
                viewHolder.image.setVisibility(View.INVISIBLE);
            } else {
                final Drawable icon = DrawableHelper.getDrawable(imageRes);
                if (icon != null) {
                    DrawableHelper.applyAccentColorFilter(icon);
                    viewHolder.image.setImageDrawable(icon);
                }
            }
        } else if (type == 1) {
            viewHolder.header.setText(mItems.get(position).title);
            viewHolder.header.setClickable(false);
            viewHolder.header.setTextColor(AppResources.get().getAccentColor());
        }

        return v;
    }

    public static class MenuItem {
        public final int id;
        public final int title;
        public final int icon;

        public MenuItem(int id, int title, int icon) {
            this.id = id;
            this.title = title;
            this.icon = icon;
        }
    }

}

