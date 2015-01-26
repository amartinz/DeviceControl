package org.namelessrom.devicecontrol.wizard.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by alex on 17.10.14.
 */
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
