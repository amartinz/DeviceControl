package org.namelessrom.devicecontrol.cards;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * A simple card for displaying informations
 */
public class TaskerCard extends Card {

    private final TaskerItem item;

    public TaskerCard(final Context context, final TaskerItem taskerItem) {
        this(context, R.layout.card_tasker_inner_content, taskerItem);
    }

    public TaskerCard(final Context context, final int innerLayout, final TaskerItem taskerItem) {
        super(context, innerLayout);
        item = taskerItem;

        setClickable(true);
        setSwipeable(false);
        setOnClickListener(new OnCardClickListener() {
            @Override public void onClick(Card card, View view) {
                final Intent intent = new Intent(getContext(), AddTaskActivity.class);
                intent.putExtra(AddTaskActivity.ARG_ITEM, item);
                getContext().startActivity(intent);
            }
        });
    }

    @Override public void setupInnerViewElements(final ViewGroup parent, final View view) {
        final TextView category = (TextView) view.findViewById(R.id.category);
        final TextView action = (TextView) view.findViewById(R.id.action);
        final TextView value = (TextView) view.findViewById(R.id.value);
        final CheckBox enabled = (CheckBox) view.findViewById(R.id.enabled);

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
    }

}
