package org.namelessrom.devicecontrol.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.fragments.tools.FlasherFragment;

import java.io.File;
import java.util.List;

public class FlasherAdapter extends RecyclerView.Adapter<FlasherAdapter.TaskerViewHolder> {
    private final FlasherFragment mFragment;
    private List<File> mFiles;

    public FlasherAdapter(final FlasherFragment fragment, final List<File> files) {
        mFragment = fragment;
        mFiles = files;
    }

    @Override public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    @Override public TaskerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final int resId;
        if (Application.get().isDarkTheme()) {
            resId = R.layout.card_flash_item_dark;
        } else {
            resId = R.layout.card_flash_item_light;
        }
        final View v = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new TaskerViewHolder(v);
    }

    @Override public void onBindViewHolder(TaskerViewHolder holder, int position) {
        final File file = mFiles.get(position);

        holder.name.setText(file.getName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mFragment.showRemoveDialog(file);
            }
        });
    }

    public static class TaskerViewHolder extends RecyclerView.ViewHolder {
        public final CardView cardView;
        public final TextView name;

        public TaskerViewHolder(final View view) {
            super(view);
            cardView = (CardView) view;
            name = (TextView) cardView.findViewById(R.id.file_name);
        }
    }

}
