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

    Activity          context;
    PackageManager    packageManager;
    ArrayList<String> pList;


    public PackAdapter(Activity context, String pmList[], PackageManager packageManager) {
        super();
        this.context = context;
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
        ViewHolder holder;
        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.pack_item, null);

            holder = new ViewHolder();

            holder.packRaw = (TextView) convertView.findViewById(R.id.packraw);
            holder.packName = (TextView) convertView.findViewById(R.id.packname);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(getItem(position), 0);
            holder.packRaw.setText(packageInfo.packageName);
            holder.packName.setText(
                    packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
            holder.imageView.setImageDrawable(
                    packageManager.getApplicationIcon(packageInfo.applicationInfo));

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        return convertView;
    }
}
