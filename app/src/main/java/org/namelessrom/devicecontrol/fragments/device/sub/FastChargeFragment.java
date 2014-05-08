package org.namelessrom.devicecontrol.fragments.device.sub;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

/**
 * Created by alex on 08.05.14.
 */
public class FastChargeFragment extends AttachPreferenceFragment implements DeviceConstants,
        Preference.OnPreferenceChangeListener {

    private static final String FC_BASE       = "/sys/kernel/fast_charge";
    private static final String FC_FORCE      = FC_BASE + "/force_fast_charge";
    private static final String FC_FAILSAFE   = FC_BASE + "/failsafe";
    private static final String FC_AC_LEVELS  = FC_BASE + "/ac_levels";
    private static final String FC_AC_LEVEL   = FC_BASE + "/ac_charge_level";
    private static final String FC_USB_LEVELS = FC_BASE + "/usb_levels";
    private static final String FC_USB_LEVEL  = FC_BASE + "/usb_charge_level";
    private static final String FC_VERSION    = FC_BASE + "/version";
    //----------------------------------------------------------------------------------------------

    private boolean isNewVersion = false;

    private PreferenceScreen mRoot;

    private CustomListPreference     mForceFastCharge;
    private CustomCheckBoxPreference mFailsafe;
    private CustomPreference         mAcLevelsValid;
    private CustomEditTextPreference mAcLevel;
    private CustomPreference         mUsbLevelsValid;
    private CustomEditTextPreference mUsbLevel;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_FAST_CHARGE);
    }

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
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fast_charge);

        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();
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
                        ? new String[]{"0", "1", "2"} : new String[]{"0", "1"};
                mForceFastCharge.setEntries(values);
                mForceFastCharge.setEntryValues(values);
                mForceFastCharge.setValue(Utils.readOneLine(FC_FORCE));
                mForceFastCharge.setSummary(getForceSummary(mForceFastCharge.getValue()));
                mForceFastCharge.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mForceFastCharge);
            }
        }

        mFailsafe = (CustomCheckBoxPreference) findPreference("failsafe");
        if (mFailsafe != null) {
            if (isNewVersion && Utils.fileExists(FC_FAILSAFE)) {
                tmp = Utils.readOneLine(FC_FAILSAFE);
                mFailsafe.setChecked(tmp != null && tmp.equals("1"));
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
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_FEATURES, "force_fast_charge", FC_FORCE, value));
            return true;
        } else if (mFailsafe == preference) {
            final boolean value = (Boolean) newValue;
            Utils.writeValue(FC_FAILSAFE, (value ? "1" : "0"));
            mFailsafe.setChecked(value);
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
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_FEATURES, "failsafe", FC_FAILSAFE, value ? "1" : "0"));
            return true;
        } else if (mAcLevel == preference) {
            final String value = String.valueOf(newValue);
            Utils.writeValue(FC_AC_LEVEL, value);
            final String currentValue = Utils.readOneLine(FC_AC_LEVEL);
            mAcLevel.setSummary(currentValue);
            mAcLevel.setText(currentValue);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_FEATURES, "ac_level", FC_AC_LEVEL, value));
            return true;
        } else if (mUsbLevel == preference) {
            final String value = String.valueOf(newValue);
            Utils.writeValue(FC_USB_LEVEL, value);
            final String currentValue = Utils.readOneLine(FC_USB_LEVEL);
            mUsbLevel.setSummary(currentValue);
            mUsbLevel.setText(currentValue);
            PreferenceHelper.setBootup(new DataItem
                    (DatabaseHandler.CATEGORY_FEATURES, "usb_level", FC_USB_LEVEL, value));
            return true;
        }

        return false;
    }

    private int getForceSummary(final String value) {
        if (value == null || value.isEmpty()) return R.string.unknown;

        if ("0".equals(value)) {
            return R.string.fast_charge_0;
        } else if ("1".equals(value)) {
            if (isNewVersion) {
                return R.string.fast_charge_1_new;
            } else {
                return R.string.fast_charge_1_old;
            }
        } else if ("2".equals(value)) {
            return R.string.fast_charge_2;
        } else {
            return R.string.unknown;
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            default:
                break;
        }

        return false;
    }
}
