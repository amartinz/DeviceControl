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
package org.namelessrom.devicecontrol.fragments.performance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.ui.views.DeviceStatusView;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class InformationFragment extends AttachFragment implements DeviceConstants {

    private DeviceStatusView mDeviceStats;

    @Override protected int getFragmentId() { return ID_PERFORMANCE_INFO; }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_performance, container, false);

        mDeviceStats = (DeviceStatusView) v.findViewById(R.id.device_stats);

        return v;
    }

    @Override public void onResume() {
        super.onResume();
        if (mDeviceStats != null) {
            mDeviceStats.onResume();
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (mDeviceStats != null) {
            mDeviceStats.onPause();
        }
    }
}
