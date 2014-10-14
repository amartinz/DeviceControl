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
package org.namelessrom.devicecontrol.fragments.tools;

import android.content.ComponentName;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.negusoft.holoaccent.widget.AccentSwitch;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.services.TaskerService;
import org.namelessrom.devicecontrol.ui.cards.TaskerCard;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

import java.util.ArrayList;
import java.util.List;

public class TaskerFragment extends AttachFragment implements DeviceConstants {

    private LinearLayout mCardsLayout;
    private View         mEmptyView;

    @Override protected int getFragmentId() { return ID_TOOLS_TASKER; }

    @Override public void onResume() {
        super.onResume();
        refreshListView();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View v = inflater.inflate(R.layout.fragment_tasker, container, false);

        mCardsLayout = (LinearLayout) v.findViewById(R.id.cards_layout);
        mEmptyView = v.findViewById(android.R.id.empty);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshListView();
    }

    public void addCards(final ArrayList<TaskerCard> cards, boolean animate, boolean remove) {
        mCardsLayout.clearAnimation();
        if (remove) {
            mCardsLayout.removeAllViews();
        }
        if (animate) {
            mCardsLayout.setAnimation(
                    AnimationUtils.loadAnimation(getActivity(), R.anim.up_from_bottom));
        }
        for (final TaskerCard card : cards) {
            mCardsLayout.addView(card);
        }
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
            final AccentSwitch sw = (AccentSwitch) v.findViewById(R.id.ab_switch);
            sw.setChecked(PreferenceHelper.getBoolean(USE_TASKER, false));
            sw.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override public void onCheckedChanged(final CompoundButton b,
                                final boolean isChecked) {
                            PreferenceHelper.setBoolean(USE_TASKER, isChecked);
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

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_task_add: {
                if (getActivity() != null) {
                    startActivity(new Intent(getActivity(), AddTaskActivity.class));
                }
                return true;
            }
        }
        return false;
    }

    private class UpdateTaskerCardList extends AsyncTask<Void, Void, ArrayList<TaskerCard>> {
        @Override protected void onPreExecute() {
            // TODO: animations and progress view
            mEmptyView.setVisibility(View.GONE);
            mCardsLayout.setVisibility(View.GONE);
        }

        @Override protected ArrayList<TaskerCard> doInBackground(final Void... voids) {
            final List<TaskerItem> items = DatabaseHandler.getInstance().getAllTaskerItems("");
            final ArrayList<TaskerCard> cards = new ArrayList<TaskerCard>(items.size());

            for (final TaskerItem item : items) {
                cards.add(new TaskerCard(getActivity(), null, item, null));
            }

            return cards;
        }

        @Override protected void onPostExecute(final ArrayList<TaskerCard> result) {
            // if the adapter exists and we have items, clear it and add the results
            if (result != null && result.size() > 0) {
                addCards(result, true, true);
                mEmptyView.setVisibility(View.GONE);
                mCardsLayout.setVisibility(View.VISIBLE);
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mCardsLayout.setVisibility(View.GONE);
            }
        }
    }

}
