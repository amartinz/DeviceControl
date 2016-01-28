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
package org.namelessrom.devicecontrol.modules.device;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.hardware.KsmUtils;
import org.namelessrom.devicecontrol.hardware.UksmUtils;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.preferences.AwesomeListPreference;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.views.CustomPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class DeviceFeatureKernelFragment extends CustomPreferenceFragment implements Preference.OnPreferenceClickListener {
    //==============================================================================================
    // Files
    //==============================================================================================
    private static final String TCP_CONGESTION_AVAILABLE =
            "/proc/sys/net/ipv4/tcp_available_congestion_control";
    private static final String TCP_CONGESTION_CONTROL =
            "/proc/sys/net/ipv4/tcp_congestion_control";

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen mRoot;

    //----------------------------------------------------------------------------------------------
    private CustomPreference mEntropy;
    private CustomPreference mKsm;
    private CustomPreference mUksm;

    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mPowerEfficientWork;
    private AwesomeListPreference mMcPowerScheduler;

    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mMsmDcvs;
    private CustomPreference mVoltageControl;

    //----------------------------------------------------------------------------------------------
    private CustomListPreference mTcpCongestion;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_feature_kernel);
        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // Kernel Features
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference("kernel_features");

        mEntropy = (CustomPreference) findPreference("entropy");
        mEntropy.setOnPreferenceClickListener(this);

        mKsm = (CustomPreference) findPreference("ksm");
        if (Utils.fileExists(KsmUtils.KSM_PATH)) {
            mKsm.setOnPreferenceClickListener(this);
        } else {
            category.removePreference(mKsm);
        }

        mUksm = (CustomPreference) findPreference("uksm");
        if (Utils.fileExists(UksmUtils.UKSM_PATH)) {
            mUksm.setOnPreferenceClickListener(this);
        } else {
            category.removePreference(mUksm);
        }

        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Power Saving
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("powersaving");
        mPowerEfficientWork = (AwesomeTogglePreference) findPreference("power_efficient_work");
        if (mPowerEfficientWork.isSupported()) {
            mPowerEfficientWork.initValue();
            mPowerEfficientWork.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mPowerEfficientWork);
        }

        mMcPowerScheduler = (AwesomeListPreference) findPreference("sched_mc_power_savings");
        if (mMcPowerScheduler.isSupported()) {
            mMcPowerScheduler.initValue();
            mMcPowerScheduler.setSummary(mMcPowerScheduler.getEntry());
            mMcPowerScheduler.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mMcPowerScheduler);
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Voltage
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("voltage");
        mMsmDcvs = (AwesomeTogglePreference) findPreference("msm_dcvs");
        if (mMsmDcvs.isSupported()) {
            mMsmDcvs.initValue();
            mMsmDcvs.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mMsmDcvs);
        }

        mVoltageControl = (CustomPreference) findPreference("voltage_control");
        if (Utils.fileExists(VoltageUtils.VDD_TABLE_FILE) || Utils.fileExists(
                VoltageUtils.UV_TABLE_FILE)) {
            mVoltageControl.setOnPreferenceClickListener(this);
        } else {
            category.removePreference(mVoltageControl);
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
        final int id;
        if (mVoltageControl == preference) {
            id = DeviceConstants.ID_VOLTAGE;
        } else if (mKsm == preference) {
            id = DeviceConstants.ID_KSM;
        } else if (mUksm == preference) {
            id = DeviceConstants.ID_UKSM;
        } else if (mEntropy == preference) {
            id = DeviceConstants.ID_ENTROPY;
        } else {
            id = Integer.MIN_VALUE;
        }

        if (id != Integer.MIN_VALUE) {
            final Activity activity = getActivity();
            if (activity instanceof ActivityCallbacks) {
                ((ActivityCallbacks) activity).shouldLoadFragment(id);
            }
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
                        String.valueOf(mMcPowerScheduler.getEntries()[Utils.parseInt(value)]);
                mMcPowerScheduler.setSummary(summary);
            }
            return true;
        } else if (preference == mMsmDcvs) {
            mMsmDcvs.writeValue((Boolean) o);
            return true;
        } else if (preference == mTcpCongestion) {
            final String value = String.valueOf(o);
            Utils.writeValue(TCP_CONGESTION_CONTROL, value);
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_EXTRAS,
                    mTcpCongestion.getKey(), TCP_CONGESTION_CONTROL, value, true));
            preference.setSummary(value);
            return true;
        }

        return false;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore(BootupConfig config) {
        final ArrayList<BootupItem> items = config
                .getItemsByCategory(BootupConfig.CATEGORY_EXTRAS);
        if (items.size() == 0) {
            return "";
        }

        final StringBuilder sbCmd = new StringBuilder();
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            if (MpDecisionAction.MPDECISION_PATH.equals(item.name)) {
                new MpDecisionAction(item.value, false).triggerAction();
            } else {
                sbCmd.append(Utils.getWriteCommand(item.name, item.value));
            }
        }

        return sbCmd.toString();
    }

}
