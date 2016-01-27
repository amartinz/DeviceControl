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
package org.namelessrom.devicecontrol.modules.tasker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.modules.wizard.AddTaskActivity;
import org.namelessrom.devicecontrol.views.CardView;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

import java.util.Collections;
import java.util.List;

public class TaskerAdapter extends RecyclerView.Adapter<TaskerAdapter.TaskerViewHolder> {
    private final Activity mActivity;
    private final List<TaskerItem> mTasks;

    public TaskerAdapter(final Activity activity, @NonNull final List<TaskerItem> tasks) {
        mActivity = activity;
        mTasks = tasks;

        Collections.sort(mTasks);
    }

    @Override public int getItemCount() {
        return mTasks.size();
    }

    @Override public TaskerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final CardView card = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_tasker, parent, false);
        return new TaskerViewHolder(card);
    }

    @Override public void onBindViewHolder(TaskerViewHolder holder, int position) {
        final TaskerItem item = mTasks.get(position);

        holder.image.setImageDrawable(ActionProcessor.getImageForCategory(item.category));
        holder.trigger.setText(item.trigger);
        holder.action.setText(item.name);
        holder.value.setText(item.value);
        holder.enabled.setChecked(item.enabled);
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TaskerConfig taskerConfig = TaskerConfig.get();
                taskerConfig.deleteItem(item);
                item.enabled = isChecked;
                taskerConfig.addItem(item).save();
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final Intent intent = new Intent(mActivity, AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.ARG_ITEM, item);
                mActivity.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                alert.setIcon(DrawableHelper.applyAccentColorFilter(R.drawable.ic_delete_black_24dp));
                alert.setTitle(R.string.delete_task);
                alert.setMessage(mActivity.getString(R.string.delete_task_question));
                alert.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int b) {
                                d.dismiss();
                            }
                        });
                alert.setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface d, int b) {
                                TaskerConfig.get().deleteItem(item).save();
                                mTasks.remove(item);
                                d.dismiss();
                                notifyDataSetChanged();
                            }
                        });
                alert.show();
                return true;
            }
        });
    }

    public static class TaskerViewHolder extends RecyclerView.ViewHolder {
        public final CardView cardView;
        public final ImageView image;
        public final TextView trigger;
        public final TextView action;
        public final TextView value;
        public final Switch enabled;

        public TaskerViewHolder(final View view) {
            super(view);
            cardView = (CardView) view;
            image = (ImageView) cardView.findViewById(R.id.task_image);
            trigger = (TextView) cardView.findViewById(R.id.trigger);
            action = (TextView) cardView.findViewById(R.id.action);
            value = (TextView) cardView.findViewById(R.id.value);
            enabled = (Switch) cardView.findViewById(R.id.enabled);
        }
    }

}
