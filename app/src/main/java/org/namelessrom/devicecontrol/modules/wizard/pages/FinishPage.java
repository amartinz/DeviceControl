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
import android.widget.ListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;
import org.namelessrom.devicecontrol.modules.wizard.setup.Page;
import org.namelessrom.devicecontrol.modules.wizard.setup.SetupDataCallbacks;
import org.namelessrom.devicecontrol.modules.wizard.ui.ReviewAdapter;
import org.namelessrom.devicecontrol.modules.wizard.ui.SetupPageFragment;

import java.util.ArrayList;

import timber.log.Timber;

public class FinishPage extends Page {
    private FinishFragment fragment;

    public FinishPage(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        super(context, callbacks, titleResourceId);
    }

    @Override public Fragment createFragment() {
        final Bundle args = new Bundle();
        args.putString(Page.KEY_PAGE_ARGUMENT, getKey());

        fragment = new FinishFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void refresh() {
        if (fragment != null) {
            fragment.setUpPage();
        }
    }

    @Override public int getNextButtonResId() {
        return R.string.finish;
    }


    public static class FinishFragment extends SetupPageFragment {

        @Override protected void setUpPage() {
            final ListView listView = (ListView) mRootView.findViewById(android.R.id.list);

            final TaskerItem item = mCallbacks.getSetupData();
            final ArrayList<String> entries = new ArrayList<>(4);
            final ArrayList<String> values = new ArrayList<>(4);
            entries.add(getString(R.string.trigger));
            values.add(item.trigger);
            entries.add(getString(R.string.category));
            values.add(item.category);
            entries.add(getString(R.string.action));
            values.add(item.name);
            entries.add(getString(R.string.value));
            values.add(item.value);

            final ReviewAdapter adapter = new ReviewAdapter(getActivity(),
                    R.layout.wizard_list_item_review, entries, values);

            listView.setAdapter(adapter);
            Timber.v("TaskerItem: %s", mCallbacks.getSetupData().toString());
        }

        @Override protected int getLayoutResource() {
            return R.layout.wizard_page_list;
        }

        @Override protected int getTitleResource() {
            return R.string.setup_complete;
        }

    }

}
