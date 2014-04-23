package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class ThermalFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen         mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mIntelliThermalCcEnabled;
    private CustomCheckBoxPreference mIntelliThermalEnabled;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_THERMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_thermal);
        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();
        PreferenceCategory category;
        String tmpString;

        //------------------------------------------------------------------------------------------
        // Intelli-Thermal
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("intelli_thermal");
        if (category != null) {
            mIntelliThermalCcEnabled = (CustomCheckBoxPreference)
                    findPreference("intelli_thermal_cc_enabled");
            if (mIntelliThermalCcEnabled != null) {
                if (Utils.fileExists(INTELLI_THERMAL_CC_ENABLED)) {
                    tmpString = Utils.readOneLine(INTELLI_THERMAL_CC_ENABLED);
                    mIntelliThermalCcEnabled.setChecked(Utils.isEnabled(tmpString));
                    mIntelliThermalCcEnabled.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliThermalCcEnabled);
                }
            }

            mIntelliThermalEnabled = (CustomCheckBoxPreference)
                    findPreference("intelli_thermal_enabled");
            if (mIntelliThermalEnabled != null) {
                if (Utils.fileExists(INTELLI_THERMAL_ENABLED)) {
                    tmpString = Utils.readOneLine(INTELLI_THERMAL_ENABLED);
                    mIntelliThermalEnabled.setChecked(Utils.isEnabled(tmpString));
                    mIntelliThermalEnabled.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliThermalEnabled);
                }
            }
        }

        removeIfEmpty(category);

        isSupported(mRoot, getActivity());
    }

    private void removeIfEmpty(final PreferenceCategory preferenceCategory) {
        if (mRoot != null && preferenceCategory.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceCategory);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mIntelliThermalCcEnabled) {
            final boolean rawValue = (Boolean) o;
            final String value = rawValue ? "1" : "0";
            Utils.writeValue(INTELLI_THERMAL_CC_ENABLED, value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mIntelliThermalCcEnabled.getKey(),
                    INTELLI_THERMAL_CC_ENABLED, value));
            changed = true;
        } else if (preference == mIntelliThermalEnabled) {
            final boolean rawValue = (Boolean) o;
            final String value = rawValue ? "1" : "0";
            Utils.writeValue(INTELLI_THERMAL_ENABLED, value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mIntelliThermalEnabled.getKey(),
                    INTELLI_THERMAL_ENABLED, value));
            changed = true;
        }

        return changed;
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


