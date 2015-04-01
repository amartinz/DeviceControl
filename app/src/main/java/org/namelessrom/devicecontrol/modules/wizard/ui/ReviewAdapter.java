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
package org.namelessrom.devicecontrol.modules.wizard.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ReviewAdapter extends BaseAdapter {
    private final Context mContext;
    private final int mLayoutResId;
    private final ArrayList<String> mEntries;
    private final ArrayList<String> mValues;

    public ReviewAdapter(final Context context, final int resId, final ArrayList<String> entries,
            final ArrayList<String> values) {
        mContext = context;
        mLayoutResId = resId;
        mEntries = entries;
        mValues = values;
    }

    @Override public int getCount() { return mEntries.size(); }

    @Override public Object getItem(final int i) { return mEntries.get(i); }

    @Override public long getItemId(int i) { return 0; }

    private static final class ViewHolder {
        private final TextView name;
        private final TextView value;

        public ViewHolder(final View rootView) {
            name = (TextView) rootView.findViewById(android.R.id.text1);
            value = (TextView) rootView.findViewById(android.R.id.text2);
        }
    }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (view == null) {
            view = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(mLayoutResId, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.name.setText(mEntries.get(i));
        holder.value.setText(mValues.get(i));

        return view;
    }

}
