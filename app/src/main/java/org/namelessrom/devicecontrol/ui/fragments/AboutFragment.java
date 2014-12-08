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
package org.namelessrom.devicecontrol.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.ui.views.SlidingTabLayout;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.ArrayList;

public class AboutFragment extends AttachFragment {

    private ViewPager mViewPager;
    private SamplePagerAdapter mAdapter;

    // hack around the missing child fragment manager on pre api 17
    private final Handler mHandler = new Handler();
    private Runnable mRunnable;

    @Override protected int getFragmentId() { return DeviceConstants.ID_ABOUT; }

    @Override public void onResume() {
        super.onResume();
        MainActivity.setSwipeOnContent(false);
    }

    @Override public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunnable);
        MainActivity.setSwipeOnContent(PreferenceHelper.getBoolean("swipe_on_content", false));
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter(getFragmentManager()));

        final SlidingTabLayout tabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        tabLayout.setViewPager(mViewPager);

        return view;
    }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new SamplePagerAdapter(getFragmentManager());
        mRunnable = new Runnable() {
            @Override public void run() {
                mViewPager.setAdapter(mAdapter);
            }
        };
        mHandler.post(mRunnable);
    }

    private class SamplePagerAdapter extends FragmentPagerAdapter {
        private static final int COUNT = 3;

        private final ArrayList<Fragment> fragments = new ArrayList<>(COUNT);
        private final ArrayList<CharSequence> titles = new ArrayList<>(COUNT);

        public SamplePagerAdapter(final FragmentManager fm) {
            super(fm);
            fragments.add(new WelcomeFragment());
            titles.add(getString(R.string.about));
            fragments.add(new SupportFragment());
            titles.add(getString(R.string.support));
            fragments.add(new LicenseFragment());
            titles.add(getString(R.string.licenses));
        }

        @Override public CharSequence getPageTitle(final int position) {
            return titles.get(position);
        }

        @Override public Fragment getItem(final int position) {
            return fragments.get(position);
        }

        @Override public int getCount() {
            return COUNT;
        }

    }

}
