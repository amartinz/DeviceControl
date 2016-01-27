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
package org.namelessrom.devicecontrol.modules.flasher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.views.CardView;

import java.io.File;
import java.util.List;

public class FlasherAdapter extends RecyclerView.Adapter<FlasherAdapter.FlashViewHolder> {
    private final FlasherFragment mFragment;
    private List<File> mFiles;

    public FlasherAdapter(final FlasherFragment fragment, final List<File> files) {
        mFragment = fragment;
        mFiles = files;
    }

    @Override public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    @Override public FlashViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final CardView card = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_flash_item, parent, false);
        return new FlashViewHolder(card);
    }

    @Override public void onBindViewHolder(FlashViewHolder holder, int position) {
        final File file = mFiles.get(position);

        holder.name.setText(file.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mFragment.showRemoveDialog(file);
            }
        });
    }

    public static class FlashViewHolder extends RecyclerView.ViewHolder {
        public final CardView cardView;
        public final TextView name;

        public FlashViewHolder(final View view) {
            super(view);
            cardView = (CardView) view;
            name = (TextView) cardView.findViewById(R.id.file_name);
        }
    }

}
