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
package org.namelessrom.devicecontrol.fragments.filepicker;

import android.content.res.Resources;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 22.06.14.
 */
public class FileAdapter extends BaseAdapter {

    public static final int TYPE_DIRECTORY = 1;
    public static final int TYPE_FILE      = 2;

    private ArrayList<File> files;

    private String fileType   = "";
    private int    colorResId = -1;

    public FileAdapter(final ArrayList<File> files) {
        this.files = files;
        Collections.sort(this.files, SortHelper.sFileComparator);
    }

    @Override public int getCount() { return files.size(); }

    @Override public Object getItem(final int position) { return files.get(position); }

    @Override public long getItemId(final int position) {
        return (files.get(position).isDirectory() ? TYPE_DIRECTORY : TYPE_FILE);
    }

    public FileAdapter setFileType(final String fileType) {
        this.fileType = fileType;
        return this;
    }

    public FileAdapter setColor(final int colorResId) {
        this.colorResId = colorResId;
        return this;
    }

    private static final class ViewHolder {
        private final View      rootView;
        private final ImageView icon;
        private final TextView  name;
        private final TextView  info;

        private ViewHolder(final View rootView) {
            this.rootView = rootView;
            this.icon = findById(rootView, R.id.file_icon);
            this.name = findById(rootView, R.id.file_name);
            this.info = findById(rootView, R.id.file_info);
        }
    }

    @Override public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = Application.getLayoutInflater().inflate(R.layout.list_item_file, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final File file = files.get(position);

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file.isDirectory()) {
                    BusProvider.getBus().post(file);
                } else {
                    BusProvider.getBus().post(new FlashItem(file.getAbsolutePath()));
                }
            }
        });

        final Resources resources = Application.applicationContext.getResources();
        int color = resources.getColor(android.R.color.white);

        viewHolder.name.setText(file.getName());

        if (file.isDirectory()) {
            viewHolder.icon
                    .setImageDrawable(resources.getDrawable(R.drawable.ic_general_directory));
            viewHolder.info.setText(String.valueOf(new Date(file.lastModified())));
        } else {
            viewHolder.icon.setImageDrawable(resources.getDrawable(R.drawable.ic_general_file));
            viewHolder.info.setText(String.valueOf(new Date(file.lastModified())) + " | "
                    + Formatter.formatFileSize(Application.applicationContext, file.length()));

            // Color the list entry if a filetype is set, to make searching easier
            color = ContentTypes.isFiletypeMatching(file, fileType)
                    ? ((colorResId > 0) ? colorResId : resources.getColor(R.color.blueish_strong))
                    : color;
        }

        viewHolder.name.setTextColor(color);
        viewHolder.icon.setColorFilter(Color.parseColor("#FFFFFF"));
        viewHolder.icon.setColorFilter(color);

        return v;
    }
}
