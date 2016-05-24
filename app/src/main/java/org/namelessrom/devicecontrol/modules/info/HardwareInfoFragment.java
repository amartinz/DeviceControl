/*
 * Copyright (C) 2013 - 2016 Alexander Martinz
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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.info.hardware.FingerprintView;
import org.namelessrom.devicecontrol.modules.info.hardware.GpsView;
import org.namelessrom.devicecontrol.views.AttachFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HardwareInfoFragment extends AttachFragment {
    @BindView(R.id.hardware_fingerprint) FingerprintView fingerprintView;
    @BindView(R.id.hardware_gps) GpsView gpsView;

    @Override protected int getFragmentId() { return DeviceConstants.ID_INFO_HARDWARE; }

    public HardwareInfoFragment() { }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstState) {
        final View v = inflater.inflate(R.layout.fragment_info_hardware, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override public void onResume() {
        super.onResume();
        fingerprintView.onResume();
        gpsView.onResume();
    }

    @Override public void onPause() {
        super.onPause();
        fingerprintView.onPause();
        gpsView.onPause();
    }

}
