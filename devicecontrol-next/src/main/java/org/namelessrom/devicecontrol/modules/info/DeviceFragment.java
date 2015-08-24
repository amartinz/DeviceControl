/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.modules.info;

import android.support.v4.app.Fragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseViewPagerFragment;

import java.util.ArrayList;

public class DeviceFragment extends BaseViewPagerFragment {
    @Override public ViewPagerAdapter getPagerAdapter() {
        final ArrayList<Fragment> fragments = new ArrayList<>(2);
        final ArrayList<CharSequence> titles = new ArrayList<>(2);

        fragments.add(new DeviceGeneralFragment());
        titles.add(getString(R.string.general));

        fragments.add(new DeviceSensorFragment());
        titles.add(getString(R.string.sensors));

        return new ViewPagerAdapter(getChildFragmentManager(), fragments, titles);
    }
}
