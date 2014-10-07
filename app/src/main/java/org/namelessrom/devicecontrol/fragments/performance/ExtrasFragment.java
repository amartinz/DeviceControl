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
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.Constants;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.List;

public class ExtrasFragment extends AttachPreferenceFragment
        implements DeviceConstants, Constants,
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    //==============================================================================================
    // Files
    //==============================================================================================
    private static final String TCP_CONGESTION_AVAILABLE =
            "/proc/sys/net/ipv4/tcp_available_congestion_control";
    private static final String TCP_CONGESTION_CONTROL   =
            "/proc/sys/net/ipv4/tcp_congestion_control";

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen mRoot;

    //----------------------------------------------------------------------------------------------
    private CustomPreference mEntropy;
    private CustomPreference mFilesystem;
    private CustomPreference mKsm;
    private CustomPreference mHotplugging;
    private CustomPreference mThermal;

    //----------------------------------------------------------------------------------------------
    private AwesomeCheckBoxPreference mPowerEfficientWork;
    private AwesomeListPreference     mMcPowerScheduler;

    //----------------------------------------------------------------------------------------------
    private AwesomeCheckBoxPreference mMsmDcvs;
    private CustomPreference          mVoltageControl;

    //----------------------------------------------------------------------------------------------
    private CustomListPreference mTcpCongestion;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return ID_PERFORMANCE_EXTRA; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras);
        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference("general");
        if (category != null) {
            mFilesystem = (CustomPreference) findPreference("filesystem");
            if (mFilesystem != null) {
                mFilesystem.setOnPreferenceClickListener(this);
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

            mMcPowerScheduler = (AwesomeListPreference) findPreference("sched_mc_power_savings");
            if (mMcPowerScheduler != null) {
                if (mMcPowerScheduler.isSupported()) {
                    mMcPowerScheduler.initValue();
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

        //------------------------------------------------------------------------------------------
        // Extras
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("extras");
        buildExtraCategory(category);
        removeIfEmpty(category);

        isSupported(mRoot, getActivity());
    }

    private void buildExtraCategory(final PreferenceCategory category) {
        mTcpCongestion = (CustomListPreference) findPreference("tcp_congestion_control");
        // read the available tcp congestion controls
        String tmp = Utils.readFile(TCP_CONGESTION_AVAILABLE);
        if (!TextUtils.isEmpty(tmp)) {
            // split them
            final String[] tcp_avail = tmp.trim().split(" ");
            // read the current congestion control
            tmp = Utils.readFile(TCP_CONGESTION_CONTROL);
            if (!TextUtils.isEmpty(tmp)) {
                tmp = tmp.trim();
                mTcpCongestion.setEntries(tcp_avail);
                mTcpCongestion.setEntryValues(tcp_avail);
                mTcpCongestion.setSummary(tmp);
                mTcpCongestion.setValue(tmp);
                mTcpCongestion.setOnPreferenceChangeListener(this);
            }
        } else {
            category.removePreference(mTcpCongestion);
        }
    }

    private void removeIfEmpty(final PreferenceGroup preferenceGroup) {
        if (mRoot != null && preferenceGroup.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceGroup);
        }
    }

    @Override public boolean onPreferenceClick(final Preference preference) {
        if (mVoltageControl == preference) {
            MainActivity.loadFragment(getActivity(), ID_VOLTAGE);
            return true;
        } else if (mHotplugging == preference) {
            MainActivity.loadFragment(getActivity(), ID_HOTPLUGGING);
            return true;
        } else if (mThermal == preference) {
            MainActivity.loadFragment(getActivity(), ID_THERMAL);
            return true;
        } else if (mKsm == preference) {
            MainActivity.loadFragment(getActivity(), ID_KSM);
            return true;
        } else if (mEntropy == preference) {
            MainActivity.loadFragment(getActivity(), ID_ENTROPY);
            return true;
        } else if (mFilesystem == preference) {
            MainActivity.loadFragment(getActivity(), ID_FILESYSTEM);
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mPowerEfficientWork) {
            mPowerEfficientWork.writeValue((Boolean) o);
            return true;
        } else if (preference == mMcPowerScheduler) {
            final String value = String.valueOf(o);
            mMcPowerScheduler.writeValue(value);
            if (mMcPowerScheduler.getEntries() != null) {
                final String summary =
                        String.valueOf(mMcPowerScheduler.getEntries()[Integer.parseInt(value)]);
                mMcPowerScheduler.setSummary(summary);
            }
            return true;
        } else if (preference == mMsmDcvs) {
            mMsmDcvs.writeValue((Boolean) o);
            return true;
        } else if (preference == mTcpCongestion) {
            final String value = String.valueOf(o);
            Utils.writeValue(TCP_CONGESTION_CONTROL, value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS,
                    mTcpCongestion.getKey(), TCP_CONGESTION_CONTROL, value));
            preference.setSummary(value);
            return true;
        }

        return false;
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
                sbCmd.append(CpuUtils.get().enableMpDecision(value.equals("1")));
            } else {
                sbCmd.append(Utils.getWriteCommand(name, value));
            }
        }

        return sbCmd.toString();
    }

}
