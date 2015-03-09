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
package org.namelessrom.devicecontrol.ui.fragments.performance;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.monitors.CpuCoreMonitor;
import org.namelessrom.devicecontrol.objects.CpuCore;
import org.namelessrom.devicecontrol.objects.ShellOutput;
import org.namelessrom.devicecontrol.ui.preferences.AwesomePreferenceCategory;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.ui.views.CpuCoreView;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CpuSettingsFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener, ShellOutput.OnShellOutputListener,
        CpuUtils.CoreListener, CpuUtils.FrequencyListener, GovernorUtils.GovernorListener {

    private CustomListPreference mMax;
    private CustomListPreference mMin;
    private CustomTogglePreference mCpuLock;

    private CustomListPreference mGovernor;
    private CustomPreference mGovernorTuning;

    private CustomTogglePreference mMpDecision;
    private CustomListPreference mCpuQuietGov;

    private static final int ID_MPDECISION = 200;
    //----------------------------------------------------------------------------------------------

    private SwitchCompat mStatusHide;
    private LinearLayout mCpuInfo;

    @Override protected int getFragmentId() { return DeviceConstants.ID_PERFORMANCE_CPU_SETTINGS; }

    @Override public void onResume() {
        super.onResume();
        if (mStatusHide != null && mStatusHide.isChecked()) {
            CpuCoreMonitor.getInstance(getActivity()).start(this, 1000);
        }
        CpuUtils.get().getCpuFreq(this);
        GovernorUtils.get().getGovernor(this);
    }

    @Override public void onPause() {
        super.onPause();
        CpuCoreMonitor.getInstance(getActivity()).stop();
    }

    @Override public void onCores(@NonNull final CpuUtils.Cores cores) {
        final List<CpuCore> coreList = cores.list;
        if (coreList != null && !coreList.isEmpty()) {
            final int count = coreList.size();
            for (int i = 0; i < count; i++) {
                generateRow(i, coreList.get(i));
            }
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cpu);

        mMax = (CustomListPreference) findPreference("pref_max");
        mMax.setOnPreferenceChangeListener(this);

        mMin = (CustomListPreference) findPreference("pref_min");
        mMin.setOnPreferenceChangeListener(this);

        mCpuLock = (CustomTogglePreference) findPreference(DeviceConfiguration.CPU_LOCK_FREQ);
        mCpuLock.setChecked(DeviceConfiguration.get(getActivity()).perfCpuLock);
        mCpuLock.setOnPreferenceChangeListener(this);

        mGovernor = (CustomListPreference) findPreference("pref_governor");
        mGovernor.setOnPreferenceChangeListener(this);

        mGovernorTuning = (CustomPreference) findPreference("pref_governor_tuning");

        // get hold of hotplugging and remove it, to add it back later if supported
        final PreferenceCategory hotplugging = (PreferenceCategory) findPreference("hotplugging");
        getPreferenceScreen().removePreference(hotplugging);

        if (Utils.fileExists(getString(R.string.directory_intelli_plug))
                || Utils.fileExists(getString(R.string.directory_mako_hotplug))
                || Utils.fileExists(getString(R.string.file_cpu_quiet_base))
                || Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
            getPreferenceScreen().addPreference(hotplugging);
            addPreferencesFromResource(R.xml.cpu_hotplugging);
            setupHotpluggingPreferences();
        }
    }

    private void setupHotpluggingPreferences() {
        AwesomePreferenceCategory awCategory;
        PreferenceCategory category;

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mMpDecision = (CustomTogglePreference) findPreference("mpdecision");
        if (Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
            Utils.getCommandResult(this, ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
        } else {
            getPreferenceScreen().removePreference(mMpDecision);
        }

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------
        awCategory = (AwesomePreferenceCategory) findPreference("intelli_plug");
        if (awCategory.isSupported()) {
            final String path = awCategory.getPath();
            AwesomeTogglePreference togglePref;
            // setup intelli plug toggle
            if (Utils.fileExists(path + "intelli_plug_active")) {
                togglePref = PreferenceUtils.addAwesomeTogglePreference(getActivity(),
                        "intelli_plug_", "", "extras", path, "intelli_plug_active", awCategory,
                        this);
                if (togglePref != null) {
                    togglePref.setupTitle();
                }
            }
            // setup touch boost toggle
            if (Utils.fileExists(path + "touch_boost_active")) {
                togglePref = PreferenceUtils.addAwesomeTogglePreference(
                        getActivity(), "intelli_plug_", "", "extras", path,
                        "touch_boost_active", awCategory, this);
                if (togglePref != null) {
                    togglePref.setupTitle();
                }
            }
            // add the other files
            final String[] files = Utils.listFiles(path, false);
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    PreferenceUtils.addAwesomeEditTextPreference(getActivity(), "intelli_plug_",
                            "extras", path, file, awCategory, this);
                }
            }
        }
        removeIfEmpty(getPreferenceScreen(), awCategory);

        awCategory = (AwesomePreferenceCategory) findPreference("mako_hotplug");
        if (awCategory.isSupported()) {
            final String path = awCategory.getPath();
            // setup mako_hotplug toggle
            if (Utils.fileExists(path + "enabled")) {
                AwesomeTogglePreference togglePref = PreferenceUtils.addAwesomeTogglePreference(
                        getActivity(), "mako_", "", "extras", path, "enabled", awCategory, this);
                if (togglePref != null) {
                    togglePref.setupTitle();
                }
            }
            final String[] files = Utils.listFiles(path, true);
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    PreferenceUtils.addAwesomeEditTextPreference(getActivity(), "mako_",
                            "extras", path, file, awCategory, this);
                }
            }
        }
        removeIfEmpty(getPreferenceScreen(), awCategory);

        //------------------------------------------------------------------------------------------
        // CPUquiet
        //------------------------------------------------------------------------------------------
        category = (PreferenceCategory) findPreference("cpu_quiet");
        if (Utils.fileExists(Application.get().getString(R.string.file_cpu_quiet_base))
                && Utils.fileExists(Application.get().getString(R.string.file_cpu_quiet_avail_gov))
                && Utils.fileExists(Application.get().getString(R.string.file_cpu_quiet_cur_gov))) {
            final String[] govs = Utils.readOneLine(
                    Application.get().getString(R.string.file_cpu_quiet_avail_gov)).split(" ");
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
        removeIfEmpty(getPreferenceScreen(), category);

        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == mMax) {
            final String selected = String.valueOf(o);
            final String other = String.valueOf(mMin.getValue());
            final boolean updateOther = Utils.parseInt(selected) < Utils.parseInt(other);
            if (updateOther) {
                mMin.setValue(selected);
                mMin.setSummary(CpuUtils.toMhz(selected));
            }
            mMax.setValue(selected);
            mMax.setSummary(CpuUtils.toMhz(selected));

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, selected, true);
            return true;
        } else if (preference == mMin) {
            final String selected = String.valueOf(o);
            final String other = String.valueOf(mMax.getValue());
            final boolean updateOther = Utils.parseInt(selected) > Utils.parseInt(other);
            if (updateOther) {
                mMax.setValue(selected);
                mMax.setSummary(CpuUtils.toMhz(selected));
            }
            mMin.setValue(selected);
            mMin.setSummary(CpuUtils.toMhz(selected));

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, selected, true);
            return true;
        } else if (preference == mCpuLock) {
            DeviceConfiguration.get(getActivity()).perfCpuLock = (Boolean) o;
            DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());
            return true;
        } else if (preference == mGovernor) {
            final String selected = String.valueOf(o);
            mGovernor.setValue(selected);
            mGovernor.setSummary(selected);

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_GOVERNOR, selected, true);
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

        return super.onPreferenceChange(preference, o);
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (preference == mGovernorTuning) {
            MainActivity.loadFragment(getActivity(), DeviceConstants.ID_GOVERNOR_TUNABLE);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
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

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedState) {
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_cpu_settings, root, false);

        mCpuInfo = (LinearLayout) view.findViewById(R.id.cpu_info);

        mStatusHide = (SwitchCompat) view.findViewById(R.id.cpu_info_hide);
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(final CompoundButton button, final boolean b) {
                if (b) {
                    mCpuInfo.setVisibility(View.VISIBLE);
                    CpuCoreMonitor.getInstance(getActivity()).start(CpuSettingsFragment.this, 1000);
                } else {
                    CpuCoreMonitor.getInstance(getActivity()).stop();
                    mCpuInfo.setVisibility(View.GONE);
                }
                DeviceConfiguration.get(getActivity()).perfCpuInfo = b;
                DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());
            }
        });
        mStatusHide.setChecked(DeviceConfiguration.get(getActivity()).perfCpuInfo);
        if (mStatusHide.isChecked()) {
            mCpuInfo.setVisibility(View.VISIBLE);
        } else {
            mCpuInfo.setVisibility(View.GONE);
        }

        CpuCore tmpCore;
        final int mCpuNum = CpuUtils.get().getNumOfCpus();
        final String format = getString(R.string.core) + " %s:";
        for (int i = 0; i < mCpuNum; i++) {
            tmpCore = new CpuCore(String.format(format, String.valueOf(i)), "0", "0", "0");
            generateRow(i, tmpCore);
        }

        return view;
    }

    @Override public void onFrequency(@NonNull final CpuUtils.Frequency cpuFreq) {
        final String[] mAvailableFrequencies = cpuFreq.available;
        Arrays.sort(mAvailableFrequencies, new Comparator<String>() {
            @Override
            public int compare(String object1, String object2) {
                return Utils.tryValueOf(object1, 0).compareTo(Utils.tryValueOf(object2, 0));
            }
        });
        Collections.reverse(Arrays.asList(mAvailableFrequencies));

        final ArrayList<String> entries = new ArrayList<>();
        for (final String mAvailableFreq : mAvailableFrequencies) {
            entries.add(CpuUtils.toMhz(mAvailableFreq));
        }

        mMax.setEntries(entries.toArray(new String[entries.size()]));
        mMax.setEntryValues(mAvailableFrequencies);
        mMax.setValue(cpuFreq.maximum);
        mMax.setSummary(CpuUtils.toMhz(cpuFreq.maximum));
        mMax.setEnabled(true);

        mMin.setEntries(entries.toArray(new String[entries.size()]));
        mMin.setEntryValues(mAvailableFrequencies);
        mMin.setValue(cpuFreq.minimum);
        mMin.setSummary(CpuUtils.toMhz(cpuFreq.minimum));
        mMin.setEnabled(true);

        entries.clear();
    }

    @Override public void onGovernor(@NonNull final GovernorUtils.Governor governor) {
        mGovernor.setEntries(governor.available);
        mGovernor.setEntryValues(governor.available);
        mGovernor.setValue(governor.current);
        mGovernor.setSummary(governor.current);
        mGovernor.setEnabled(true);
    }

    public View generateRow(final int core, final CpuCore cpuCore) {
        if (!isAdded() || mCpuInfo == null) { return null; }
        Logger.v(this, String.format("generateRow(%s);", cpuCore.toString()));

        View rowView = mCpuInfo.getChildAt(core);
        if (rowView == null) {
            rowView = new CpuCoreView(getActivity());
            mCpuInfo.addView(rowView);
        }

        if (rowView instanceof CpuCoreView) {
            final boolean isOffline = cpuCore.mCoreCurrent == 0;

            ((CpuCoreView) rowView).core.setText(cpuCore.mCore);
            ((CpuCoreView) rowView).freq.setText(isOffline
                    ? getString(R.string.core_offline)
                    : CpuUtils.toMhz(String.valueOf(cpuCore.mCoreCurrent))
                    + " / " + CpuUtils.toMhz(String.valueOf(cpuCore.mCoreMax))
                    + " [" + cpuCore.mCoreGov + ']');
            ((CpuCoreView) rowView).bar.setMax(cpuCore.mCoreMax);
            ((CpuCoreView) rowView).bar.setProgress(cpuCore.mCoreCurrent);
        }

        return rowView;
    }

}

