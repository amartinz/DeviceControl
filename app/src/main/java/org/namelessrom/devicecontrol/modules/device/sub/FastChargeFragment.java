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
package org.namelessrom.devicecontrol.modules.device.sub;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

public class FastChargeFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String FC_BASE = "/sys/kernel/fast_charge";
    private static final String FC_FORCE = FC_BASE + "/force_fast_charge";
    private static final String FC_AC_LEVELS = FC_BASE + "/ac_levels";
    private static final String FC_AC_LEVEL = FC_BASE + "/ac_charge_level";
    private static final String FC_USB_LEVELS = FC_BASE + "/usb_levels";
    private static final String FC_USB_LEVEL = FC_BASE + "/usb_charge_level";
    private static final String FC_VERSION = FC_BASE + "/version";
    //----------------------------------------------------------------------------------------------

    private boolean isNewVersion = false;

    private CustomListPreference mForceFastCharge;
    private AwesomeTogglePreference mFailsafe;
    private CustomPreference mAcLevelsValid;
    private CustomEditTextPreference mAcLevel;
    private CustomPreference mUsbLevelsValid;
    private CustomEditTextPreference mUsbLevel;

    @Override protected int getFragmentId() { return DeviceConstants.ID_FAST_CHARGE; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_extras_fast_charge);

        final PreferenceScreen mRoot = getPreferenceScreen();
        String tmp;

        final CustomPreference mVersion = (CustomPreference) findPreference("version");
        if (mVersion != null) {
            if (Utils.fileExists(FC_VERSION)) {
                tmp = Utils.readOneLine(FC_VERSION);
                mVersion.setSummary(tmp);
                isNewVersion = tmp.toLowerCase().contains("by paul reioux");
            } else {
                isNewVersion = false;
                mRoot.removePreference(mVersion);
            }
        }

        mForceFastCharge = (CustomListPreference) findPreference("force_fast_charge");
        if (mForceFastCharge != null) {
            if (Utils.fileExists(FC_FORCE)) {
                final String[] values = isNewVersion
                        ? new String[]{ "0", "1", "2" } : new String[]{ "0", "1" };
                mForceFastCharge.setEntries(values);
                mForceFastCharge.setEntryValues(values);
                mForceFastCharge.setValue(Utils.readOneLine(FC_FORCE));
                mForceFastCharge.setSummary(getForceSummary(mForceFastCharge.getValue()));
                mForceFastCharge.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mForceFastCharge);
            }
        }

        mFailsafe = (AwesomeTogglePreference) findPreference("failsafe");
        if (mFailsafe != null) {
            if (isNewVersion && mFailsafe.isSupported()) {
                mFailsafe.initValue();
                mFailsafe.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mFailsafe);
            }
        }

        mAcLevelsValid = (CustomPreference) findPreference("ac_levels_valid");
        if (mAcLevelsValid != null) {
            if (isNewVersion && Utils.fileExists(FC_AC_LEVELS)) {
                if (mFailsafe != null && mFailsafe.isChecked()) {
                    tmp = Utils.readOneLine(FC_AC_LEVELS);
                    mAcLevelsValid.setSummary(tmp);
                } else {
                    mAcLevelsValid.setSummary(R.string.any_level_valid);
                }
            } else {
                mRoot.removePreference(mAcLevelsValid);
            }
        }

        mAcLevel = (CustomEditTextPreference) findPreference("ac_level");
        if (mAcLevel != null) {
            if (isNewVersion && Utils.fileExists(FC_AC_LEVEL)) {
                tmp = Utils.readOneLine(FC_AC_LEVEL);
                mAcLevel.setText(tmp);
                mAcLevel.setSummary(tmp);
                mAcLevel.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mAcLevel);
            }
        }

        mUsbLevelsValid = (CustomPreference) findPreference("usb_levels_valid");
        if (mUsbLevelsValid != null) {
            if (isNewVersion && Utils.fileExists(FC_USB_LEVELS)) {
                if (mFailsafe != null && mFailsafe.isChecked()) {
                    tmp = Utils.readOneLine(FC_USB_LEVELS);
                    mUsbLevelsValid.setSummary(tmp);
                } else {
                    mUsbLevelsValid.setSummary(R.string.any_level_valid);
                }
            } else {
                mRoot.removePreference(mUsbLevelsValid);
            }
        }

        mUsbLevel = (CustomEditTextPreference) findPreference("usb_level");
        if (mUsbLevel != null) {
            if (isNewVersion && Utils.fileExists(FC_USB_LEVEL)) {
                tmp = Utils.readOneLine(FC_USB_LEVEL);
                mUsbLevel.setText(tmp);
                mUsbLevel.setSummary(tmp);
                mUsbLevel.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mUsbLevel);
            }
        }

        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mForceFastCharge == preference) {
            final String value = String.valueOf(newValue);
            Utils.writeValue(FC_FORCE, value);
            mForceFastCharge.setSummary(getForceSummary(Utils.readOneLine(FC_FORCE)));
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, "force_fast_charge", FC_FORCE, value, true));
            return true;
        } else if (mFailsafe == preference) {
            final boolean value = (Boolean) newValue;
            mFailsafe.writeValue(value);
            if (mAcLevelsValid != null) {
                if (!value) {
                    mAcLevelsValid.setSummary(R.string.any_level_valid);
                } else {
                    mAcLevelsValid.setSummary(Utils.readOneLine(FC_AC_LEVELS));
                }
            }
            if (mUsbLevelsValid != null) {
                if (!value) {
                    mUsbLevelsValid.setSummary(R.string.any_level_valid);
                } else {
                    mUsbLevelsValid.setSummary(Utils.readOneLine(FC_USB_LEVELS));
                }
            }
            String tmp;
            if (mAcLevel != null) {
                tmp = Utils.readOneLine(FC_AC_LEVEL);
                mAcLevel.setText(tmp);
                mAcLevel.setSummary(tmp);
            }
            if (mUsbLevel != null) {
                tmp = Utils.readOneLine(FC_USB_LEVEL);
                mUsbLevel.setText(tmp);
                mUsbLevel.setSummary(tmp);
            }
            return true;
        } else if (mAcLevel == preference) {
            final String value = String.valueOf(newValue);
            Utils.writeValue(FC_AC_LEVEL, value);
            final String currentValue = Utils.readOneLine(FC_AC_LEVEL);
            mAcLevel.setSummary(currentValue);
            mAcLevel.setText(currentValue);
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, "ac_level", FC_AC_LEVEL, value, true));
            return true;
        } else if (mUsbLevel == preference) {
            final String value = String.valueOf(newValue);
            Utils.writeValue(FC_USB_LEVEL, value);
            final String currentValue = Utils.readOneLine(FC_USB_LEVEL);
            mUsbLevel.setSummary(currentValue);
            mUsbLevel.setText(currentValue);
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, "usb_level", FC_USB_LEVEL, value, true));
            return true;
        }

        return false;
    }

    private int getForceSummary(final String value) {
        if (value == null || value.isEmpty()) return R.string.unknown;

        switch (value) {
            case "0":
                return R.string.fast_charge_0;
            case "1":
                if (isNewVersion) {
                    return R.string.fast_charge_1_new;
                } else {
                    return R.string.fast_charge_1_old;
                }
            case "2":
                return R.string.fast_charge_2;
            default:
                return R.string.unknown;
        }
    }

}
