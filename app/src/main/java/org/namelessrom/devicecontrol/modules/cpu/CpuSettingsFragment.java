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
package org.namelessrom.devicecontrol.modules.cpu;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.preferences.AutoEditTextPreference;
import org.namelessrom.devicecontrol.preferences.AutoSwitchPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreferenceCategoryMaterial;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachMaterialPreferenceFragment;
import org.namelessrom.devicecontrol.views.CpuCoreView;

import java.util.ArrayList;
import java.util.List;

import at.amartinz.execution.RootShell;
import at.amartinz.hardware.cpu.CpuCore;
import at.amartinz.hardware.cpu.CpuCoreMonitor;
import at.amartinz.hardware.cpu.CpuInformation;
import at.amartinz.hardware.cpu.CpuInformationListener;
import at.amartinz.hardware.cpu.CpuReader;
import alexander.martinz.libs.materialpreferences.MaterialListPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CpuSettingsFragment extends AttachMaterialPreferenceFragment implements CpuCoreMonitor.CoreListener, MaterialPreference.MaterialPreferenceChangeListener, MaterialPreference.MaterialPreferenceClickListener, CpuInformationListener {

    @BindView(R.id.cpu_pref_max) MaterialListPreference mMax;
    @BindView(R.id.cpu_pref_min) MaterialListPreference mMin;
    @BindView(R.id.cpu_pref_cpu_lock) MaterialSwitchPreference mCpuLock;

    @BindView(R.id.cpu_pref_governor) MaterialListPreference mGovernor;
    @BindView(R.id.cpu_pref_governor_tuning) MaterialPreference mGovernorTuning;
    @BindView(R.id.cpu_pref_gov_lock) MaterialSwitchPreference mCpuGovLock;

    private MaterialSwitchPreference mMpDecision;
    private MaterialListPreference mCpuQuietGov;

    @BindView(R.id.cpu_info_hide) SwitchCompat mStatusHide;
    @BindView(R.id.cpu_info) LinearLayout mCpuInfo;

    @BindString(R.string.core) String coreString;

    private static final int ID_MPDECISION = 200;


    private final ShellOutput.OnShellOutputListener mShellOutputListener = new ShellOutput.OnShellOutputListener() {
        @Override public void onShellOutput(final ShellOutput shellOutput) {
            if (shellOutput != null) {
                switch (shellOutput.id) {
                    case ID_MPDECISION:
                        if (mMpDecision != null) {
                            mMpDecision.setChecked(!TextUtils.isEmpty(shellOutput.output));
                            mMpDecision.setOnPreferenceChangeListener(
                                    CpuSettingsFragment.this);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    @Override protected int getLayoutResourceId() {
        return R.layout.preferences_cpu;
    }

    @Override protected int getFragmentId() { return DeviceConstants.ID_CTRL_PROCESSOR; }

    @Override public void onResume() {
        super.onResume();
        if (mStatusHide != null && mStatusHide.isChecked()) {
            CpuCoreMonitor.getInstance(App.HANDLER).start(this, 750);
        }
        CpuReader.getCpuInformation(CpuSettingsFragment.this);
    }

    @Override public void onPause() {
        super.onPause();
        CpuCoreMonitor.getInstance(App.HANDLER).stop();
    }

    @Override public void onDestroy() {
        CpuCoreMonitor.getInstance(App.HANDLER).destroy();
        super.onDestroy();
    }

    @Override public void onCores(@NonNull final List<CpuCore> cores) {
        final int count = cores.size();
        if (count != 0) {
            for (int i = 0; i < count; i++) {
                generateRow(i, cores.get(i));
            }
        }
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_refresh: {
                CpuReader.getCpuInformation(CpuSettingsFragment.this);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull @Override public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedState) {
        setHasOptionsMenu(true);

        final View view = super.onCreateView(inflater, root, savedState);
        ButterKnife.bind(this, view);

        final DeviceConfig deviceConfig = DeviceConfig.get();
        mStatusHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(final CompoundButton button, final boolean b) {
                if (b) {
                    mCpuInfo.setVisibility(View.VISIBLE);
                    CpuCoreMonitor.getInstance(App.HANDLER).start(CpuSettingsFragment.this, 750);
                } else {
                    CpuCoreMonitor.getInstance(App.HANDLER).stop();
                    mCpuInfo.setVisibility(View.GONE);
                }
                deviceConfig.perfCpuInfo = b;
                deviceConfig.save();
            }
        });
        mStatusHide.setChecked(deviceConfig.perfCpuInfo);
        if (mStatusHide.isChecked()) {
            mCpuInfo.setVisibility(View.VISIBLE);
        } else {
            mCpuInfo.setVisibility(View.GONE);
        }

        CpuCore tmpCore;
        final int numCpus = CpuReader.readAvailableCores();
        for (int i = 0; i < numCpus; i++) {
            tmpCore = new CpuCore(i, "0", "0", "0");
            generateRow(i, tmpCore);
        }

        mMax.setOnPreferenceChangeListener(CpuSettingsFragment.this);
        mMin.setOnPreferenceChangeListener(CpuSettingsFragment.this);
        mGovernor.setOnPreferenceChangeListener(CpuSettingsFragment.this);

        mCpuLock.getSwitch().setChecked(deviceConfig.perfCpuLock);
        mCpuLock.setOnPreferenceChangeListener(this);

        mGovernorTuning.setOnPreferenceClickListener(this);

        mCpuGovLock.getSwitch().setChecked(deviceConfig.perfCpuGovLock);
        mCpuGovLock.setOnPreferenceChangeListener(this);

        if (Utils.fileExists(getResources().getStringArray(R.array.directories_intelli_plug))
            || Utils.fileExists(getString(R.string.directory_mako_hotplug))
            || Utils.fileExists(getString(R.string.file_cpu_quiet_base))
            || Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
            setupHotpluggingPreferences();
        }

        view.postDelayed(new Runnable() {
            @Override public void run() {
                CpuReader.getCpuInformation(CpuSettingsFragment.this);
                view.postDelayed(new Runnable() {
                    @Override public void run() {
                        CpuReader.getCpuInformation(CpuSettingsFragment.this);
                    }
                }, 500);
            }
        }, 250);

        return view;
    }

    private void setupHotpluggingPreferences() {
        CustomPreferenceCategoryMaterial category = null;
        AutoSwitchPreference togglePref;
        AutoEditTextPreference editPref;
        String[] paths;
        String path;

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        boolean mpdecision = Utils.fileExists(MpDecisionAction.MPDECISION_PATH);
        boolean cpuQuiet = Utils.fileExists(getString(R.string.file_cpu_quiet_base))
                           && Utils.fileExists(getString(R.string.file_cpu_quiet_avail_gov))
                           && Utils.fileExists(getString(R.string.file_cpu_quiet_cur_gov));
        boolean hotplug = mpdecision || cpuQuiet;
        if (hotplug) {
            category = createCustomPreferenceCategoryMaterial("hotplug", getString(R.string.hotplug));
            addPreference(category);
        }

        if (mpdecision) {
            mMpDecision = createSwitchPreference(false, "mpdecision",
                    getString(R.string.mpdecision), getString(R.string.mpdecision_summary), false);
            category.addPreference(mMpDecision);
            Utils.getCommandResult(mShellOutputListener, ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
        }

        if (cpuQuiet) {
            final String[] govs = Utils.readOneLine(getString(R.string.file_cpu_quiet_avail_gov)).split(" ");
            final String gov = Utils.readOneLine(getString(R.string.file_cpu_quiet_cur_gov));
            mCpuQuietGov = new MaterialListPreference(getActivity());
            mCpuQuietGov.setAsCard(false);
            mCpuQuietGov.init(getActivity());
            mCpuQuietGov.setKey("pref_cpu_quiet_governor");
            mCpuQuietGov.setTitle(getString(R.string.cpu_quiet));
            mCpuQuietGov.setAdapter(mCpuQuietGov.createAdapter(govs, govs));
            mCpuQuietGov.setValue(gov);
            category.addPreference(mCpuQuietGov);
            mCpuQuietGov.setOnPreferenceChangeListener(this);
        }

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------
        paths = getResources().getStringArray(R.array.directories_intelli_plug);
        path = Utils.checkPaths(paths);
        if (!TextUtils.isEmpty(path)) {
            category = createCustomPreferenceCategoryMaterial("intelli_plug", getString(R.string.intelli_plug));
            addPreference(category);

            // setup intelli plug toggle
            if (Utils.fileExists(path + "intelli_plug_active")) {
                togglePref = new AutoSwitchPreference(getActivity());
                togglePref.setAsCard(false);
                togglePref.init(getActivity());
                togglePref.setCategory(BootupConfig.CATEGORY_INTELLI_HOTPLUG);
                togglePref.setKey("intelli_plug_intelli_plug_active");
                togglePref.setTitle(getString(R.string.enable));
                togglePref.setPath(path + "intelli_plug_active");
                togglePref.initValue();
                category.addPreference(togglePref);
            }
            // setup touch boost toggle
            if (Utils.fileExists(path + "touch_boost_active")) {
                togglePref = new AutoSwitchPreference(getActivity());
                togglePref.setAsCard(false);
                togglePref.init(getActivity());
                togglePref.setCategory(BootupConfig.CATEGORY_INTELLI_HOTPLUG);
                togglePref.setKey("intelli_plug_touch_boost_active");
                togglePref.setTitle(getString(R.string.touch_boost));
                togglePref.setPath(path + "touch_boost_active");
                togglePref.initValue();
                category.addPreference(togglePref);
            }
            // add the other files
            final String[] files = Utils.listFiles(path, false);
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    editPref = new AutoEditTextPreference(getActivity());
                    editPref.setAsCard(false);
                    editPref.init(getActivity());
                    editPref.setCategory(BootupConfig.CATEGORY_INTELLI_HOTPLUG);
                    editPref.setKey("intelli_plug_" + file);
                    editPref.setTitle(file);
                    editPref.setPath(path + file);
                    editPref.initValue();
                    category.addPreference(editPref);
                }
            }
        }

        path = Utils.checkPath(getString(R.string.directory_mako_hotplug));
        if (!TextUtils.isEmpty(path)) {
            category = createCustomPreferenceCategoryMaterial("mako_hotplug",
                    getString(R.string.mako_hotplug));
            addPreference(category);

            // setup mako_hotplug toggle
            if (Utils.fileExists(path + "enabled")) {
                togglePref = new AutoSwitchPreference(getActivity());
                togglePref.setAsCard(false);
                togglePref.init(getActivity());
                togglePref.setCategory(BootupConfig.CATEGORY_MAKO_HOTPLUG);
                togglePref.setKey("mako_enabled");
                togglePref.setTitle(getString(R.string.enable));
                togglePref.setPath(path + "enabled");
                togglePref.initValue();
                category.addPreference(togglePref);
            }
            final String[] files = Utils.listFiles(path, true);
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    editPref = new AutoEditTextPreference(getActivity());
                    editPref.setAsCard(false);
                    editPref.init(getActivity());
                    editPref.setCategory(BootupConfig.CATEGORY_MAKO_HOTPLUG);
                    editPref.setKey("mako_" + file);
                    editPref.setTitle(file);
                    editPref.setPath(path + file);
                    editPref.initValue();
                    category.addPreference(editPref);
                }
            }
        }

    }

    private CustomPreferenceCategoryMaterial createCustomPreferenceCategoryMaterial(String key, String title) {
        final Activity activity = getActivity();
        final CustomPreferenceCategoryMaterial preference = new CustomPreferenceCategoryMaterial(activity);
        preference.init(activity);
        preference.setKey(key);
        preference.setTitle(title);
        return preference;
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object o) {
        if (preference == mMax) {
            final String selected = String.valueOf(o);
            mMax.setValue(selected);

            final String other = String.valueOf(mMin.getValue());
            final boolean updateOther = Utils.parseInt(selected) < Utils.parseInt(other);
            if (updateOther) {
                onPreferenceChanged(mMin, selected);
            }

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MAX, selected, true);
            return true;
        } else if (preference == mMin) {
            final String selected = String.valueOf(o);
            mMin.setValue(selected);

            final String other = String.valueOf(mMax.getValue());
            final boolean updateOther = Utils.parseInt(selected) > Utils.parseInt(other);
            if (updateOther) {
                onPreferenceChanged(mMax, selected);
            }

            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_FREQUENCY_MIN, selected, true);
            return true;
        } else if (preference == mCpuLock) {
            DeviceConfig.get().perfCpuLock = (Boolean) o;
            DeviceConfig.get().save();
            return true;
        } else if (preference == mGovernor) {
            final String selected = String.valueOf(o);
            ActionProcessor.processAction(ActionProcessor.ACTION_CPU_GOVERNOR, selected, true);
            return true;
        } else if (preference == mCpuGovLock) {
            DeviceConfig.get().perfCpuGovLock = (Boolean) o;
            DeviceConfig.get().save();
            return true;
        } else if (preference == mMpDecision) {
            final boolean value = (Boolean) o;
            new MpDecisionAction(value ? "1" : "0", true).triggerAction();
            return true;
        } else if (preference == mCpuQuietGov) {
            final String path = getString(R.string.file_cpu_quiet_cur_gov);
            final String value = String.valueOf(o);
            RootShell.fireAndForget(Utils.getWriteCommand(path, value));
            BootupConfig.setBootup(new BootupItem(BootupConfig.CATEGORY_EXTRAS, mCpuQuietGov.getKey(), path, value, true));
            return true;
        }

        return false;
    }

    @Override public boolean onPreferenceClicked(MaterialPreference preference) {
        if (preference == mGovernorTuning) {
            final Activity activity = getActivity();
            if (activity instanceof ActivityCallbacks) {
                ((ActivityCallbacks) activity).shouldLoadFragment(DeviceConstants.ID_GOVERNOR_TUNABLE);
            }
            return true;
        }
        return false;
    }

    public View generateRow(final int core, final CpuCore cpuCore) {
        if (!isAdded() || mCpuInfo == null) {
            return null;
        }

        View rowView = mCpuInfo.getChildAt(core);
        if (rowView == null) {
            rowView = new CpuCoreView(getActivity());
            mCpuInfo.addView(rowView);
        }

        if (rowView instanceof CpuCoreView) {
            final boolean isOffline = cpuCore.current == 0;

            ((CpuCoreView) rowView).core.setText(String.format("%s %s:", coreString, cpuCore.core));
            ((CpuCoreView) rowView).freq.setText(isOffline
                    ? getString(R.string.core_offline)
                    : CpuInformation.toMhz(String.valueOf(cpuCore.current))
                      + " / " + CpuInformation.toMhz(String.valueOf(cpuCore.max))
                      + " [" + cpuCore.governor + ']');
            ((CpuCoreView) rowView).bar.setMax(cpuCore.max);
            ((CpuCoreView) rowView).bar.setProgress(cpuCore.current);
        }

        return rowView;
    }

    @Override public void onCpuInformation(@NonNull final CpuInformation cpuInformation) {
        final Integer[] availableFrequencies = cpuInformation.freqAvail.toArray(new Integer[cpuInformation.freqAvail.size()]);

        final ArrayList<String> entries = new ArrayList<>();
        for (final Integer availableFreq : availableFrequencies) {
            entries.add(CpuInformation.toMhz(String.valueOf(availableFreq)));
        }

        final ArrayList<String> entryValues = new ArrayList<>();
        for (final Integer availableFreq : availableFrequencies) {
            entryValues.add(String.valueOf(availableFreq));
        }

        final String[] entryArray = entries.toArray(new String[entries.size()]);
        final String[] entryValuesArray = entryValues.toArray(new String[entryValues.size()]);
        final String[] govAvail = cpuInformation.govAvail.toArray(new String[cpuInformation.govAvail.size()]);

        mMax.post(new Runnable() {
            @Override public void run() {
                mMax.setAdapter(mMax.createAdapter(entryArray, entryValuesArray));
                mMax.setValue(String.valueOf(cpuInformation.freqMax));
                mMax.setEnabled(true);

                mMin.setAdapter(mMin.createAdapter(entryArray, entryValuesArray));
                mMin.setValue(String.valueOf(cpuInformation.freqMin));
                mMin.setEnabled(true);

                mGovernor.setAdapter(mGovernor.createAdapter(govAvail, govAvail));
                mGovernor.setValue(cpuInformation.govCur);
                mGovernor.setEnabled(true);
            }
        });
    }
}

