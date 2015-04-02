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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ObservableScrollView;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.TaskerConfiguration;
import org.namelessrom.devicecontrol.modules.wizard.AddTaskActivity;
import org.namelessrom.devicecontrol.services.TaskerService;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.List;

public class TaskerFragment extends AttachFragment {

    private RecyclerView mRecyclerView;
    private View mEmptyView;

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_TASKER; }

    @Override public void onResume() {
        super.onResume();
        refreshListView();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View v = inflater.inflate(R.layout.fragment_tasker, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mEmptyView = v.findViewById(android.R.id.empty);
        final FloatingActionButton fabAdd = (FloatingActionButton) v.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), AddTaskActivity.class));
                }
            }
        });

        final ObservableScrollView scrollView = (ObservableScrollView)
                v.findViewById(R.id.cards_layout_container);
        if (scrollView != null) {
            fabAdd.attachToScrollView(scrollView);
        }

        return v;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshListView();
    }

    private void refreshListView() {
        new UpdateTaskerCardList().execute();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_tasker, menu);

        final MenuItem toggle = menu.findItem(R.id.menu_action_toggle);
        final View v;
        if (toggle != null && (v = toggle.getActionView()) != null) {
            final SwitchCompat sw = (SwitchCompat) v.findViewById(R.id.ab_switch);
            sw.setChecked(TaskerConfiguration.get(getActivity()).enabled);
            sw.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override public void onCheckedChanged(final CompoundButton b,
                                final boolean isChecked) {
                            TaskerConfiguration.get(getActivity()).enabled = isChecked;
                            TaskerConfiguration.get(getActivity()).saveConfiguration(getActivity());
                            Utils.toggleComponent(new ComponentName(getActivity().getPackageName(),
                                    TaskerService.class.getName()), !isChecked);
                            if (isChecked) {
                                Utils.startTaskerService();
                            } else {
                                Utils.stopTaskerService();
                            }
                        }
                    }
            );
        }
    }

    private class UpdateTaskerCardList extends AsyncTask<Void, Void, List<TaskerItem>> {
        @Override protected void onPreExecute() {
            // TODO: animations and progress view
            mEmptyView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
        }

        @Override protected List<TaskerItem> doInBackground(final Void... voids) {
            final Context context = getActivity();
            return TaskerConfiguration.get(context).loadConfiguration(context).items;
        }

        @Override protected void onPostExecute(final List<TaskerItem> result) {
            // if the adapter exists and we have items, clear it and add the results
            if (result != null && result.size() > 0 && getActivity() != null) {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(new TaskerAdapter(getActivity(), result));
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        }
    }

}
