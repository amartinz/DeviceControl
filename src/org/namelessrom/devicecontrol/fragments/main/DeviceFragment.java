/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
import org.namelessrom.devicecontrol.fragments.device.DeviceGraphicsFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceHelpFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceInputFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceLightsFragment;
import org.namelessrom.devicecontrol.fragments.device.DeviceSensorsFragment;
import org.namelessrom.devicecontrol.fragments.parents.AttachFragment;
import org.namelessrom.devicecontrol.utils.adapters.ScreenSlidePagerAdapter;
import org.namelessrom.devicecontrol.utils.widgets.JfViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 18.12.13.
 */
public class DeviceFragment extends AttachFragment {

    public static final int ID = 1;

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
        /*mPagerTabStrip.setBackgroundColor(getResources()
                .getColor(android.R.color.holo_green_dark));
        mPagerTabStrip.setTabIndicatorColor(getResources()
                .getColor(android.R.color.holo_green_light));*/
        mPagerTabStrip.setDrawFullUnderline(false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, DeviceFragment.ID);
    }

    private List<Fragment> getFragments() {
        List<Fragment> tmpList = new ArrayList<Fragment>();
        tmpList.add(new DeviceHelpFragment());
        if (DeviceInputFragment.isSupported()) {
            tmpList.add(new DeviceInputFragment());
        }
        if (DeviceLightsFragment.isSupported()) {
            tmpList.add(new DeviceLightsFragment());
        }
        if (DeviceGraphicsFragment.isSupported()) {
            tmpList.add(new DeviceGraphicsFragment());
        }
        if (DeviceSensorsFragment.isSupported()) {
            tmpList.add(new DeviceSensorsFragment());
        }
        return tmpList;
    }

    private List<String> getTitles() {
        List<String> tmpList = new ArrayList<String>();
        tmpList.add(getString(R.string.section_title_information));
        if (DeviceInputFragment.isSupported()) {
            tmpList.add(getString(R.string.section_title_device_input));
        }
        if (DeviceLightsFragment.isSupported()) {
            tmpList.add(getString(R.string.section_title_device_light));
        }
        if (DeviceGraphicsFragment.isSupported()) {
            tmpList.add(getString(R.string.section_title_device_graphics));
        }
        if (DeviceSensorsFragment.isSupported()) {
            tmpList.add(getString(R.string.section_title_device_sensors));
        }
        return tmpList;
    }
}
