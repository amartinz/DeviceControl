/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.preferences.SeekBarPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class ExtrasFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final int ID = 220;

    private static final int ID_WORK       = 100;
    private static final int ID_MPDECISION = 200;

    //==============================================================================================
    // Files
    //==============================================================================================
    public static final String  sPowerEfficientWorkFile =
            Utils.checkPaths(FILES_POWER_EFFICIENT_WORK);
    public static final boolean sPowerEfficientWork     = !sPowerEfficientWorkFile.isEmpty();
    //----------------------------------------------------------------------------------------------
    public static final String  sMcPowerSchedulerFile   =
            Utils.checkPaths(FILES_MC_POWER_SCHEDULER);
    public static final boolean sMcPowerScheduler       = !sMcPowerSchedulerFile.isEmpty();
    //----------------------------------------------------------------------------------------------
    private PreferenceScreen         mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mForceHighEndGfx;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mPowerEfficientWork;
    private SeekBarPreference        mMcPowerScheduler;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mMpDecision;
    private CustomCheckBoxPreference mIntelliPlug;
    private CustomCheckBoxPreference mIntelliPlugEco;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mMsmDcvs;
    private CustomPreference         mVoltageControl;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID); }

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras);
        mRoot = getPreferenceScreen();

        mForceHighEndGfx = (CustomCheckBoxPreference) findPreference(FORCE_HIGHEND_GFX_PREF);
        if (mForceHighEndGfx != null) {
            if (Utils.isLowRamDevice(getActivity())) {
                mForceHighEndGfx.setChecked(
                        Utils.existsInBuildProp("persist.sys.force_highendgfx=1"));
                mForceHighEndGfx.setOnPreferenceChangeListener(this);
            } else {
                mRoot.removePreference(mForceHighEndGfx);
            }
        }

        //------------------------------------------------------------------------------------------
        // Power Saving
        //------------------------------------------------------------------------------------------

        PreferenceCategory category = (PreferenceCategory) findPreference(CATEGORY_POWERSAVING);
        if (category != null) {
            mPowerEfficientWork =
                    (CustomCheckBoxPreference) findPreference(KEY_POWER_EFFICIENT_WORK);
            if (mPowerEfficientWork != null) {
                if (sPowerEfficientWork) {
                    Utils.getCommandResult(ID_WORK, Utils.getReadCommand(sPowerEfficientWorkFile));
                } else {
                    category.removePreference(mPowerEfficientWork);
                }
            }
            mMcPowerScheduler = (SeekBarPreference) findPreference(KEY_MC_POWER_SCHEDULER);
            if (mMcPowerScheduler != null) {
                if (sMcPowerScheduler) {
                    mMcPowerScheduler.setProgress(
                            Integer.parseInt(Utils.readOneLine(sMcPowerSchedulerFile))
                    );
                    mMcPowerScheduler.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMcPowerScheduler);
                }
            }
        }

        removeIfEmpty(category);
        //------------------------------------------------------------------------------------------
        // Hotplugging
        //------------------------------------------------------------------------------------------

        category = (PreferenceCategory) findPreference("hotplugging");
        if (category != null) {
            mMpDecision = (CustomCheckBoxPreference) findPreference("mpdecision");
            if (mMpDecision != null) {
                if (Utils.fileExists(MPDECISION_PATH)) {
                    Utils.getCommandResult(ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
                } else {
                    category.removePreference(mMpDecision);
                }
            }
        }

        removeIfEmpty(category);
        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------

        category = (PreferenceCategory) findPreference(GROUP_INTELLI_PLUG);
        if (category != null) {
            mIntelliPlug = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG);
            if (mIntelliPlug != null) {
                if (CpuUtils.hasIntelliPlug()) {
                    mIntelliPlug.setChecked(CpuUtils.getIntelliPlugActive());
                    mIntelliPlug.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlug);
                }
            }

            mIntelliPlugEco = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG_ECO);
            if (mIntelliPlugEco != null) {
                if (CpuUtils.hasIntelliPlug() && CpuUtils.hasIntelliPlugEcoMode()) {
                    mIntelliPlugEco.setChecked(CpuUtils.getIntelliPlugEcoMode());
                    mIntelliPlugEco.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlugEco);
                }
            }
        }

        removeIfEmpty(category);
        //------------------------------------------------------------------------------------------
        // Voltage
        //------------------------------------------------------------------------------------------

        category = (PreferenceCategory) findPreference("voltage");
        if (category != null) {
            mMsmDcvs = (CustomCheckBoxPreference) findPreference("msm_dcvs");
            if (mMsmDcvs != null) {
                if (CpuUtils.hasMsmDcvs()) {
                    mMsmDcvs.setChecked(CpuUtils.isMsmDcvs());
                    mMsmDcvs.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mMsmDcvs);
                }
            }

            mVoltageControl = (CustomPreference) findPreference("voltage_control");
            if (mVoltageControl != null) {
                if (Utils.fileExists(VDD_TABLE_FILE) || Utils.fileExists(UV_TABLE_FILE)) {
                    mVoltageControl.setOnPreferenceClickListener(this);
                } else {
                    category.removePreference(mVoltageControl);
                }
            }
        }

        removeIfEmpty(category);

        isSupported(mRoot, getActivity());
    }

    private void removeIfEmpty(final PreferenceGroup preferenceGroup) {
        if (preferenceGroup.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceGroup);
        }
    }

    @Override
    public boolean onPreferenceClick(final Preference preference) {

        if (mVoltageControl == preference) {
            BusProvider.getBus().post(new VoltageFragment());
            return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceHighEndGfx) {
            Utils.runRootCommand(Scripts.toggleForceHighEndGfx());
            changed = true;
        } else if (preference == mMpDecision) {
            final boolean value = (Boolean) o;
            Utils.runRootCommand(CpuUtils.enableMpDecision(value));
            PreferenceHelper.setBoolean(KEY_MPDECISION, value);
            changed = true;
        } else if (preference == mIntelliPlug) {
            final boolean value = (Boolean) o;
            CpuUtils.enableIntelliPlug(value);
            PreferenceHelper.setBoolean(KEY_INTELLI_PLUG, value);
            changed = true;
        } else if (preference == mIntelliPlugEco) {
            final boolean value = (Boolean) o;
            CpuUtils.enableIntelliPlugEcoMode(value);
            PreferenceHelper.setBoolean(KEY_INTELLI_PLUG_ECO, value);
            changed = true;
        } else if (preference == mPowerEfficientWork) {
            final boolean rawValue = (Boolean) o;
            final String value = rawValue ? "1" : "0";
            Utils.runRootCommand(Utils.getWriteCommand(sPowerEfficientWorkFile, value));
            PreferenceHelper.setBoolean(KEY_POWER_EFFICIENT_WORK, rawValue);
            changed = true;
        } else if (preference == mMcPowerScheduler) {
            final int value = (Integer) o;
            Utils.writeValue(sMcPowerSchedulerFile, String.valueOf(value));
            PreferenceHelper.setInt(KEY_MC_POWER_SCHEDULER, value);
            changed = true;
        } else if (preference == mMsmDcvs) {
            final boolean value = (Boolean) o;
            CpuUtils.enableMsmDcvs(value);
            PreferenceHelper.setBoolean("msm_dcvs", value);
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
                case ID_WORK:
                    if (mPowerEfficientWork != null) {
                        mPowerEfficientWork.setChecked(result.equals("Y"));
                        mPowerEfficientWork.setOnPreferenceChangeListener(this);
                    }
                    break;
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

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();
        String value;

        if (CpuUtils.hasIntelliPlug()) {
            value = PreferenceHelper.getBoolean(KEY_INTELLI_PLUG, false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(CpuUtils.INTELLI_PLUG_PATH, value));
        }
        if (CpuUtils.hasIntelliPlugEcoMode()) {
            value = PreferenceHelper.getBoolean(KEY_INTELLI_PLUG_ECO, false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(CpuUtils.INTELLI_PLUG_ECO_MODE_PATH, value));
        }
        if (sPowerEfficientWork) {
            value = PreferenceHelper.getBoolean(KEY_POWER_EFFICIENT_WORK, false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(sPowerEfficientWorkFile, value));
        }
        if (sMcPowerScheduler) {
            value = String.valueOf(PreferenceHelper.getInt(KEY_MC_POWER_SCHEDULER, 2));
            sbCmd.append(Utils.getWriteCommand(sMcPowerSchedulerFile, value));
        }
        if (CpuUtils.hasMsmDcvs()) {
            value = PreferenceHelper.getBoolean("msm_dcvs", false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(MSM_DCVS_FILE, value));
        }

        return sbCmd.toString();
    }

}
