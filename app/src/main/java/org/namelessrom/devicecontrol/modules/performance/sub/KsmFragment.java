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
import org.namelessrom.devicecontrol.hardware.KsmUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.DialogHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class KsmFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private PreferenceScreen mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomPreference mFullScans;
    private CustomPreference mPagesShared;
    private CustomPreference mPagesSharing;
    private CustomPreference mPagesUnshared;
    private CustomPreference mPagesVolatile;
    //----------------------------------------------------------------------------------------------
    private AwesomeTogglePreference mEnable;
    private AwesomeTogglePreference mDefer;
    private CustomPreference mPagesToScan;
    private CustomPreference mSleep;

    @Override protected int getFragmentId() { return DeviceConstants.ID_KSM; }

    @Override public void onCreate(final Bundle savedInstanceState) {
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
                if (!Utils.fileExists(KsmUtils.KSM_FULL_SCANS)) {
                    category.removePreference(mFullScans);
                }
            }

            mPagesShared = (CustomPreference) findPreference("ksm_pages_shared");
            if (mPagesShared != null) {
                if (!Utils.fileExists(KsmUtils.KSM_PAGES_SHARED)) {
                    category.removePreference(mPagesShared);
                }
            }

            mPagesSharing = (CustomPreference) findPreference("ksm_pages_sharing");
            if (mPagesSharing != null) {
                if (!Utils.fileExists(KsmUtils.KSM_PAGES_SHARING)) {
                    category.removePreference(mPagesSharing);
                }
            }

            mPagesUnshared = (CustomPreference) findPreference("ksm_pages_unshared");
            if (mPagesUnshared != null) {
                if (!Utils.fileExists(KsmUtils.KSM_PAGES_UNSHARED)) {
                    category.removePreference(mPagesUnshared);
                }
            }

            mPagesVolatile = (CustomPreference) findPreference("ksm_pages_volatile");
            if (mPagesVolatile != null) {
                if (!Utils.fileExists(KsmUtils.KSM_PAGES_VOLATILE)) {
                    category.removePreference(mPagesVolatile);
                }
            }

            new RefreshTask().execute();
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // KSM-Tweakables
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("ksm_settings");
        if (category != null) {
            mEnable = (AwesomeTogglePreference) findPreference("ksm_run");
            if (mEnable != null) {
                if (mEnable.isSupported()) {
                    mEnable.initValue();
                    mEnable.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mEnable);
                }
            }

            mDefer = (AwesomeTogglePreference) findPreference("ksm_deferred");
            if (mDefer != null) {
                if (mDefer.isSupported()) {
                    mDefer.initValue();
                    mDefer.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mDefer);
                }
            }

            mPagesToScan = (CustomPreference) findPreference("ksm_pages_to_scan");
            if (mPagesToScan != null) {
                if (Utils.fileExists(KsmUtils.KSM_PAGES_TO_SCAN)) {
                    tmpString = Utils.readOneLine(KsmUtils.KSM_PAGES_TO_SCAN);
                    mPagesToScan.setSummary(tmpString);
                } else {
                    category.removePreference(mPagesToScan);
                }
            }

            mSleep = (CustomPreference) findPreference("ksm_sleep");
            if (mSleep != null) {
                if (Utils.fileExists(KsmUtils.KSM_SLEEP)) {
                    tmpString = Utils.readOneLine(KsmUtils.KSM_SLEEP);
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

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (mPagesToScan == preference) {
            final String title = String.valueOf(mPagesToScan.getTitle());
            final int currentProgress =
                    Utils.parseInt(Utils.readOneLine(KsmUtils.KSM_PAGES_TO_SCAN));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 1,
                    1024, preference, KsmUtils.KSM_PAGES_TO_SCAN, BootupConfig.CATEGORY_EXTRAS);
            return true;
        } else if (mSleep == preference) {
            final String title = String.valueOf(mSleep.getTitle());
            final int currentProgress = Utils.parseInt(Utils.readOneLine(KsmUtils.KSM_SLEEP));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 50,
                    5000, preference, KsmUtils.KSM_SLEEP, BootupConfig.CATEGORY_EXTRAS);
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mEnable == preference) {
            mEnable.writeValue((Boolean) o);
            return true;
        } else if (mDefer == preference) {
            mDefer.writeValue((Boolean) o);
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

            list.add(Utils.readOneLine(KsmUtils.KSM_FULL_SCANS));     // 0
            list.add(Utils.readOneLine(KsmUtils.KSM_PAGES_SHARED));   // 1
            list.add(Utils.readOneLine(KsmUtils.KSM_PAGES_SHARING));  // 2
            list.add(Utils.readOneLine(KsmUtils.KSM_PAGES_UNSHARED)); // 3
            list.add(Utils.readOneLine(KsmUtils.KSM_PAGES_VOLATILE)); // 4

            return list;
        }

        @Override protected void onPostExecute(final List<String> strings) {
            if (isAdded()) {
                String tmp;
                if (mFullScans != null) {
                    tmp = strings.get(0);
                    mFullScans.setSummary(tmp);
                }
                if (mPagesShared != null) {
                    tmp = strings.get(1);
                    mPagesShared.setSummary(tmp);
                }
                if (mPagesSharing != null) {
                    tmp = strings.get(2);
                    mPagesSharing.setSummary(tmp);
                }
                if (mPagesUnshared != null) {
                    tmp = strings.get(3);
                    mPagesUnshared.setSummary(tmp);
                }
                if (mPagesVolatile != null) {
                    tmp = strings.get(4);
                    mPagesVolatile.setSummary(tmp);
                }
            }
        }
    }
}


