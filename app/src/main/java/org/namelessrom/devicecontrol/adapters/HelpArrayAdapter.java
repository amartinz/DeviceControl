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
package org.namelessrom.devicecontrol.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import static butterknife.ButterKnife.findById;

public class HelpArrayAdapter extends BaseAdapter {

    private final Context  mContext;
    private final String[] mTitles;
    private final String[] mContent;

    public HelpArrayAdapter(final Context context, final String[] titles, final String[] content) {
        mContext = context;
        mTitles = titles;
        mContent = content;
    }

    @Override
    public int getCount() { return mTitles.length; }

    @Override
    public Object getItem(final int position) { return position; }

    @Override
    public long getItemId(final int arg0) { return 0; }

    private static class ViewHolder {
        private final TextView title;
        private final TextView summary;

        public ViewHolder(final View v) {
            title = findById(v, android.R.id.text1);
            summary = findById(v, android.R.id.text2);
        }
    }

    @Override
    public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.list_item, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        viewHolder.title.setTextColor(Color.WHITE);
        viewHolder.title.setTextAppearance(mContext, android.R.attr.textAppearanceListItemSmall);
        viewHolder.title.setText(mTitles[position]);
        viewHolder.summary.setText(mContent[position]);
        return v;
    }

}

