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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import static butterknife.ButterKnife.findById;

public class MenuListArrayAdapter extends BaseAdapter {

    private final Context mContext;
    private final int     mLayoutResourceId;
    private final int[]   mTitles;
    private final int[]   mIcons;

    public MenuListArrayAdapter(final Context context, final int layoutResourceId,
            final int[] titles, final int[] icons) {
        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mTitles = titles;
        mIcons = icons;
    }

    @Override public int getCount() { return mTitles.length; }

    @Override public Object getItem(final int position) { return mTitles[position]; }

    @Override public long getItemId(final int arg0) { return 0; }

    @Override public int getViewTypeCount() { return 2; }

    @Override public int getItemViewType(final int pos) { return (mIcons[pos] == -1) ? 1 : 0; }

    @Override public boolean isEnabled(final int pos) { return getItemViewType(pos) != 1; }

    private static final class ViewHolder {
        private final TextView  header;
        private final TextView  title;
        private final ImageView image;

        public ViewHolder(final View v, final int type) {
            if (type == 0) {
                header = null;
                title = findById(v, android.R.id.text1);
                image = findById(v, R.id.image);
            } else if (type == 1) {
                header = findById(v, R.id.menu_header);
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

        final int defaultColor = mContext.getResources().getColor(android.R.color.white);
        if (type == 0) {
            viewHolder.title.setTextColor(Color.WHITE);
            viewHolder.title.setText(mTitles[position]);

            final int imageRes = mIcons[position];
            if (imageRes == 0) {
                viewHolder.image.setVisibility(View.INVISIBLE);
            } else {
                viewHolder.image.setImageDrawable(
                        mContext.getResources().getDrawable(mIcons[position]));
                viewHolder.image.setColorFilter(Color.parseColor("#FFFFFF"));
                viewHolder.image.setColorFilter(defaultColor);
            }
        } else if (type == 1) {
            viewHolder.header.setText(mContext.getString(mTitles[position]).replaceAll("--", ""));
            viewHolder.header.setClickable(false);
            viewHolder.header.setTextColor(defaultColor);
        }

        return v;
    }

}

