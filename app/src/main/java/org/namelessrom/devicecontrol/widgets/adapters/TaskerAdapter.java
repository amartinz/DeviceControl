package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.Activity;
import android.content.Context;
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

import static butterknife.ButterKnife.findById;

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
    public TaskerItem getItem(final int position) { return mTaskerList.get(position); }

    @Override
    public long getItemId(final int arg0) { return 0; }

    @Override
    public View getView(final int position, View v, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (v == null) {
            v = ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.list_item_tasker, parent, false);
            assert (v != null);
            viewHolder = new ViewHolder(v);
            v.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) v.getTag();
        }

        final TaskerItem item = mTaskerList.get(position);

        viewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusProvider.getBus().post(item);
            }
        });
        viewHolder.category.setText(item.getCategory());
        viewHolder.action.setText(item.getName());
        viewHolder.value.setText(item.getValue());
        viewHolder.enabled.setChecked(item.getEnabled());
        viewHolder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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

    private static final class ViewHolder {
        private final LinearLayout container;
        private final TextView     category;
        private final TextView     action;
        private final TextView     value;
        private final CheckBox     enabled;

        public ViewHolder(final View v) {
            container = findById(v, R.id.item_container);
            category = findById(v, R.id.category);
            action = findById(v, R.id.action);
            value = findById(v, R.id.value);
            enabled = findById(v, R.id.enabled);
        }
    }

}

