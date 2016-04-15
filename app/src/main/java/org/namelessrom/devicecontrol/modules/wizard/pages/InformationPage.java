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

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.wizard.setup.Page;
import org.namelessrom.devicecontrol.modules.wizard.setup.SetupDataCallbacks;
import org.namelessrom.devicecontrol.modules.wizard.ui.SetupPageFragment;

import timber.log.Timber;

public class InformationPage extends Page {

    public InformationPage(Context context, SetupDataCallbacks callbacks, int titleResourceId) {
        super(context, callbacks, titleResourceId);
    }

    @Override public Fragment createFragment() {
        final Bundle args = new Bundle();
        args.putString(Page.KEY_PAGE_ARGUMENT, getKey());

        final InformationFragment fragment = new InformationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void refresh() { }

    @Override public int getNextButtonResId() { return R.string.next; }

    public static class InformationFragment extends SetupPageFragment {

        @Override protected void setUpPage() {
            mPage.setRequired(false);
            mPage.setCompleted(true);
            Timber.v("TaskerItem: %s", mCallbacks.getSetupData().toString());
        }

        @Override protected int getLayoutResource() {
            return R.layout.wizard_page_welcome;
        }

        @Override protected int getTitleResource() { return R.string.setup_welcome; }

    }
}
