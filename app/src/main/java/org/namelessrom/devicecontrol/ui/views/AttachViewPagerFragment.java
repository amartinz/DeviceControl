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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.astuetz.PagerSlidingTabStrip;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

import java.util.ArrayList;

public abstract class AttachViewPagerFragment extends AttachFragment {

    @Override protected int getFragmentId() {
        return 0;
    }

    @Override public void onResume() {
        super.onResume();
        MainActivity.setSwipeOnContent(false);
    }

    @Override public void onPause() {
        super.onPause();
        MainActivity.setSwipeOnContent(PreferenceHelper.getBoolean("swipe_on_content", false));
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        final ViewPagerAdapter adapter = getPagerAdapter();
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        final PagerSlidingTabStrip tabHost = (PagerSlidingTabStrip) view.findViewById(R.id.tabHost);
        tabHost.setViewPager(viewPager);

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
