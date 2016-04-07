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
package org.namelessrom.devicecontrol.modules.performance.sub;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.hardware.UksmUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.DialogHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

/*
abundant_threshold
cpu_governor                [full] medium low quiet
cpu_ratios                  20 40 MAX/4 MAX/1
ema_per_page_time
eval_intervals
full_scans
hash_strength
max_cpu_percentage
pages_scanned
pages_shared
pages_sharing
pages_unshared
run
sleep_millisecs
sleep_times
thrash_threshold
 */

public class UksmFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomPreference mPagesShared;
    private CustomPreference mPagesScanned;
    private CustomPreference mFullScans;
    private CustomPreference mHashStrength;
    private CustomPreference mPagesSharing;
    private CustomPreference mSleepTimes;
    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mEnable;
    private CustomPreference mSleep;
    private CustomListPreference mCpuGovernor;

    @Override protected int getFragmentId() { return DeviceConstants.ID_UKSM; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_uksm);
        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();
        PreferenceCategory category;
        String tmpString;

        //------------------------------------------------------------------------------------------
        // UKSM-Informations
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("uksm_info");
        if (category != null) {
            mPagesShared = (CustomPreference) findPreference("uksm_pages_shared");
            if (mPagesShared != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_PAGES_SHARED)) {
                    category.removePreference(mPagesShared);
                }
            }
            mPagesScanned = (CustomPreference) findPreference("uksm_pages_scanned");
            if (mPagesScanned != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_PAGES_SCANNED)) {
                    category.removePreference(mPagesScanned);
                }
            }
            mFullScans = (CustomPreference) findPreference("uksm_full_scans");
            if (mFullScans != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_FULL_SCANS)) {
                    category.removePreference(mFullScans);
                }
            }
            mHashStrength = (CustomPreference) findPreference("uksm_hash_strength");
            if (mHashStrength != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_HASH_STRENGTH)) {
                    category.removePreference(mHashStrength);
                }
            }
            mPagesSharing = (CustomPreference) findPreference("uksm_pages_sharing");
            if (mPagesSharing != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_PAGES_SHARING)) {
                    category.removePreference(mPagesSharing);
                }
            }
            mSleepTimes = (CustomPreference) findPreference("uksm_sleep_times");
            if (mSleepTimes != null) {
                if (!Utils.fileExists(UksmUtils.UKSM_SLEEP_TIMES)) {
                    category.removePreference(mSleepTimes);
                }
            }
            new RefreshTask().execute();
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // UKSM-Tweakables
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("uksm_settings");
        if (category != null) {
            mEnable = (AwesomeTogglePreference) findPreference("uksm_run");
            if (mEnable != null) {
                if (mEnable.isSupported()) {
                    mEnable.initValue();
                    mEnable.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mEnable);
                }
            }

            mSleep = (CustomPreference) findPreference("uksm_sleep");
            if (mSleep != null) {
                if (Utils.fileExists(UksmUtils.UKSM_SLEEP)) {
                    tmpString = Utils.readOneLine(UksmUtils.UKSM_SLEEP);
                    mSleep.setSummary(tmpString);
                } else {
                    category.removePreference(mSleep);
                }
            }
            mCpuGovernor = (CustomListPreference) findPreference("uksm_governor");
            if (mCpuGovernor != null) {
                if (Utils.fileExists(UksmUtils.UKSM_CPU_GOV)) {
                    final String[] uksm_cpu_governors = UksmUtils.get().getAvailableCpuGovernors();
                    final String uksm_cpu_governor = UksmUtils.get().getCurrentCpuGovernor();
                    mCpuGovernor.setEntries(uksm_cpu_governors);
                    mCpuGovernor.setEntryValues(uksm_cpu_governors);
                    mCpuGovernor.setSummary(uksm_cpu_governor);
                    mCpuGovernor.setValue(uksm_cpu_governor);
                    mCpuGovernor.setOnPreferenceChangeListener(this);
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

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (mSleep == preference) {
            final String title = String.valueOf(mSleep.getTitle());
            final int currentProgress = Utils.parseInt(Utils.readOneLine(UksmUtils.UKSM_SLEEP));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 50,
                    1000, preference, UksmUtils.UKSM_SLEEP, BootupConfig.CATEGORY_EXTRAS);
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mEnable == preference) {
            mEnable.writeValue((Boolean) o);
            return true;
        } else if (mCpuGovernor == preference) {
            final String value = String.valueOf(o);
            mCpuGovernor.setSummary(value);
            ActionProcessor.processAction(ActionProcessor.ACTION_UKSM_GOVERNOR, value, true);
            return true;
        }

        return false;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_refresh:
                new RefreshTask().execute();
                break;
        }

        return false;
    }

    private class RefreshTask extends AsyncTask<Void, Void, List<String>> {

        @Override protected List<String> doInBackground(Void... params) {
            final ArrayList<String> list = new ArrayList<>();

            list.add(Utils.readOneLine(UksmUtils.UKSM_PAGES_SHARED));   // 0
            list.add(Utils.readOneLine(UksmUtils.UKSM_PAGES_SCANNED));  // 1
            list.add(Utils.readOneLine(UksmUtils.UKSM_FULL_SCANS));     // 2
            list.add(Utils.readOneLine(UksmUtils.UKSM_HASH_STRENGTH));  // 3
            list.add(Utils.readOneLine(UksmUtils.UKSM_PAGES_SHARING));  // 4
            list.add(Utils.readOneLine(UksmUtils.UKSM_SLEEP_TIMES));    // 5

            return list;
        }

        @Override protected void onPostExecute(final List<String> strings) {
            if (isAdded()) {
                String tmp;
                if (mPagesShared != null) {
                    tmp = strings.get(0);
                    mPagesShared.setSummary(tmp);
                }
                if (mPagesScanned != null) {
                    tmp = strings.get(1);
                    mPagesScanned.setSummary(tmp);
                }
                if (mFullScans != null) {
                    tmp = strings.get(2);
                    mFullScans.setSummary(tmp);
                }
                if (mHashStrength != null) {
                    tmp = strings.get(3);
                    mHashStrength.setSummary(tmp);
                }
                if (mPagesSharing != null) {
                    tmp = strings.get(4);
                    mPagesSharing.setSummary(tmp);
                }
                if (mSleepTimes != null) {
                    tmp = strings.get(5);
                    mSleepTimes.setSummary(tmp);
                }
            }
        }
    }
}


