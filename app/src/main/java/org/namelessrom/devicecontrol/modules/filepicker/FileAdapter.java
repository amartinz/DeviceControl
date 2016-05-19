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
package org.namelessrom.devicecontrol.modules.filepicker;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.SortHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class FileAdapter extends BaseAdapter {

    private final Context context;

    private ArrayList<File> files;

    private String fileType = "";
    private int colorResId = -1;

    private FilePickerListener listener;

    public FileAdapter(Context context, FilePickerListener filePickerListener) {
        this.context = context;
        this.listener = filePickerListener;
    }

    public void setFiles(final ArrayList<File> files) {
        this.files = files;
        Collections.sort(this.files, SortHelper.sFileComparator);
    }

    @Override public int getCount() { return files.size(); }

    @Override public Object getItem(final int position) { return files.get(position); }

    @Override public long getItemId(final int position) { return 0; /* unused */ }

    public FileAdapter setFileType(final String fileType) {
        this.fileType = fileType;
        return this;
    }

    public FileAdapter setColor(final int colorResId) {
        this.colorResId = colorResId;
        return this;
    }

    private static final class ViewHolder {
        private final View rootView;
        private final ImageView icon;
        private final TextView name;
        private final TextView info;

        private ViewHolder(final View rootView) {
            this.rootView = rootView;
            this.icon = (ImageView) rootView.findViewById(R.id.file_icon);
            this.name = (TextView) rootView.findViewById(R.id.file_name);
            this.info = (TextView) rootView.findViewById(R.id.file_info);
        }
    }

    @Override public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.list_item_file, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final File file = files.get(position);

        // we need to hack here as sometimes ../ is not properly recognized as directory
        final boolean isDirectory = file.isDirectory()
                || (file.getAbsolutePath() + File.separator).endsWith("../");

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (listener != null) {
                    if (isDirectory) {
                        listener.onFilePicked(file);
                    } else {
                        listener.onFlashItemPicked(new FlashItem(file.getAbsolutePath()));
                    }
                }
            }
        });

        int color = ContextCompat.getColor(context, R.color.graph_text_color);
        viewHolder.name.setText(file.getName());

        if (isDirectory) {
            viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_folder_black_24dp));
            viewHolder.info.setText(String.valueOf(new Date(file.lastModified())));
        } else {
            viewHolder.icon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_insert_drive_file_black_24dp));
            viewHolder.info.setText(String.valueOf(new Date(file.lastModified())) + " | "
                    + Formatter.formatFileSize(context, file.length()));

            // Color the list entry if a file type is set, to make searching easier
            if (ContentTypes.isFiletypeMatching(file, fileType)) {
                if (colorResId > 0) {
                    color = colorResId;
                } else {
                    color = ContextCompat.getColor(context, R.color.grass);
                }
            }
        }

        viewHolder.name.setTextColor(color);
        viewHolder.info.setTextColor(ContextCompat.getColor(context, R.color.blueish_strong));
        viewHolder.icon.setColorFilter(Color.parseColor("#FFFFFF"));
        viewHolder.icon.setColorFilter(color);

        return v;
    }
}
