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
package org.namelessrom.devicecontrol.modules.performance;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.hardware.Emmc;
import org.namelessrom.devicecontrol.hardware.IoUtils;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.AlarmHelper;
import org.namelessrom.devicecontrol.utils.Utils;

public class FilesystemFragment extends AttachPreferenceFragment implements IoUtils.IoSchedulerListener, Preference.OnPreferenceChangeListener {

    private CustomListPreference mIoScheduler;
    private CustomPreference mIoSchedulerConfigure;
    private CustomListPreference mReadAhead;

    private AwesomeTogglePreference mFsync;
    private AwesomeTogglePreference mDynFsync;

    private AwesomeTogglePreference mSoftwareCrc;

    private CustomTogglePreference mFstrim;
    private CustomListPreference mFstrimInterval;
    //----------------------------------------------------------------------------------------------

    @Override protected int getFragmentId() { return DeviceConstants.ID_FILESYSTEM; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_filesystem);

        String value;

        mIoScheduler = (CustomListPreference) findPreference("io");
        mIoScheduler.setEnabled(false);
        IoUtils.get().getIoScheduler(this);
        // setting listener when "onIoScheduler" arrives
        mIoSchedulerConfigure = (CustomPreference) findPreference("io_configure");
        mIoSchedulerConfigure.setEnabled(false);

        mReadAhead = (CustomListPreference) findPreference("read_ahead");
        value = Utils.readOneLine(IoUtils.READ_AHEAD_PATH[0]);
        mReadAhead.setValue(value);
        mReadAhead.setSummary(mapReadAhead(value));
        mReadAhead.setOnPreferenceChangeListener(this);

        mFsync = (AwesomeTogglePreference) findPreference("fsync");
        if (mFsync.isSupported()) {
            mFsync.initValue();
            mFsync.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mFsync);
        }

        mDynFsync = (AwesomeTogglePreference) findPreference("dyn_fsync");
        if (mDynFsync.isSupported()) {
            mDynFsync.initValue();
            mDynFsync.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mDynFsync);
        }

        mSoftwareCrc = (AwesomeTogglePreference) findPreference("mmc_software_crc");
        if (mSoftwareCrc.isSupported()) {
            mSoftwareCrc.initValue();
            mSoftwareCrc.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mSoftwareCrc);
        }

        final boolean canBrickEmmc = Emmc.get().canBrick();
        mFstrim = (CustomTogglePreference) findPreference(TaskerConfig.FSTRIM);
        if (canBrickEmmc) {
            mFstrim.setEnabled(false);
        } else {
            mFstrim.setChecked(TaskerConfig.get().fstrimEnabled);
            mFstrim.setOnPreferenceChangeListener(this);
        }

        mFstrimInterval =
                (CustomListPreference) findPreference(TaskerConfig.FSTRIM_INTERVAL);
        if (canBrickEmmc) {
            mFstrimInterval.setEnabled(false);
        } else {
            mFstrimInterval.setValueIndex(getFstrim());
            mFstrimInterval.setOnPreferenceChangeListener(this);
        }
    }

    @Override public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mIoSchedulerConfigure) {
            MainActivity.loadFragment(getActivity(), DeviceConstants.ID_IOSCHED_TUNING);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (preference == mIoScheduler) {
            final String value = String.valueOf(o);
            mIoScheduler.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_IO_SCHEDULER, value, true);
            updateIoSchedulerConfigure(value);
            return true;
        } else if (preference == mFsync) {
            mFsync.writeValue((Boolean) o);
            return true;
        } else if (preference == mDynFsync) {
            mDynFsync.writeValue((Boolean) o);
            return true;
        } else if (preference == mSoftwareCrc) {
            mSoftwareCrc.writeValue((Boolean) o);
            return true;
        } else if (preference == mReadAhead) {
            final String value = String.valueOf(o);
            mReadAhead.setSummary(mapReadAhead(value));
            ActionProcessor.processAction(ActionProcessor.ACTION_READ_AHEAD, value, true);
            return true;
        } else if (mFstrim == preference) {
            final boolean value = (Boolean) o;

            TaskerConfig.get().fstrimEnabled = value;
            TaskerConfig.get().save();

            if (value) {
                AlarmHelper.setAlarmFstrim(getActivity(),
                        parseFstrim(mFstrimInterval.getValue()));
            } else {
                AlarmHelper.cancelAlarmFstrim(getActivity());
            }
            mFstrim.setChecked(value);
            Logger.v(this, String.format("mFstrim: %s", value));
            return true;
        } else if (mFstrimInterval == preference) {
            final String value = String.valueOf(o);
            final int realValue = parseFstrim(value);

            TaskerConfig.get().fstrimInterval = realValue;
            TaskerConfig.get().save();

            if (mFstrim.isChecked()) {
                AlarmHelper.setAlarmFstrim(getActivity(), realValue);
            }
            Logger.v(this, "mFstrimInterval: " + value);
            return true;
        }

        return false;
    }

    private String mapReadAhead(final String value) {
        int val;

        // try to parse the value we are writing
        try {
            val = Utils.parseInt(value);
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
            case 8192:
                return Application.get().getString(R.string.size_8192_kb);
            // if all fails, we return the value we got at the beginning
            default:
            case -1:
                return value;
        }
    }

    @Override public void onIoScheduler(final IoUtils.IoScheduler ioScheduler) {
        final Activity activity = getActivity();
        if (activity != null && ioScheduler != null) {
            if (ioScheduler.available != null && ioScheduler.available.length > 0
                    && !TextUtils.isEmpty(ioScheduler.current)) {
                mIoScheduler.setEntries(ioScheduler.available);
                mIoScheduler.setEntryValues(ioScheduler.available);
                mIoScheduler.setValue(ioScheduler.current);
                mIoScheduler.setSummary(ioScheduler.current);
                mIoScheduler.setOnPreferenceChangeListener(this);
                mIoScheduler.setEnabled(true);

                updateIoSchedulerConfigure(ioScheduler.current);
            }
        }
    }

    private void updateIoSchedulerConfigure(final String scheduler) {
        final String title = getString(R.string.configure_format, scheduler);
        mIoSchedulerConfigure.setTitle(title);
        mIoSchedulerConfigure.setEnabled(true);
    }

    private int parseFstrim(final String position) {
        try {
            return parseFstrim(Utils.parseInt(position));
        } catch (Exception exc) {
            return 480;
        }
    }

    private int parseFstrim(final int position) {
        int value;
        switch (position) {
            case 0:
                value = 5;
                break;
            case 1:
                value = 10;
                break;
            case 2:
                value = 20;
                break;
            case 3:
                value = 30;
                break;
            case 4:
                value = 60;
                break;
            case 5:
                value = 120;
                break;
            case 6:
                value = 240;
                break;
            default:
            case 7:
                value = 480;
                break;
        }
        return value;
    }

    private int getFstrim() {
        int position;

        final int value = TaskerConfig.get().fstrimInterval;
        switch (value) {
            case 5:
                position = 0;
                break;
            case 10:
                position = 1;
                break;
            case 20:
                position = 2;
                break;
            case 30:
                position = 3;
                break;
            case 60:
                position = 4;
                break;
            case 120:
                position = 5;
                break;
            case 240:
                position = 6;
                break;
            default:
            case 480:
                position = 7;
                break;
        }

        return position;
    }

}
