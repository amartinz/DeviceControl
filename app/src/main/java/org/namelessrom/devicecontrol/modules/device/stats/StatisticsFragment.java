/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.device.stats;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.preferences.AdvancedPreferencesFragment;
import org.namelessrom.devicecontrol.modules.preferences.MainPreferencesFragment;
import org.namelessrom.devicecontrol.ui.views.AttachViewPagerFragment;

import java.util.ArrayList;

public class StatisticsFragment extends AttachViewPagerFragment {

    @Override protected int getFragmentId() {
        return DeviceConstants.ID_DEVICE_STATISTICS;
    }

    @Override public ViewPagerAdapter getPagerAdapter() {
        final ArrayList<Drawable> icons = new ArrayList<>(1);
        final ArrayList<Fragment> fragments = new ArrayList<>(1);

        Drawable cpuIcon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_memory).mutate();
        cpuIcon.setColorFilter(new LightingColorFilter(Color.BLACK, Color.WHITE));
        icons.add(cpuIcon);
        fragments.add(new CpuStatisticsFragment());

        return new ViewPagerAdapter(getChildFragmentManager(), fragments, null, icons);
    }

}
