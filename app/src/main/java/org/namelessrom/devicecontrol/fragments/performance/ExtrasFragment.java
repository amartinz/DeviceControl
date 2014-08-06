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

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.IoSchedulerEvent;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.List;

public class ExtrasFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    //==============================================================================================
    // Files
    //==============================================================================================

    public static final String  sMcPowerSchedulerFile = Utils.checkPaths(FILES_MC_POWER_SCHEDULER);
    public static final boolean sMcPowerScheduler     = !sMcPowerSchedulerFile.isEmpty();
    //----------------------------------------------------------------------------------------------

    private PreferenceScreen mRoot;
    //----------------------------------------------------------------------------------------------

    private CustomListPreference mIoScheduler;
    private CustomPreference     mEntropy;
    private CustomPreference     mKsm;
    private CustomPreference     mHotplugging;
    private CustomPreference     mThermal;
    //----------------------------------------------------------------------------------------------

    private AwesomeCheckBoxPreference mPowerEfficientWork;
    private CustomListPreference      mMcPowerScheduler;
    //----------------------------------------------------------------------------------------------

    private AwesomeCheckBoxPreference mMsmDcvs;
    private CustomPreference          mVoltageControl;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_PERFORMANCE_EXTRA); }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras);
        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference("general");
        if (category != null) {
            mIoScheduler = (CustomListPreference) findPreference("io");
            if (mIoScheduler != null) {
                mIoScheduler.setEnabled(false);
                CpuUtils.getIoSchedulerEvent();
            }

            mEntropy = (CustomPreference) findPreference("entropy");
            if (mEntropy != null) {
                mEntropy.setOnPreferenceClickListener(this);
            }
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Kernel Features
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("kernel_features");
        if (category != null) {
            mKsm = (CustomPreference) findPreference("ksm");
            if (mKsm != null) {
                if (Utils.fileExists(KSM_PATH)) {
                    mKsm.setOnPreferenceClickListener(this);
                } else {
                    category.removePreference(mKsm);
                }
            }

            mHotplugging = (CustomPreference) findPreference("hotplugging");
            if (mHotplugging != null) {
                if (Utils.fileExists(getString(R.string.file_intelli_plug_base))
                        || Utils.fileExists(getString(R.string.file_cpu_quiet_base))
                        || Utils.fileExists(MPDECISION_PATH)) {
                    mHotplugging.setOnPreferenceClickListener(this);
                } else {
                    category.removePreference(mHotplugging);
                }
            }

            mThermal = (CustomPreference) findPreference("thermal");
            if (mThermal != null) {
                if (Utils.fileExists(MSM_THERMAL_PARAMS)
                        || Utils.fileExists(getString(R.string.file_intelli_thermal_base))) {
                    mThermal.setOnPreferenceClickListener(this);
                } else {
                    category.removePreference(mThermal);
                }
            }
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Power Saving
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("powersaving");
        if (category != null) {
            mPowerEfficientWork =
                    (AwesomeCheckBoxPreference) findPreference("power_efficient_work");
            if (mPowerEfficientWork != null) {
                if (mPowerEfficientWork.isSupported()) {
                    mPowerEfficientWork.initValue();
                    mPowerEfficientWork.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mPowerEfficientWork);
                }
            }
            mMcPowerScheduler = (CustomListPreference) findPreference("sched_mc_power_savings");
            if (mMcPowerScheduler != null) {
                if (sMcPowerScheduler) {
                    final String value = Utils.readOneLine(sMcPowerSchedulerFile);
                    mMcPowerScheduler.setValue(value);
                    mMcPowerScheduler.setSummary(mMcPowerScheduler.getEntry());
                    mMcPowerScheduler.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMcPowerScheduler);
                }
            }
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Voltage
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("voltage");
        if (category != null) {
            mMsmDcvs = (AwesomeCheckBoxPreference) findPreference("msm_dcvs");
            if (mMsmDcvs != null) {
                if (mMsmDcvs.isSupported()) {
                    mMsmDcvs.initValue();
                    mMsmDcvs.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmDcvs);
                }
            }

            mVoltageControl = (CustomPreference) findPreference("voltage_control");
            if (mVoltageControl != null) {
                if (Utils.fileExists(VDD_TABLE_FILE) || Utils.fileExists(UV_TABLE_FILE)) {
                    mVoltageControl.setOnPreferenceClickListener(this);
                } else {
                    category.removePreference(mVoltageControl);
                }
            }
        }
        removeIfEmpty(category);

        isSupported(mRoot, getActivity());
    }

    private void removeIfEmpty(final PreferenceGroup preferenceGroup) {
        if (mRoot != null && preferenceGroup.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceGroup);
        }
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        if (mVoltageControl == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_VOLTAGE));
            return true;
        } else if (mHotplugging == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_HOTPLUGGING));
            return true;
        } else if (mThermal == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_THERMAL));
            return true;
        } else if (mKsm == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_KSM));
            return true;
        } else if (mEntropy == preference) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_ENTROPY));
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mIoScheduler) {
            final String value = String.valueOf(o);
            mIoScheduler.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_IO_SCHEDULER, value, true);
            changed = true;
        } else if (preference == mPowerEfficientWork) {
            mPowerEfficientWork.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mMcPowerScheduler) {
            final String value = String.valueOf(o);
            Utils.writeValue(sMcPowerSchedulerFile, value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS,
                    mMcPowerScheduler.getKey(), sMcPowerSchedulerFile, value));
            if (mMcPowerScheduler.getEntries() != null) {
                final String summary = String.valueOf(
                        mMcPowerScheduler.getEntries()[Integer.parseInt(value)]);
                mMcPowerScheduler.setSummary(summary);
            }
            changed = true;
        } else if (preference == mMsmDcvs) {
            mMsmDcvs.writeValue((Boolean) o);
            changed = true;
        }

        return changed;
    }

    @Subscribe
    public void onIoScheduler(final IoSchedulerEvent event) {
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

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = DatabaseHandler.getInstance().getAllItems(
                DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_EXTRAS);
        String name, value;
        for (final DataItem item : items) {
            name = item.getFileName();
            value = item.getValue();

            if (MPDECISION_PATH.equals(name)) {
                sbCmd.append(CpuUtils.enableMpDecision(value.equals("1")));
            } else {
                sbCmd.append(Utils.getWriteCommand(name, value));
            }
        }

        return sbCmd.toString();
    }

}
