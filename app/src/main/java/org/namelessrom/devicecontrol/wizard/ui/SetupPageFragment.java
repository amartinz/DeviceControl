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

package org.namelessrom.devicecontrol.wizard.ui;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.wizard.setup.Page;
import org.namelessrom.devicecontrol.wizard.setup.SetupDataCallbacks;

public abstract class SetupPageFragment extends Fragment {

    protected SetupDataCallbacks mCallbacks;
    protected String             mKey;
    protected Page               mPage;
    protected View               mRootView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        mKey = args.getString(Page.KEY_PAGE_ARGUMENT);
        if (mKey == null) {
            throw new IllegalArgumentException("No KEY_PAGE_ARGUMENT given");
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(getLayoutResource(), container, false);
        TextView titleView = (TextView) mRootView.findViewById(android.R.id.title);
        titleView.setText(getTitleResource());
        return mRootView;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPage = mCallbacks.getPage(mKey);
        setUpPage();
    }

    @Override public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof SetupDataCallbacks)) {
            throw new ClassCastException("Activity must implement SetupDataCallbacks");
        }
        mCallbacks = (SetupDataCallbacks) activity;
    }

    @Override public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    protected abstract void setUpPage();

    protected abstract int getLayoutResource();

    protected abstract int getTitleResource();
}
