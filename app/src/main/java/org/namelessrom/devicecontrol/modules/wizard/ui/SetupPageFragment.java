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

package org.namelessrom.devicecontrol.modules.wizard.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.modules.wizard.setup.Page;
import org.namelessrom.devicecontrol.modules.wizard.setup.SetupDataCallbacks;

public abstract class SetupPageFragment extends Fragment {

    protected SetupDataCallbacks mCallbacks;

    protected String mKey;
    protected Page mPage;
    protected View mRootView;

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

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof SetupDataCallbacks)) {
            throw new ClassCastException("Activity must implement SetupDataCallbacks");
        }
        mCallbacks = (SetupDataCallbacks) context;
    }

    @Override public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    protected abstract void setUpPage();

    protected abstract int getLayoutResource();

    protected abstract int getTitleResource();
}
