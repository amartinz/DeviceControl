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
    public int getCount() { return mTitles.length; }

    @Override
    public Object getItem(int position) { return position; }

    @Override
    public long getItemId(int arg0) { return 0; }

    @Override
    public int getViewTypeCount() { return 2; }

    @Override
    public int getItemViewType(int position) {
        return (mIcons[position] == -1) ? 1 : 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != 1;
    }

    private static final class ViewHolder {
        private TextView  header;
        private TextView  text;
        private ImageView image;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        final ViewHolder viewHolder;
        final int type = getItemViewType(position);
        if (v == null) {
            viewHolder = new ViewHolder();
            final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            if (type == 0) {
                v = inflater.inflate(mLayoutResourceId, parent, false);

                viewHolder.text = findById(v, android.R.id.text1);
                viewHolder.image = findById(v, R.id.image);
            } else if (type == 1) {
                v = inflater.inflate(R.layout.menu_header, parent, false);

                viewHolder.header = findById(v, R.id.menu_header);
            }

            assert (v != null);

            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final int defaultColor = mContext.getResources().getColor(android.R.color.white);
        if (type == 0) {
            viewHolder.text.setTextColor(Color.WHITE);
            viewHolder.text.setText(mTitles[position]);

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
            viewHolder.header.setText(mTitles[position].replaceAll("--", ""));
            viewHolder.header.setClickable(false);
            viewHolder.header.setTextColor(defaultColor);
        }

        return v;
    }

}

