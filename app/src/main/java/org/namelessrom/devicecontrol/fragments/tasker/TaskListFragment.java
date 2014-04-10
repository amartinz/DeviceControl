package org.namelessrom.devicecontrol.fragments.tasker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.widgets.adapters.TaskerAdapter;

import java.util.Arrays;

public class TaskListFragment extends ListFragment {

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TaskerAdapter adapter = new TaskerAdapter(getActivity());
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tasker, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            case R.id.action_task_add: {
                final Activity activity = getActivity();
                if (activity != null) {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.add_task);
                    builder.setView(getDialogView(activity, null));
                    mDialog = builder.create();
                    mDialog.show();
                }
                return true;
            }
            default: {
                break;
            }
        }

        return false;
    }

    @Subscribe
    public void onEditItem(final TaskerItem taskerItem) {
        if (taskerItem != null) {
            final Activity activity = getActivity();
            if (activity != null) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.edit_task);
                builder.setView(getDialogView(activity, taskerItem));
                mDialog = builder.create();
                mDialog.show();
            }
        }
    }

    private AlertDialog mDialog;

    private Spinner  mCategory;
    private Spinner  mAction;
    private Spinner  mValueSpinner;
    private EditText mValueEditText;

    private RelativeLayout mActionContainer;
    private RelativeLayout mValueContainer;

    private Button mAddTask;

    private View getDialogView(final Activity activity, final TaskerItem item) {
        final LayoutInflater inflater = LayoutInflater.from(activity);
        final View v = inflater.inflate(R.layout.dialog_task, null, false);

        mCategory = (Spinner) v.findViewById(R.id.category_spinner);
        mActionContainer = (RelativeLayout) v.findViewById(R.id.action_container);
        mAction = (Spinner) v.findViewById(R.id.action_spinner);
        mValueContainer = (RelativeLayout) v.findViewById(R.id.value_container);
        mValueSpinner = (Spinner) v.findViewById(R.id.value_spinner);
        mValueEditText = (EditText) v.findViewById(R.id.value_edittext);

        mAddTask = (Button) v.findViewById(R.id.add_task);
        mAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseHandler db = DatabaseHandler.getInstance(activity);
                TaskerItem item1 = item;
                boolean add = false;
                if (item == null) {
                    item1 = new TaskerItem("", "", "", "", true);
                    item1.setFileName("NONE");
                    item1.setEnabled(true);
                    add = true;
                }
                item1.setCategory(String.valueOf(mCategory.getSelectedItem()));
                item1.setName(String.valueOf(mAction.getSelectedItem()));
                item1.setValue(String.valueOf(mValueSpinner.getSelectedItem()));
                if (add) {
                    db.addTaskerItem(item1);
                } else {
                    db.updateTaskerItem(item1);
                }
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                final TaskerAdapter adapter = ((TaskerAdapter) getListAdapter());
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        });
        final Button mCancelTask = (Button) v.findViewById(R.id.cancel_task);
        mCancelTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        if (item != null) {
            mAddTask.setText(R.string.edit_task);
            mActionContainer.setVisibility(View.VISIBLE);
            mAction.setVisibility(View.VISIBLE);
            mValueContainer.setVisibility(View.VISIBLE);
            mValueSpinner.setVisibility(View.VISIBLE);
            mValueEditText.setVisibility(View.GONE);
        } else {
            mAddTask.setText(R.string.add_task);
            mActionContainer.setVisibility(View.GONE);
            mAction.setVisibility(View.GONE);
            mValueContainer.setVisibility(View.GONE);
            mValueSpinner.setVisibility(View.GONE);
            mValueEditText.setVisibility(View.GONE);
        }

        setUpSpinners(activity, item);

        return v;
    }

    private void setUpSpinners(final Activity activity, final TaskerItem taskerItem) {
        setUpCategory(activity, taskerItem);
        setUpAction(activity, taskerItem);
        setUpValue(activity, taskerItem);
    }

    private void setUpCategory(final Activity activity, final TaskerItem taskerItem) {
        final ArrayAdapter<CharSequence> categoryAdapter = new ArrayAdapter<CharSequence>(
                activity, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final String category = (taskerItem != null ? taskerItem.getCategory() :
                TaskerItem.CATEGORY_SCREEN_ON);
        final String[] categoryEntries = ActionProcessor.CATEGORIES;

        for (final String categoryEntry : categoryEntries) {
            categoryAdapter.add(categoryEntry);
        }
        mCategory.setAdapter(categoryAdapter);
        mCategory.setSelection(Arrays.asList(categoryEntries).indexOf(category));
        mCategory.post(new Runnable() {
            public void run() {
                mCategory.setOnItemSelectedListener(new CategoryListener(activity, taskerItem));
            }
        });
    }

    private void setUpAction(final Activity activity, final TaskerItem taskerItem) {
        final ArrayAdapter<CharSequence> actionAdapter = new ArrayAdapter<CharSequence>(
                activity, android.R.layout.simple_spinner_item);
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final String category = (taskerItem != null ? taskerItem.getCategory() : String.valueOf(
                mCategory.getSelectedItem()));
        final String action = (taskerItem != null ? taskerItem.getName() :
                TaskerItem.CATEGORY_SCREEN_ON);
        String[] actionEntries;

        if (TaskerItem.CATEGORY_SCREEN_OFF.equals(category)
                || TaskerItem.CATEGORY_SCREEN_ON.equals(category)) {
            actionEntries = ActionProcessor.SCREEN_ACTIONS;
        } else {
            actionEntries = ActionProcessor.SCREEN_ACTIONS;
        }

        for (final String actionEntry : actionEntries) {
            actionAdapter.add(actionEntry);
        }

        mAction.setAdapter(actionAdapter);
        mAction.setSelection(Arrays.asList(actionEntries).indexOf(action));
        mAction.post(new Runnable() {
            public void run() {
                mAction.setOnItemSelectedListener(new ActionListener(activity, taskerItem));
            }
        });
        mActionContainer.setVisibility(View.VISIBLE);
        mAction.setVisibility(View.VISIBLE);
    }

    private void setUpValue(final Activity activity, final TaskerItem taskerItem) {
        final ArrayAdapter<CharSequence> valueAdapter = new ArrayAdapter<CharSequence>(
                activity, android.R.layout.simple_spinner_item);
        valueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final String action = (taskerItem != null ? taskerItem.getName()
                : String.valueOf(mAction.getSelectedItem()));
        final String value = (taskerItem != null ? taskerItem.getValue() : "");
        String[] valueEntries;

        if (ActionProcessor.ACTION_CPU_FREQUENCY_MAX.equals(action)
                || ActionProcessor.ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            valueEntries = CpuUtils.getAvailableFrequencies();
        } else if (ActionProcessor.ACTION_CPU_GOVERNOR.equals(action)) {
            valueEntries = CpuUtils.getAvailableGovernors().split(" ");
        } else if (ActionProcessor.ACTION_IO_SCHEDULER.equals(action)) {
            valueEntries = CpuUtils.getAvailableIOSchedulers();
        } else {
            valueEntries = CpuUtils.getAvailableFrequencies();
        }

        if (valueEntries != null) {
            for (final String valueEntry : valueEntries) {
                valueAdapter.add(valueEntry);
            }
            mValueSpinner.setAdapter(valueAdapter);
            mValueSpinner.setSelection(Arrays.asList(valueEntries).indexOf(value));
            mValueSpinner.post(new Runnable() {
                public void run() {
                    mValueSpinner
                            .setOnItemSelectedListener(new ValueListener(activity, taskerItem));
                }
            });
            mValueContainer.setVisibility(View.VISIBLE);
            mValueSpinner.setVisibility(View.VISIBLE);
            mAddTask.setEnabled(true);
        }
    }

    private class CategoryListener implements AdapterView.OnItemSelectedListener {

        private final Activity   activity;
        private final TaskerItem taskerItem;

        public CategoryListener(final Activity activity, final TaskerItem taskerItem) {
            this.activity = activity;
            this.taskerItem = taskerItem;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            setUpAction(activity, taskerItem);
            setUpValue(activity, taskerItem);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    private class ActionListener implements AdapterView.OnItemSelectedListener {
        private final Activity   activity;
        private final TaskerItem taskerItem;

        public ActionListener(final Activity activity, final TaskerItem taskerItem) {
            this.activity = activity;
            this.taskerItem = taskerItem;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            setUpValue(activity, taskerItem);
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

    private class ValueListener implements AdapterView.OnItemSelectedListener {
        private final Activity   activity;
        private final TaskerItem taskerItem;

        public ValueListener(final Activity activity, final TaskerItem taskerItem) {
            this.activity = activity;
            this.taskerItem = taskerItem;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            /* TODO: cool stuff */
        }

        public void onNothingSelected(AdapterView<?> parent) { /* Do nothing. */ }
    }

}
