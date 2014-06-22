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
package org.namelessrom.devicecontrol.widgets.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.RefreshEvent;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 22.06.14.
 */
public class FlashListAdapter extends BaseAdapter {

    private List<FlashItem> flashItemList;

    public FlashListAdapter() { flashItemList = new ArrayList<FlashItem>(); }

    public FlashListAdapter(final List<FlashItem> flashItems) { this.flashItemList = flashItems; }

    @Override public int getCount() { return flashItemList.size(); }

    @Override public Object getItem(final int position) { return flashItemList.get(position); }

    @Override public long getItemId(final int position) { return 0; }

    public List<FlashItem> getFlashItemList() { return this.flashItemList; }

    private static final class ViewHolder {
        private final View      rootView;
        private final TextView  filePath;
        private final TextView  fileName;
        private final ImageView cancel;

        private ViewHolder(final View rootView) {
            this.rootView = rootView;
            this.filePath = findById(rootView, R.id.flash_path);
            this.fileName = findById(rootView, R.id.flash_name);
            this.cancel = findById(rootView, R.id.flash_cancel);
        }
    }

    @Override public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = Application.getLayoutInflater().inflate(R.layout.list_item_flasher, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final FlashItem item = flashItemList.get(position);

        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(item);
            }
        });
        viewHolder.fileName.setText(item.getName());
        viewHolder.filePath.setText(item.getPath());

        viewHolder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(final View v) {
                flashItemList.remove(position);
                notifyDataSetChanged();
                BusProvider.getBus().post(new RefreshEvent());
            }
        });

        return v;
    }
}
