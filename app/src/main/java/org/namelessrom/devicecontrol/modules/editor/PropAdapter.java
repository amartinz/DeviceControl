/*
 *  Copyright (C) 2013 h0rn3t
 *  Modifications Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.editor;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;
import java.util.Iterator;

public class PropAdapter extends ArrayAdapter<Prop> {
    private final Context mContext;
    private final ArrayList<Prop> mProps;
    private ArrayList<Prop> mFiltered;

    public PropAdapter(Context context, ArrayList<Prop> objects) {
        super(context, R.layout.list_item_prop, objects);
        mContext = context;
        mFiltered = objects;

        // save original items
        mProps = new ArrayList<>(mFiltered);
    }

    public Prop getItem(final int i) { return mFiltered.get(i); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = ((LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.list_item_prop, parent, false);
            viewHolder = new ViewHolder(convertView);
            assert (convertView != null);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Prop p = mFiltered.get(position);
        if (p != null) {
            if (viewHolder.pp != null) {
                viewHolder.pp.setText(p.getName());
            }
            if (viewHolder.pv != null) {
                viewHolder.pv.setText(p.getVal());
            }

        }
        return convertView;
    }

    public void filter(String query) {
        mFiltered.clear();
        mFiltered.addAll(mProps);

        query = (query != null ? query.trim().toLowerCase() : null);
        if (!TextUtils.isEmpty(query)) {
            Iterator<Prop> iterator = mFiltered.iterator();
            while (iterator.hasNext()) {
                final Prop prop = iterator.next();
                final String propName = ((prop != null) ? prop.getName() : null);
                if (!TextUtils.isEmpty(propName) && propName.toLowerCase().contains(query)) {
                    iterator.remove();
                }
            }
        }

        notifyDataSetChanged();
    }

    private static final class ViewHolder {
        private final TextView pp;
        private final TextView pv;

        private ViewHolder(final View rootView) {
            pp = (TextView) rootView.findViewById(R.id.prop);
            pv = (TextView) rootView.findViewById(R.id.pval);
        }
    }
}
