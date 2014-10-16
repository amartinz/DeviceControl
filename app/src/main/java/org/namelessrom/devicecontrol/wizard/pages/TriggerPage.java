/*
 * Copyright (C) 2013 The MoKee OpenSource Project
 * Modifications Copyright (C) 2014 The NamelessRom Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.wizard.pages;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.wizard.setup.Page;
import org.namelessrom.devicecontrol.wizard.setup.SetupDataCallbacks;
import org.namelessrom.devicecontrol.wizard.ui.SetupPageFragment;

public class TriggerPage extends Page {

    public TriggerPage(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        super(context, callbacks, titleResourceId);
    }

    @Override
    public Fragment createFragment() {
        Bundle args = new Bundle();
        args.putString(Page.KEY_PAGE_ARGUMENT, getKey());

        EmptyFragment fragment = new EmptyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public int getNextButtonResId() { return R.string.next; }

    public static class EmptyFragment extends SetupPageFragment {

        @Override protected void setUpPage() {
            final ListView listView = (ListView) mRootView.findViewById(android.R.id.list);
        }

        @Override protected int getLayoutResource() { return R.layout.wizard_page_list; }

        @Override protected int getTitleResource() { return R.string.setup_trigger; }

    }
}
