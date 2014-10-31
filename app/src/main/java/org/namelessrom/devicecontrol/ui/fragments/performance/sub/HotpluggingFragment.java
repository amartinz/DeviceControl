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
package org.namelessrom.devicecontrol.ui.fragments.performance.sub;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.objects.ShellOutput;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomePreferenceCategory;
import org.namelessrom.devicecontrol.ui.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class HotpluggingFragment extends AttachPreferenceFragment
        implements DeviceConstants,
        Preference.OnPreferenceChangeListener, ShellOutput.OnShellOutputListener {

    //----------------------------------------------------------------------------------------------
    private static final int ID_MPDECISION = 200;
    //----------------------------------------------------------------------------------------------
    private PreferenceScreen mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mMpDecision;
    private CustomListPreference mCpuQuietGov;

    @Override protected int getFragmentId() { return ID_HOTPLUGGING; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_hotplugging);

        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mMpDecision = (CustomCheckBoxPreference) findPreference("mpdecision");
        if (mMpDecision != null) {
            if (Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
                Utils.getCommandResult(this, ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
            } else {
                mRoot.removePreference(mMpDecision);
            }
        }

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference("intelli_plug");
        if (category != null) {
            final AwesomeCheckBoxPreference intelliPlug =
                    (AwesomeCheckBoxPreference) findPreference("intelli_plug_active");
            if (intelliPlug != null) {
                if (intelliPlug.isSupported()) {
                    intelliPlug.initValue();
                    intelliPlug.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(intelliPlug);
                }
            }

            final AwesomeCheckBoxPreference intelliPlugEco =
                    (AwesomeCheckBoxPreference) findPreference("intelli_plug_eco");
            if (intelliPlugEco != null) {
                if (intelliPlugEco.isSupported()) {
                    intelliPlugEco.initValue();
                    intelliPlugEco.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(intelliPlugEco);
                }
            }
        }
        removeIfEmpty(category);

        final AwesomePreferenceCategory makoHotplug =
                (AwesomePreferenceCategory) findPreference("mako_hotplug");
        if (makoHotplug.isSupported()) {
            final String[] files = Utils.listFiles(makoHotplug.getPath(), true);
            AwesomeEditTextPreference preference;
            for (final String file : files) {
                preference = new AwesomeEditTextPreference(getActivity(),
                        makoHotplug.getPath() + file, null, "extras", false, true);
                preference.setKey("mako_" + file);
                if (preference.isSupported()) {
                    makoHotplug.addPreference(preference);
                    preference.setTitle(Utils.getFileName(makoHotplug.getPath() + file));
                    preference.initValue();
                    preference.setOnPreferenceChangeListener(this);
                }
            }
        } else {
            getPreferenceScreen().removePreference(makoHotplug);
        }

        //------------------------------------------------------------------------------------------
        // CPUquiet
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("cpu_quiet");
        if (category != null) {
            if (Utils.fileExists(Application.get().getString(R.string.file_cpu_quiet_base))) {
                if (Utils.fileExists(Application.get().getString(R.string.file_cpu_quiet_avail_gov))
                        && Utils.fileExists(Application.get().getString(
                        R.string.file_cpu_quiet_cur_gov))) {
                    final String[] govs = Utils.readOneLine(
                            Application.get().getString(R.string.file_cpu_quiet_avail_gov))
                            .split(" ");
                    final String gov = Utils.readOneLine(
                            Application.get().getString(R.string.file_cpu_quiet_cur_gov));
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

    @Override public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference instanceof AwesomeCheckBoxPreference) {
            ((AwesomeCheckBoxPreference) preference).writeValue((Boolean) o);
            return true;
        } else if (preference instanceof AwesomeEditTextPreference) {
            ((AwesomeEditTextPreference) preference).writeValue(String.valueOf(o));
            return true;
        } else if (preference == mMpDecision) {
            final boolean value = (Boolean) o;
            new MpDecisionAction(value ? "1" : "0", true).triggerAction();
            return true;
        } else if (preference == mCpuQuietGov) {
            final String path = Application.get().getString(R.string.file_cpu_quiet_cur_gov);
            final String value = String.valueOf(o);
            Utils.runRootCommand(Utils.getWriteCommand(path, value));
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mCpuQuietGov.getKey(),
                    path, value));
            mCpuQuietGov.setSummary(value);
            return true;
        }

        return false;
    }

    public void onShellOutput(final ShellOutput shellOutput) {
        if (shellOutput != null) {
            switch (shellOutput.id) {
                case ID_MPDECISION:
                    if (mMpDecision != null) {
                        mMpDecision.setChecked(!TextUtils.isEmpty(shellOutput.output));
                        mMpDecision.setOnPreferenceChangeListener(this);
                    }
                    break;
                default:
                    break;
            }
        }
    }

}


