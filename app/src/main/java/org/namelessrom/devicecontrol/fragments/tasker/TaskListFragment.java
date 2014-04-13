package org.namelessrom.devicecontrol.fragments.tasker;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.adapters.TaskerAdapter;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

public class TaskListFragment extends ListFragment {

    private TaskerAdapter mAdapter;
    private TextView      mEmptyText;

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
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
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new TaskerAdapter(getActivity());
        setListAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
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
                    startActivity(new Intent(getActivity(), AddTaskActivity.class));
                }
                return true;
            }
            default: {
                break;
            }
        }

        return false;
    }

}
