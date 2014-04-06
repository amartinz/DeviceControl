package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;

import java.util.List;

public class TaskerAdapter extends BaseAdapter {

    private final Context          mContext;
    private       DatabaseHandler  mDatabase;
    private       List<TaskerItem> mTaskerList;

    public TaskerAdapter(final Context context) {
        mContext = context;
        mDatabase = DatabaseHandler.getInstance(context);
        mTaskerList = mDatabase.getAllTaskerItems("");
    }

    @Override
    public int getCount() { return mTaskerList.size(); }

    @Override
    public Object getItem(int position) { return position; }

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

        holder.category.setText(item.getCategory());
        holder.action.setText(item.getName());
        holder.value.setText(item.getValue());
        holder.enabled.setChecked(item.getEnabled());

        return v;
    }

    class ViewHolder {
        TextView category;
        TextView action;
        TextView value;
        CheckBox enabled;

        public ViewHolder(final View v) {
            category = (TextView) v.findViewById(R.id.category);
            action = (TextView) v.findViewById(R.id.action);
            value = (TextView) v.findViewById(R.id.value);
            enabled = (CheckBox) v.findViewById(R.id.enabled);
        }
    }

}

