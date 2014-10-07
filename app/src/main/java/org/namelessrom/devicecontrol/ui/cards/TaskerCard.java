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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

public class TaskerCard extends Card {

    private final TaskerItem item;

    public TaskerCard(Context context, AttributeSet attrs, TaskerItem taskerItem,
            Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState, false);
        item = taskerItem;

        hideHeaderLayout();
        setLayoutId(R.layout.card_tasker);

        final TextView category = (TextView) findViewById(R.id.category);
        final TextView action = (TextView) findViewById(R.id.action);
        final TextView value = (TextView) findViewById(R.id.value);
        final CheckBox enabled = (CheckBox) findViewById(R.id.enabled);

        category.setText(item.getCategory());
        action.setText(item.getName());
        value.setText(item.getValue());
        enabled.setChecked(item.getEnabled());
        enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setEnabled(isChecked);
                DatabaseHandler.getInstance().updateTaskerItem(item);
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
