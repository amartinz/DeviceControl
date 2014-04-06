package org.namelessrom.devicecontrol.fragments.tasker;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import org.namelessrom.devicecontrol.widgets.adapters.TaskerAdapter;

/**
 * Created by alex on 06.04.14.
 */
public class TaskListFragment extends ListFragment {

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TaskerAdapter adapter = new TaskerAdapter(getActivity());
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
