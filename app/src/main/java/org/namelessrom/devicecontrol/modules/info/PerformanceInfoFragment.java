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
package org.namelessrom.devicecontrol.modules.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.views.AttachFragment;
import org.namelessrom.devicecontrol.views.CpuStateView;
import org.namelessrom.devicecontrol.views.DeviceStatusView;

public class PerformanceInfoFragment extends AttachFragment {

    private DeviceStatusView mDeviceStats;
    private CpuStateView mCpuStates;

    @Override protected int getFragmentId() { return DeviceConstants.ID_INFO_PERFORMANCE; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_performance, container, false);

        mDeviceStats = (DeviceStatusView) v.findViewById(R.id.device_stats);
        mCpuStates = (CpuStateView) v.findViewById(R.id.cpu_states);

        return v;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();

        switch (id) {
            case R.id.menu_action_refresh: {
                if (mCpuStates != null) {
                    mCpuStates.refreshData();
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onResume() {
        super.onResume();
        if (mDeviceStats != null) {
            mDeviceStats.onResume();
        }
        if (mCpuStates != null) {
            mCpuStates.onResume();
        }
    }

    @Override public void onPause() {
        super.onPause();
        if (mDeviceStats != null) {
            mDeviceStats.onPause();
        }
        if (mCpuStates != null) {
            mCpuStates.onPause();
        }
    }

    @Override public void onDestroy() {
        if (mCpuStates != null) {
            mCpuStates.onDestroy();
        }
        super.onDestroy();
    }
}
