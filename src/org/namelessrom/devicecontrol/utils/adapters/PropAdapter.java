/*
 *  Copyright (C) 2013 h0rn3t
 *  Modifications Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.utils.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.classes.Prop;

import java.util.ArrayList;
import java.util.List;

public class PropAdapter extends ArrayAdapter<Prop> {
    private final Context mContext;
    private final int mId;
    private final List<Prop> mProps;
    private static Filter mFilter;

    public PropAdapter(Context context, int textViewResourceId, List<Prop> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mId = textViewResourceId;
        mProps = objects;
    }

    public Prop getItem(int i) {
        return mProps.get(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(mId, null);
        }

        final Prop p = mProps.get(position);
        if (p != null) {
            TextView pp = (TextView) v.findViewById(R.id.prop);
            TextView pv = (TextView) v.findViewById(R.id.pval);
            if (pp != null) {
                pp.setText(p.getName());
            }
            if (pv != null) {
                pv.setText(p.getVal());
            }

        }
        return v;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new AppFilter(mProps);
        }
        return mFilter;
    }

    private class AppFilter extends Filter {
        private final List<Prop> sourceObjects;

        public AppFilter(List<Prop> props) {
            sourceObjects = new ArrayList<Prop>();
            synchronized (this) {
                sourceObjects.addAll(props);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                List<Prop> filter = new ArrayList<Prop>();
                for (Prop o : mProps) {
                    if (o.getName().toLowerCase().contains(filterSeq))
                        filter.add(o);
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<Prop> filtered = (List<Prop>) results.values;
            notifyDataSetChanged();
            clear();
            for (Prop aFiltered : filtered) add(aFiltered);
            notifyDataSetInvalidated();
        }
    }
}
