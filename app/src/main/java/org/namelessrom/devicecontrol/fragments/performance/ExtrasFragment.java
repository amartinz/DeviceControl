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
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

import java.util.List;

public class ExtrasFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final int ID_WORK = 100;

    //==============================================================================================
    // Files
    //==============================================================================================

    public static final String  sPowerEfficientWorkFile =
            Utils.checkPaths(FILES_POWER_EFFICIENT_WORK);
    public static final boolean sPowerEfficientWork     = !sPowerEfficientWorkFile.isEmpty();
    //----------------------------------------------------------------------------------------------

    public static final String  sMcPowerSchedulerFile = Utils.checkPaths(FILES_MC_POWER_SCHEDULER);
    public static final boolean sMcPowerScheduler     = !sMcPowerSchedulerFile.isEmpty();
    //----------------------------------------------------------------------------------------------

    private static final String CATEGORY_POWERSAVING = "powersaving";
    //----------------------------------------------------------------------------------------------

    private PreferenceScreen mRoot;
    //----------------------------------------------------------------------------------------------

    private CustomListPreference mIoScheduler;
    private CustomPreference     mKsm;
    private CustomPreference     mHotplugging;
    private CustomPreference     mThermal;
    //----------------------------------------------------------------------------------------------

    private CustomCheckBoxPreference mPowerEfficientWork;
    private CustomListPreference     mMcPowerScheduler;
    //----------------------------------------------------------------------------------------------

    private CustomCheckBoxPreference mMsmDcvs;
    private CustomPreference         mVoltageControl;

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

        mIoScheduler = (CustomListPreference) findPreference("io");
        if (mIoScheduler != null) {
            mIoScheduler.setEnabled(false);
            CpuUtils.getIoSchedulerEvent();
        }

        mKsm = (CustomPreference) findPreference("ksm");
        if (mKsm != null) {
            mKsm.setOnPreferenceClickListener(this);
        }

        mHotplugging = (CustomPreference) findPreference("hotplugging");
        if (mHotplugging != null) {
            mHotplugging.setOnPreferenceClickListener(this);
        }

        mThermal = (CustomPreference) findPreference("thermal");
        if (mThermal != null) {
            mThermal.setOnPreferenceClickListener(this);
        }

        //------------------------------------------------------------------------------------------
        // Power Saving
        //------------------------------------------------------------------------------------------

        PreferenceCategory category = (PreferenceCategory) findPreference(CATEGORY_POWERSAVING);
        if (category != null) {
            mPowerEfficientWork = (CustomCheckBoxPreference) findPreference("power_efficient_work");
            if (mPowerEfficientWork != null) {
                if (sPowerEfficientWork) {
                    Utils.getCommandResult(ID_WORK, Utils.getReadCommand(sPowerEfficientWorkFile));
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
            mMsmDcvs = (CustomCheckBoxPreference) findPreference("msm_dcvs");
            if (mMsmDcvs != null) {
                if (CpuUtils.hasMsmDcvs()) {
                    mMsmDcvs.setChecked(CpuUtils.isMsmDcvs());
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
            final boolean rawValue = (Boolean) o;
            final String value = rawValue ? "1" : "0";
            Utils.runRootCommand(Utils.getWriteCommand(sPowerEfficientWorkFile, value));
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mPowerEfficientWork.getKey(),
                    sPowerEfficientWorkFile, value));
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
            final boolean value = (Boolean) o;
            CpuUtils.enableMsmDcvs(value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mMsmDcvs.getKey(),
                    MSM_DCVS_FILE, value ? "1" : "0"));
            changed = true;
        }

        return changed;
    }

    @Subscribe
    public void onGetCommandResult(final ShellOutputEvent event) {
        if (event != null) {
            final int id = event.getId();
            final String result = event.getOutput();
            switch (id) {
                case ID_WORK:
                    if (mPowerEfficientWork != null) {
                        mPowerEfficientWork.setChecked(result.equals("Y"));
                        mPowerEfficientWork.setOnPreferenceChangeListener(this);
                    }
                    break;
                default:
                    break;
            }
        }
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

    public static String restore(final DatabaseHandler db) {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = db.getAllItems(
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
