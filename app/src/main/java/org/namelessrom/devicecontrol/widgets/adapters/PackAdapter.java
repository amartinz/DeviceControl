/*
 * Copyright (C) 2013 h0rn3t
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import java.util.ArrayList;
import java.util.Arrays;

public class PackAdapter extends BaseAdapter {

    Activity          mContext;
    PackageManager    packageManager;
    ArrayList<String> pList;


    public PackAdapter(Activity context, String pmList[], PackageManager packageManager) {
        super();
        this.mContext = context;
        this.packageManager = packageManager;
        this.pList = new ArrayList<String>(Arrays.asList(pmList));
    }

    private class ViewHolder {
        TextView  packRaw;
        TextView  packName;
        ImageView imageView;
    }

    public int getCount() {
        return pList.size();
    }

    public void delItem(int position) {
        pList.remove(position);
    }

    public String getItem(int position) {
        return pList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi =
                    (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.pack_item, null);
        }

        ViewHolder holder = (ViewHolder) v.getTag();

        if (holder == null) {
            holder = new ViewHolder();

            holder.packRaw = (TextView) v.findViewById(R.id.packraw);
            holder.packName = (TextView) v.findViewById(R.id.packname);
            holder.imageView = (ImageView) v.findViewById(R.id.icon);

            v.setTag(holder);
        }

        try {
            final PackageManager pm = mContext.getPackageManager();
            final PackageInfo packageInfo = pm.getPackageInfo(getItem(position), 0);
            holder.packRaw.setText(packageInfo.packageName);
            holder.packName.setText(
                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            holder.imageView.setImageDrawable(
                    packageManager.getApplicationIcon(packageInfo.applicationInfo));

        } catch (PackageManager.NameNotFoundException ignored) { /* TODO: Error? */ }

        return v;
    }
}
