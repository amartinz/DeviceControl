package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.List;

public class TaskerAdapter extends BaseAdapter {

    private final Context          mContext;
    private final DatabaseHandler  mDatabase;
    private       List<TaskerItem> mTaskerList;

    public TaskerAdapter(final Context context) {
        mContext = context;
        mDatabase = DatabaseHandler.getInstance(context);
        mTaskerList = mDatabase.getAllTaskerItems("");
    }

    @Override
    public int getCount() { return mTaskerList.size(); }

    @Override
    public TaskerItem getItem(int position) { return mTaskerList.get(position); }

    @Override
    public long getItemId(int arg0) { return 0; }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            v = inflater.inflate(R.layout.list_item_tasker, parent, false);
        }

        ViewHolder holder = (ViewHolder) v.getTag();
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(holder);
        }

        final TaskerItem item = mTaskerList.get(position);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(item);
            }
        });
        holder.category.setText(item.getCategory());
        holder.action.setText(item.getName());
        holder.value.setText(item.getValue());
        holder.enabled.setChecked(item.getEnabled());
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.setEnabled(isChecked);
                mDatabase.updateTaskerItem(item);
            }
        });

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        mTaskerList = mDatabase.getAllTaskerItems("");
        super.notifyDataSetChanged();
    }

    class ViewHolder {
        private final LinearLayout container;
        private final TextView     category;
        private final TextView     action;
        private final TextView     value;
        private final CheckBox     enabled;

        public ViewHolder(final View v) {
            container = (LinearLayout) v.findViewById(R.id.item_container);
            category = (TextView) v.findViewById(R.id.category);
            action = (TextView) v.findViewById(R.id.action);
            value = (TextView) v.findViewById(R.id.value);
            enabled = (CheckBox) v.findViewById(R.id.enabled);
        }
    }

}

