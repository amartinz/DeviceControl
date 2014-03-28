package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

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
    public Object getItem(int position) { return position; }

    @Override
    public long getItemId(int arg0) { return 0; }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            v = inflater.inflate(R.layout.list_item, parent, false);
        }
        final TextView text1 = (TextView) v.findViewById(android.R.id.text1);
        text1.setTextColor(Color.WHITE);
        text1.setTextAppearance(mContext, android.R.attr.textAppearanceListItemSmall);
        final TextView text2 = (TextView) v.findViewById(android.R.id.text2);
        text1.setText(mTitles[position]);
        text2.setText(mContent[position]);
        return v;
    }

}

