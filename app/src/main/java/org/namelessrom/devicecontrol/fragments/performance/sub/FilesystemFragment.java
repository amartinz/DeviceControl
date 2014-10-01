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
package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.IoSchedulerEvent;
import org.namelessrom.devicecontrol.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class FilesystemFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener {

    private CustomListPreference mIoScheduler;
    private CustomListPreference mReadAhead;

    private AwesomeCheckBoxPreference mFsync;
    private AwesomeCheckBoxPreference mDynFsync;
    //----------------------------------------------------------------------------------------------

    @Override protected int getFragmentId() { return ID_FILESYSTEM; }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_filesystem);

        String value;

        mIoScheduler = (CustomListPreference) findPreference("io");
        mIoScheduler.setEnabled(false);
        CpuUtils.getIoSchedulerEvent();
        // setting listener when "onIoScheduler" arrives

        mReadAhead = (CustomListPreference) findPreference("read_ahead");
        value = Utils.readOneLine(PerformanceConstants.READ_AHEAD_PATH[0]);
        mReadAhead.setValue(value);
        mReadAhead.setSummary(mapReadAhead(value));
        mReadAhead.setOnPreferenceChangeListener(this);

        mFsync = (AwesomeCheckBoxPreference) findPreference("fsync");
        if (mFsync.isSupported()) {
            mFsync.initValue();
            mFsync.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mFsync);
        }

        mDynFsync = (AwesomeCheckBoxPreference) findPreference("dyn_fsync");
        if (mDynFsync.isSupported()) {
            mDynFsync.initValue();
            mDynFsync.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mDynFsync);
        }
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (preference == mIoScheduler) {
            final String value = String.valueOf(o);
            mIoScheduler.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_IO_SCHEDULER, value, true);
            return true;
        } else if (preference == mFsync) {
            mFsync.writeValue((Boolean) o);
            return true;
        } else if (preference == mDynFsync) {
            mDynFsync.writeValue((Boolean) o);
            return true;
        } else if (preference == mReadAhead) {
            final String value = String.valueOf(o);
            mReadAhead.setSummary(mapReadAhead(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_READ_AHEAD, value, true);
            return true;
        }

        return false;
    }

    private String mapReadAhead(final String value) {
        int val;

        // try to parse the value we are writing
        try {
            val = Integer.parseInt(value);
        } catch (Exception exc) {
            Logger.e(this, exc.getMessage());
            val = -1;
        }

        // check the value and return the corresponding string
        switch (val) {
            case 128:
                return Application.get().getString(R.string.size_128_kb);
            case 256:
                return Application.get().getString(R.string.size_256_kb);
            case 512:
                return Application.get().getString(R.string.size_512_kb);
            case 1024:
                return Application.get().getString(R.string.size_1024_kb);
            case 2048:
                return Application.get().getString(R.string.size_2048_kb);
            case 3072:
                return Application.get().getString(R.string.size_3072_kb);
            case 4096:
                return Application.get().getString(R.string.size_4096_kb);
            // if all fails, we return the value we got at the beginning
            default:
            case -1:
                return value;
        }
    }

    @Subscribe public void onIoScheduler(final IoSchedulerEvent event) {
        final Activity activity = getActivity();
        if (activity != null && event != null) {
            final String[] mAvailableIo = event.getAvailableIoScheduler();
            final String mCurrentIo = event.getCurrentIoScheduler();
            if (mAvailableIo != null && mAvailableIo.length > 0
                    && mCurrentIo != null && !mCurrentIo.isEmpty()) {
                mIoScheduler.setEntries(mAvailableIo);
                mIoScheduler.setEntryValues(mAvailableIo);
                mIoScheduler.setValue(mCurrentIo);
                mIoScheduler.setSummary(mCurrentIo);
                mIoScheduler.setOnPreferenceChangeListener(this);
                mIoScheduler.setEnabled(true);
            }
        }
    }

}
