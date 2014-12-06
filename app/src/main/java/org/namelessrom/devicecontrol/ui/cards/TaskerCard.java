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

package org.namelessrom.devicecontrol.ui.cards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

public class TaskerCard extends Card {

    public final TaskerItem item;

    public TaskerCard(Context context, AttributeSet attrs, TaskerItem taskerItem,
            Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState, false);
        item = taskerItem;

        hideHeaderLayout();
        setLayoutId(R.layout.card_tasker);

        final ImageView image = (ImageView) findViewById(R.id.task_image);
        final TextView trigger = (TextView) findViewById(R.id.trigger);
        final TextView action = (TextView) findViewById(R.id.action);
        final TextView value = (TextView) findViewById(R.id.value);
        final Switch enabled = (Switch) findViewById(R.id.enabled);

        image.setImageDrawable(ActionProcessor.getImageForCategory(item.category));
        trigger.setText(item.trigger);
        action.setText(item.name);
        value.setText(item.value);
        enabled.setChecked(item.enabled);
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.enabled = isChecked;
                DatabaseHandler.getInstance().updateOrInsertTaskerItem(item);
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override public void onClick(View view) {
                final Intent intent = new Intent(getContext(), AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.ARG_ITEM, item);
                getContext().startActivity(intent);
            }
        });
    }

    @Override protected boolean canExpand() { return false; }
}
