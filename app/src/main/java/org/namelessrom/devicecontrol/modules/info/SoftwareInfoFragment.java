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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.info.software.DrmView;
import org.namelessrom.devicecontrol.views.AttachFragment;

import at.amartinz.hardware.drm.BaseDrmInfo;
import at.amartinz.hardware.drm.DrmInfoManager;
import at.amartinz.hardware.knox.KnoxInformation;
import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;
import timber.log.Timber;

public class SoftwareInfoFragment extends AttachFragment {
    @BindView(R.id.drm_provider_widevine) DrmView widevineView;
    @BindView(R.id.drm_provider_playready) DrmView playReadyView;

    @Override protected int getFragmentId() { return DeviceConstants.ID_INFO_SOFTWARE; }

    public SoftwareInfoFragment() { }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstState) {
        final View v = inflater.inflate(R.layout.fragment_info_software, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (DrmInfoManager.isSupported()) {
            setupDrmInfo();
        }

        if (BuildConfig.DEBUG) {
            debugPrintKnoxInfo();
        }
    }

    private void setupDrmInfo() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final BaseDrmInfo widevineInfo = DrmInfoManager.getWidevineDrmInfo();
                widevineView.post(new Runnable() {
                    @Override public void run() {
                        widevineView.setDrmInfo(widevineInfo);
                    }
                });
            }
        });
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final BaseDrmInfo playReadyInfo = DrmInfoManager.getPlayReadyDrmInfo();
                playReadyView.post(new Runnable() {
                    @Override public void run() {
                        playReadyView.setDrmInfo(playReadyInfo);
                    }
                });
            }
        });
    }

    // just for testing
    @DebugLog private void debugPrintKnoxInfo() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override public void run() {
                final KnoxInformation knoxInformation = KnoxInformation.get();
                Timber.d("Knox Information:\n%s", knoxInformation);
            }
        });
    }
}
