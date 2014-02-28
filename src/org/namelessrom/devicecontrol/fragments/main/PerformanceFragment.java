/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.main;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.parents.AttachFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceCpuSettings;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceGeneralFragment;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceInformationFragment;
import org.namelessrom.devicecontrol.widgets.adapters.ScreenSlidePagerAdapter;
import org.namelessrom.devicecontrol.widgets.JfViewPager;

import java.util.ArrayList;
import java.util.List;

public class PerformanceFragment extends AttachFragment {

    public static final int ID = 2;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View rootView = layoutInflater.inflate(R.layout.fragment_viewpager, viewGroup, false);

        List<Fragment> mFragments = getFragments();
        List<String> mTitles = getTitles();

        JfViewPager mViewPager = (JfViewPager) rootView.findViewById(R.id.pager);

        ScreenSlidePagerAdapter mTabsAdapter = new ScreenSlidePagerAdapter(
                getChildFragmentManager(), mFragments, mTitles);
        mViewPager.setAdapter(mTabsAdapter);

        PagerTabStrip mPagerTabStrip = (PagerTabStrip) rootView.findViewById(R.id.pagerTabStrip);
        mPagerTabStrip.setDrawFullUnderline(false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, PerformanceFragment.ID);
    }

    private List<Fragment> getFragments() {
        List<Fragment> tmpList = new ArrayList<Fragment>();
        tmpList.add(new PerformanceInformationFragment());
        tmpList.add(new PerformanceCpuSettings());
        if (PerformanceGeneralFragment.isSupported(getActivity())) {
            tmpList.add(new PerformanceGeneralFragment());
        }
        return tmpList;
    }

    private List<String> getTitles() {
        List<String> tmpList = new ArrayList<String>();
        tmpList.add(getString(R.string.section_title_information));
        tmpList.add(getString(R.string.cpusettings));
        if (PerformanceGeneralFragment.isSupported(getActivity())) {
            tmpList.add(getString(R.string.general));
        }
        return tmpList;
    }
}
