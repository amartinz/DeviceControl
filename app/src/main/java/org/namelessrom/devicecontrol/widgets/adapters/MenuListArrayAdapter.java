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

public class MenuListArrayAdapter extends BaseAdapter {

    private final Context  mContext;
    private final int      mLayoutResourceId;
    private final String[] mTitles;
    private final int[]    mIcons;

    public MenuListArrayAdapter(final Context context, final int layoutResourceId,
            final String[] titles, final int[] icons) {

        mContext = context;
        mLayoutResourceId = layoutResourceId;
        mTitles = titles;
        mIcons = icons;
    }

    @Override
    public int getCount() {
        return mTitles.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2; //return 2, you have two types that the getView() method will return,
        // normal(0) and for the last row(1)
    }

    @Override
    public int getItemViewType(int position) {
        return (mIcons[position] == -1) ? 1 : 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 1;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        final int type = getItemViewType(position);
        if (v == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (type == 0) {
                v = inflater.inflate(mLayoutResourceId, parent, false);
            } else if (type == 1) {
                v = inflater.inflate(R.layout.menu_header, parent, false);
            }
        }

        final int defaultColor = mContext.getResources().getColor(android.R.color.white);
        if (type == 0) {
            final TextView text1 = (TextView) v.findViewById(android.R.id.text1);
            text1.setTextColor(Color.WHITE);
            text1.setText(mTitles[position]);

            final ImageView image = (ImageView) v.findViewById(R.id.image);
            final int imageRes = mIcons[position];
            if (imageRes == 0) {
                image.setVisibility(View.INVISIBLE);
            } else {
                image.setImageDrawable(mContext.getResources().getDrawable(mIcons[position]));
                image.setColorFilter(Color.parseColor("#FFFFFF"));
                image.setColorFilter(defaultColor);
            }
        } else if (type == 1) {
            final TextView header = (TextView) v.findViewById(R.id.menu_header);
            header.setText(mTitles[position].replaceAll("--", ""));
            header.setClickable(false);
            header.setTextColor(defaultColor);
        }

        return v;
    }

}

