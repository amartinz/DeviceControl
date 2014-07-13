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
package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class HotpluggingFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private static final int ID_MPDECISION = 200;
    //----------------------------------------------------------------------------------------------
    private PreferenceScreen          mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference  mMpDecision;
    private AwesomeCheckBoxPreference mIntelliPlug;
    private AwesomeCheckBoxPreference mIntelliPlugEco;
    private CustomListPreference      mCpuQuietGov;

    @Override
    public void onAttach(final Activity activity) { super.onAttach(activity, ID_HOTPLUGGING); }

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
        addPreferencesFromResource(R.xml.extras_hotplugging);
        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mMpDecision = (CustomCheckBoxPreference) findPreference("mpdecision");
        if (mMpDecision != null) {
            if (Utils.fileExists(MPDECISION_PATH)) {
                Utils.getCommandResult(ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
            } else {
                mRoot.removePreference(mMpDecision);
            }
        }

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference("intelli_plug");
        if (category != null) {
            mIntelliPlug = (AwesomeCheckBoxPreference) findPreference("intelli_plug_active");
            if (mIntelliPlug != null) {
                if (mIntelliPlug.isSupported()) {
                    mIntelliPlug.initValue();
                    mIntelliPlug.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlug);
                }
            }

            mIntelliPlugEco = (AwesomeCheckBoxPreference) findPreference("intelli_plug_eco");
            if (mIntelliPlugEco != null) {
                if (mIntelliPlugEco.isSupported()) {
                    mIntelliPlugEco.initValue();
                    mIntelliPlugEco.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlugEco);
                }
            }
        }
        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // CPUquiet
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("cpu_quiet");
        if (category != null) {
            if (Utils.fileExists(Application.getStr(R.string.file_cpu_quiet_base))) {
                if (Utils.fileExists(Application.getStr(R.string.file_cpu_quiet_avail_gov))
                        && Utils.fileExists(Application.getStr(R.string.file_cpu_quiet_cur_gov))) {
                    final String[] govs = Utils.readOneLine(
                            Application.getStr(R.string.file_cpu_quiet_avail_gov)).split(" ");
                    final String gov = Utils.readOneLine(
                            Application.getStr(R.string.file_cpu_quiet_cur_gov));
                    mCpuQuietGov = new CustomListPreference(getActivity());
                    mCpuQuietGov.setKey("pref_cpu_quiet_governor");
                    mCpuQuietGov.setTitle(R.string.governor);
                    mCpuQuietGov.setEntries(govs);
                    mCpuQuietGov.setEntryValues(govs);
                    mCpuQuietGov.setValue(gov);
                    mCpuQuietGov.setSummary(gov);
                    mCpuQuietGov.setOnPreferenceChangeListener(this);
                    category.addPreference(mCpuQuietGov);
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
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mMpDecision) {
            final boolean value = (Boolean) o;
            Utils.runRootCommand(CpuUtils.enableMpDecision(value));
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mMpDecision.getKey(),
                    MPDECISION_PATH, value ? "1" : "0"));
            changed = true;
        } else if (preference == mIntelliPlug) {
            mIntelliPlug.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mIntelliPlugEco) {
            mIntelliPlugEco.writeValue((Boolean) o);
            changed = true;
        } else if (preference == mCpuQuietGov) {
            final String path = Application.getStr(R.string.file_cpu_quiet_cur_gov);
            final String value = String.valueOf(o);
            Utils.runRootCommand(Utils.getWriteCommand(path, value));
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mCpuQuietGov.getKey(),
                    path, value));
            mCpuQuietGov.setSummary(value);
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
                case ID_MPDECISION:
                    if (mMpDecision != null) {
                        mMpDecision.setChecked(!result.isEmpty());
                        mMpDecision.setOnPreferenceChangeListener(this);
                    }
                    break;
                default:
                    break;
            }
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


