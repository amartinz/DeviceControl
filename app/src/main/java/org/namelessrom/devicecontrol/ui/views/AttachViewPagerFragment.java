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
package org.namelessrom.devicecontrol.ui.views;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.MainActivityCallbacks;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;

import java.util.ArrayList;

public abstract class AttachViewPagerFragment extends AttachFragment {
    private NoSwipeViewPager mViewPager;

    @Override protected int getFragmentId() {
        return 0;
    }

    @Override public void onResume() {
        super.onResume();
        setSwipeOnContent(false);
    }

    @Override public void onPause() {
        super.onPause();
        setSwipeOnContent(DeviceConfig.get().swipeOnContent);
    }

    private void setSwipeOnContent(boolean swipeOnContent) {
        final Activity activity = getActivity();
        if (activity instanceof MainActivityCallbacks) {
            ((MainActivityCallbacks) activity).setSwipeOnContent(swipeOnContent);
        }
    }

    public NoSwipeViewPager getViewPager() {
        return mViewPager;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        final ViewPagerAdapter adapter = getPagerAdapter();
        mViewPager = (NoSwipeViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(3);

        final TabLayout tabHost = (TabLayout) view.findViewById(R.id.tabHost);
        tabHost.setupWithViewPager(mViewPager);

        return view;
    }

    @Override public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (MainActivity.sDisableFragmentAnimations) {
            final Animation a = new Animation() { };
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    public abstract ViewPagerAdapter getPagerAdapter();

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final ArrayList<Fragment> fragments;
        private final ArrayList<CharSequence> titles;

        public ViewPagerAdapter(final FragmentManager fm, final ArrayList<Fragment> fragments,
                final ArrayList<CharSequence> titles) {
            super(fm);
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override public CharSequence getPageTitle(final int position) {
            return titles.get(position);
        }

        @Override public Fragment getItem(final int position) {
            return fragments.get(position);
        }

        @Override public int getCount() {
            return fragments.size();
        }

    }

}
