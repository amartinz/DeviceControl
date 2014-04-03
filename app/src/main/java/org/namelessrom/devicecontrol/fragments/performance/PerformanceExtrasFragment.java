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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.events.PerformanceExtrasFragmentEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.SeekBarPreference;
import org.namelessrom.devicecontrol.utils.BusProvider;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.helpers.CpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class PerformanceExtrasFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, Preference.OnPreferenceChangeListener {

    public static final int ID = 220;

    //==============================================================================================
    // Files
    //==============================================================================================
    public static final String  sMcPowerSchedulerFile = Utils.checkPaths(FILES_MC_POWER_SCHEDULER);
    public static final boolean sMcPowerScheduler     = !sMcPowerSchedulerFile.equals("");
    //==============================================================================================
    // Fields
    //==============================================================================================
    private static      boolean IS_LOW_RAM_DEVICE     = false;
    //----------------------------------------------------------------------------------------------
    private PreferenceScreen         mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mForceHighEndGfx;
    //----------------------------------------------------------------------------------------------
    private SeekBarPreference        mMcPowerScheduler;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mIntelliPlug;
    private CustomCheckBoxPreference mIntelliPlugEco;

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
        addPreferencesFromResource(R.xml.performance_extras);
        mRoot = getPreferenceScreen();

        IS_LOW_RAM_DEVICE = Utils.getLowRamDevice(getActivity());

        if (IS_LOW_RAM_DEVICE) {
            mForceHighEndGfx = (CustomCheckBoxPreference) findPreference(FORCE_HIGHEND_GFX_PREF);
        } else {
            mRoot.removePreference(findPreference(FORCE_HIGHEND_GFX_PREF));
        }

        //------------------------------------------------------------------------------------------
        // Power Saving
        //------------------------------------------------------------------------------------------

        PreferenceCategory category = (PreferenceCategory) findPreference(CATEGORY_POWERSAVING);

        mMcPowerScheduler = (SeekBarPreference) findPreference(KEY_MC_POWER_SCHEDULER);
        if (sMcPowerScheduler) {
            mMcPowerScheduler.setProgress(
                    Integer.parseInt(Utils.readOneLine(sMcPowerSchedulerFile))
            );
            mMcPowerScheduler.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mMcPowerScheduler);
        }

        removeIfEmpty(category);

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------

        category = (PreferenceCategory) findPreference(GROUP_INTELLI_PLUG);

        mIntelliPlug = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG);
        if (CpuUtils.hasIntelliPlug()) {
            mIntelliPlug.setChecked(CpuUtils.getIntelliPlugActive());
            mIntelliPlug.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mIntelliPlug);
        }

        mIntelliPlugEco = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG_ECO);
        if (CpuUtils.hasIntelliPlug() && CpuUtils.hasIntelliPlugEcoMode()) {
            mIntelliPlugEco.setChecked(CpuUtils.getIntelliPlugEcoMode());
            mIntelliPlugEco.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mIntelliPlugEco);
        }

        removeIfEmpty(category);

        isSupported(mRoot, getActivity());

        new PerformanceCpuTask().execute();

    }

    private void removeIfEmpty(final PreferenceGroup preferenceGroup) {
        if (preferenceGroup.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceGroup);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mForceHighEndGfx) {
            Utils.runRootCommand(Scripts.toggleForceHighEndGfx());
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
        } else if (preference == mMcPowerScheduler) {
            final int value = (Integer) o;
            Utils.writeValue(sMcPowerSchedulerFile, String.valueOf(value));
            PreferenceHelper.setInt(KEY_MC_POWER_SCHEDULER, value);
            changed = true;
        }

        return changed;
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();
        String value;

        if (CpuUtils.hasIntelliPlug()) {
            logDebug("Reapplying: IntelliPlug");
            value = PreferenceHelper.getBoolean(KEY_INTELLI_PLUG, false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(CpuUtils.INTELLI_PLUG_PATH, value));
        }
        if (CpuUtils.hasIntelliPlugEcoMode()) {
            logDebug("Reapplying: IntelliPlugEco");
            value = PreferenceHelper.getBoolean(KEY_INTELLI_PLUG_ECO, false) ? "1" : "0";
            sbCmd.append(Utils.getWriteCommand(CpuUtils.INTELLI_PLUG_ECO_MODE_PATH, value));
        }
        if (PerformanceExtrasFragment.sMcPowerScheduler) {
            logDebug("Reapplying: McPowerScheduler");
            value = String.valueOf(PreferenceHelper.getInt(KEY_MC_POWER_SCHEDULER, 2));
            sbCmd.append(Utils.getWriteCommand(sMcPowerSchedulerFile, value));
        }

        return sbCmd.toString();
    }

    @Subscribe
    public void onPerformanceExtrasEvent(final PerformanceExtrasFragmentEvent event) {
        if (event == null) { return; }
        final boolean forceHighEndGfx = event.isForceHighEndGfx();
        if (mForceHighEndGfx != null) {
            mForceHighEndGfx.setChecked(forceHighEndGfx);
            mForceHighEndGfx.setOnPreferenceChangeListener(this);
        }
    }

    //==============================================================================================
    // Internal Classes
    //==============================================================================================

    class PerformanceCpuTask extends AsyncTask<Void, Void, PerformanceExtrasFragmentEvent>
            implements DeviceConstants {

        @Override
        protected PerformanceExtrasFragmentEvent doInBackground(Void... voids) {
            boolean isForceHighEndGfx = false;

            if (IS_LOW_RAM_DEVICE && Application.HAS_ROOT) {
                isForceHighEndGfx = Scripts.getForceHighEndGfx();
            }

            return new PerformanceExtrasFragmentEvent(isForceHighEndGfx);
        }

        @Override
        protected void onPostExecute(final PerformanceExtrasFragmentEvent event) {
            if (event != null) {
                BusProvider.getBus().post(event);
            }
        }
    }
}
