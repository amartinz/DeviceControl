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
import org.namelessrom.devicecontrol.utils.DialogHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

public class KsmFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen         mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomPreference         mFullScans;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mEnable;
    private CustomPreference         mPagesToScan;
    private CustomPreference         mSleep;

    @Override
    public void onAttach(final Activity activity) { super.onAttach(activity, ID_KSM); }

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
        addPreferencesFromResource(R.xml.extras_ksm);
        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();
        PreferenceCategory category;
        String tmpString;

        //------------------------------------------------------------------------------------------
        // KSM-Informations
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("ksm_info");
        if (category != null) {
            mFullScans = (CustomPreference) findPreference("ksm_full_scans");
            if (mFullScans != null) {
                if (Utils.fileExists(KSM_FULL_SCANS)) {
                    tmpString = Utils.readOneLine(KSM_FULL_SCANS);
                    mFullScans.setSummary(tmpString);
                } else {
                    category.removePreference(mFullScans);
                }
            }
        }

        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // KSM-Tweakables
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("ksm_settings");
        if (category != null) {
            mEnable = (CustomCheckBoxPreference) findPreference("ksm_run");
            if (mEnable != null) {
                if (Utils.fileExists(KSM_RUN)) {
                    tmpString = Utils.readOneLine(KSM_RUN);
                    mEnable.setChecked(Utils.isEnabled(tmpString));
                    mEnable.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mEnable);
                }
            }

            mPagesToScan = (CustomPreference) findPreference("ksm_pages_to_scan");
            if (mPagesToScan != null) {
                if (Utils.fileExists(KSM_PAGES_TO_SCAN)) {
                    tmpString = Utils.readOneLine(KSM_PAGES_TO_SCAN);
                    mPagesToScan.setSummary(tmpString);
                } else {
                    category.removePreference(mPagesToScan);
                }
            }

            mSleep = (CustomPreference) findPreference("ksm_sleep");
            if (mSleep != null) {
                if (Utils.fileExists(KSM_SLEEP)) {
                    tmpString = Utils.readOneLine(KSM_SLEEP);
                    mSleep.setSummary(tmpString);
                } else {
                    category.removePreference(mSleep);
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
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mPagesToScan == preference) {
            final String title = String.valueOf(mPagesToScan.getTitle());
            final int currentProgress = Integer.parseInt(Utils.readOneLine(KSM_PAGES_TO_SCAN));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 1,
                    1024, preference, KSM_PAGES_TO_SCAN, DatabaseHandler.CATEGORY_EXTRAS);
            return true;
        } else if (mSleep == preference) {
            final String title = String.valueOf(mSleep.getTitle());
            final int currentProgress = Integer.parseInt(Utils.readOneLine(KSM_SLEEP));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 50,
                    5000, preference, KSM_SLEEP, DatabaseHandler.CATEGORY_EXTRAS);
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mEnable == preference) {
            preferenceChange(mEnable, (Boolean) o, KSM_RUN);
            return true;
        }

        return false;
    }

    private void preferenceChange(final CustomCheckBoxPreference pref, final boolean rawValue,
            final String file) {
        final String value = rawValue ? "1" : "0";
        Utils.writeValue(file, value);
        PreferenceHelper.setBootup(
                new DataItem(DatabaseHandler.CATEGORY_EXTRAS, pref.getKey(), file, value));
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


