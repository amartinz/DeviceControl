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
package org.namelessrom.devicecontrol.modules.wizard.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.modules.wizard.setup.Page;
import org.namelessrom.devicecontrol.modules.wizard.setup.SetupDataCallbacks;
import org.namelessrom.devicecontrol.modules.wizard.ui.SetupPageFragment;

import java.util.ArrayList;

import timber.log.Timber;

public class ValuePage extends Page {
    private ValueFragment fragment;

    public ValuePage(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        super(context, callbacks, titleResourceId);
    }

    @Override public Fragment createFragment() {
        final Bundle args = new Bundle();
        args.putString(Page.KEY_PAGE_ARGUMENT, getKey());

        fragment = new ValueFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void refresh() {
        if (fragment != null) {
            fragment.setUpPage();
        }
    }

    @Override public int getNextButtonResId() { return R.string.next; }

    public static class ValueFragment extends SetupPageFragment {

        @Override protected void setUpPage() {
            mPage.setRequired(true);
            final ListView listView = (ListView) mRootView.findViewById(android.R.id.list);

            final String action = mCallbacks.getSetupData().name;
            final ArrayList<ActionProcessor.Entry> values = ActionProcessor.getValues(action);
            final ArrayList<String> entries = new ArrayList<>(values.size());
            final ArrayList<String> entryValues = new ArrayList<>(values.size());
            for (final ActionProcessor.Entry value : values) {
                entries.add(value.name);
                entryValues.add(value.value);
            }

            final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_single_choice, entries);

            listView.setAdapter(adapter);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override public void onItemClick(AdapterView<?> adapterView, View view, int i,
                        long l) {
                    Timber.i("position: %s | values: %s", i, entryValues.get(i));
                    setData(entryValues.get(i));
                    mCallbacks.onPageLoaded(mPage);
                }
            });

            final String entry = mCallbacks.getSetupData().value;
            if (!TextUtils.isEmpty(entry)) {
                listView.setItemChecked(entryValues.indexOf(entry), true);
                setData(entry);
            }
        }

        private void setData(final String entry) {
            mCallbacks.getSetupData().value = entry;
            mPage.setCompleted(true);
        }

        @Override protected int getLayoutResource() {
            return R.layout.wizard_page_list;
        }

        @Override protected int getTitleResource() { return R.string.setup_value; }

    }
}
