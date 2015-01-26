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
package org.namelessrom.devicecontrol.ui.fragments.tools.editor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.widget.EditText;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.preferences.CustomEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class BuildPropFragment extends AttachPreferenceFragment
        implements DeviceConstants, Preference.OnPreferenceChangeListener {

    private static final String DISABLE = "disable";
    private static final String WIFI_SCAN_PREF = "pref_wifi_scan_interval";
    private static final String WIFI_SCAN_PROP = "wifi.supplicant_scan_interval";
    private static final String WIFI_SCAN_DEFAULT = System.getProperty(WIFI_SCAN_PROP);
    private static final String MAX_EVENTS_PREF = "pref_max_events";
    private static final String MAX_EVENTS_PROP = "windowsmgr.max_events_per_sec";
    private static final String MAX_EVENTS_DEFAULT = System.getProperty(MAX_EVENTS_PROP);
    private static final String RING_DELAY_PREF = "pref_ring_delay";
    private static final String RING_DELAY_PROP = "ro.telephony.call_ring.delay";
    private static final String RING_DELAY_DEFAULT = System.getProperty(RING_DELAY_PROP);
    private static final String VM_HEAPSIZE_PREF = "pref_vm_heapsize";
    private static final String VM_HEAPSIZE_PROP = "dalvik.vm.heapsize";
    private static final String VM_HEAPSIZE_DEFAULT = System.getProperty(VM_HEAPSIZE_PROP);
    private static final String FAST_UP_PREF = "pref_fast_up";
    private static final String FAST_UP_PROP = "ro.ril.hsxpa";
    private static final String FAST_UP_DEFAULT = System.getProperty(FAST_UP_PROP);
    private static final String PROX_DELAY_PREF = "pref_prox_delay";
    private static final String PROX_DELAY_PROP = "mot.proximity.delay";
    private static final String PROX_DELAY_DEFAULT = System.getProperty(PROX_DELAY_PROP);
    private static final String MOD_LCD_PROP = "ro.sf.lcd_density";
    private static final String MOD_LCD_PREF = "pref_lcd_density";
    private static final String SLEEP_PREF = "pref_sleep";
    private static final String SLEEP_PROP = "pm.sleep_mode";
    private static final String SLEEP_DEFAULT = System.getProperty(SLEEP_PROP);
    private static final String TCP_STACK_PREF = "pref_tcp_stack";
    private static final String TCP_STACK_PROP_0 = "net.tcp.buffersize.default";
    private static final String TCP_STACK_PROP_1 = "net.tcp.buffersize.wifi";
    private static final String TCP_STACK_PROP_2 = "net.tcp.buffersize.umts";
    private static final String TCP_STACK_PROP_3 = "net.tcp.buffersize.gprs";
    private static final String TCP_STACK_PROP_4 = "net.tcp.buffersize.edge";
    private static final String TCP_STACK_BUFFER = "4096,87380,256960,4096,16384,256960";
    private static final String JIT_PREF = "pref_jit";
    private static final String JIT_PROP = "dalvik.vm.execution-mode";
    private static final String GPU_PREF = "pref_gpu";
    private static final String GPU_PROP = "debug.sf.hw";

    private CustomPreference mFullEditor;
    private CustomListPreference mWifiScanPref;
    private CustomListPreference mMaxEventsPref;
    private CustomListPreference mRingDelayPref;
    private CustomListPreference mVmHeapsizePref;
    private CustomListPreference mFastUpPref;
    private CustomListPreference mProxDelayPref;
    private CustomEditTextPreference mLcdPref;
    private CustomListPreference mSleepPref;
    private CustomTogglePreference mTcpStackPref;
    private CustomTogglePreference mJitPref;
    private CustomTogglePreference mGpuPref;

    private boolean result = false;

    @Override protected int getFragmentId() { return ID_TOOLS_BUILD_PROP; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prop_modder);

        final PreferenceScreen prefSet = getPreferenceScreen();

        mFullEditor = (CustomPreference) prefSet.findPreference("pref_full_editor");

        mWifiScanPref = (CustomListPreference) prefSet.findPreference(WIFI_SCAN_PREF);
        if (mWifiScanPref != null) {
            mWifiScanPref.setOnPreferenceChangeListener(this);
        }

        mMaxEventsPref = (CustomListPreference) prefSet.findPreference(MAX_EVENTS_PREF);
        if (mMaxEventsPref != null) {
            mMaxEventsPref.setOnPreferenceChangeListener(this);
        }

        mRingDelayPref = (CustomListPreference) prefSet.findPreference(RING_DELAY_PREF);
        if (mRingDelayPref != null) {
            mRingDelayPref.setOnPreferenceChangeListener(this);
        }

        mVmHeapsizePref = (CustomListPreference) prefSet.findPreference(VM_HEAPSIZE_PREF);
        if (mVmHeapsizePref != null) {
            mVmHeapsizePref.setOnPreferenceChangeListener(this);
        }

        mFastUpPref = (CustomListPreference) prefSet.findPreference(FAST_UP_PREF);
        if (mFastUpPref != null) {
            mFastUpPref.setOnPreferenceChangeListener(this);
        }

        mProxDelayPref = (CustomListPreference) prefSet.findPreference(PROX_DELAY_PREF);
        if (mProxDelayPref != null) {
            mProxDelayPref.setOnPreferenceChangeListener(this);
        }

        mSleepPref = (CustomListPreference) prefSet.findPreference(SLEEP_PREF);
        if (mSleepPref != null) {
            mSleepPref.setOnPreferenceChangeListener(this);
        }

        mTcpStackPref = (CustomTogglePreference) prefSet.findPreference(TCP_STACK_PREF);

        mJitPref = (CustomTogglePreference) prefSet.findPreference(JIT_PREF);

        mLcdPref = (CustomEditTextPreference) prefSet.findPreference(MOD_LCD_PREF);
        final String lcd = Utils.findPropValue(Scripts.BUILD_PROP, MOD_LCD_PROP);
        if (mLcdPref != null) {
            final EditText lcdET = mLcdPref.getEditText();
            if (lcdET != null) {
                InputFilter lengthFilter = new LengthFilter(3);
                lcdET.setFilters(new InputFilter[]{ lengthFilter });
                lcdET.setSingleLine(true);
            }
            mLcdPref.setSummary(String.format(getString(R.string.lcd_density_alt_summary), lcd));
            mLcdPref.setText(lcd);
            mLcdPref.setOnPreferenceChangeListener(this);
        }

        mGpuPref = (CustomTogglePreference) prefSet.findPreference(GPU_PREF);

        updateScreen();
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        boolean value;
        if (preference == mFullEditor) {
            MainActivity.loadFragment(getActivity(), ID_TOOLS_EDITORS_BUILD_PROP);
            return true;
        } else if (preference == mTcpStackPref) {
            value = mTcpStackPref.isChecked();
            return doMod(TCP_STACK_PROP_0, String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PROP_1,
                    String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PROP_2,
                    String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PROP_3,
                    String.valueOf(value ? TCP_STACK_BUFFER : DISABLE))
                    && doMod(TCP_STACK_PROP_4,
                    String.valueOf(value ? TCP_STACK_BUFFER : DISABLE));
        } else if (preference == mJitPref) {
            value = mJitPref.isChecked();
            if (value) {
                return doMod(JIT_PROP, "int:jit");
            } else {
                return doMod(JIT_PROP, "int:fast");
            }
        } else if (preference == mGpuPref) {
            value = mGpuPref.isChecked();
            return doMod(GPU_PROP, String.valueOf(value ? 1 : DISABLE));
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            if (preference == mWifiScanPref) {
                return doMod(WIFI_SCAN_PROP, newValue.toString());
            } else if (preference == mMaxEventsPref) {
                return doMod(MAX_EVENTS_PROP, newValue.toString());
            } else if (preference == mRingDelayPref) {
                return doMod(RING_DELAY_PROP, newValue.toString());
            } else if (preference == mVmHeapsizePref) {
                return doMod(VM_HEAPSIZE_PROP, newValue.toString());
            } else if (preference == mFastUpPref) {
                return doMod(FAST_UP_PROP, newValue.toString());
            } else if (preference == mProxDelayPref) {
                return doMod(PROX_DELAY_PROP, newValue.toString());
            } else if (preference == mLcdPref) {
                return doMod(MOD_LCD_PROP, newValue.toString());
            } else if (preference == mSleepPref) {
                return doMod(SLEEP_PROP, newValue.toString());
            }
        }

        return false;
    }

    private boolean doMod(final String key, final String value) {

        result = false;

        class AsyncDoModTask extends AsyncTask<Void, Void, Boolean> {
            private ProgressDialog pd = null;

            @Override protected void onPreExecute() {
                final Activity activity = getActivity();
                if (activity != null) {
                    pd = new ProgressDialog(activity);
                    pd.setIndeterminate(true);
                    pd.setMessage(getString(R.string.applying_wait));
                    pd.setCancelable(false);
                    pd.show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                Logger.v(this, String.format("Calling script with args '%s' and '%s'", key, value));
                backupBuildProp();
                Utils.remount("/system", "rw");
                boolean success = false;
                try {
                    if (value.equals(DISABLE)) {
                        Logger.v(this, String.format("value == %s", DISABLE));
                        final String cmd = Scripts.removeProperty(key);
                        success = cmd.isEmpty() || Utils.getCommandResult(cmd);
                    } else {
                        success = Utils.getCommandResult(Scripts.addOrUpdate(key, value));
                    }

                } catch (Exception exc) {
                    Logger.e(this, exc.getMessage());
                }

                return success;
            }

            @Override
            protected void onPostExecute(final Boolean res) {
                result = res;
                if (!res) {
                    restoreBuildProp();
                } else {
                    updateScreen();
                }
                Utils.remount("/system", "ro");
                if (pd != null) {
                    pd.dismiss();
                }
            }
        }
        new AsyncDoModTask().execute();
        return result;
    }

    private boolean backupBuildProp() {
        Logger.v(this, "Backing up build.prop to /data/local/tmp/pm_build.prop");
        return Utils.getCommandResult("cp /system/build.prop /data/local/tmp/pm_build.prop");
    }

    private boolean restoreBuildProp() {
        Logger.v(this, "Restoring build.prop from /data/local/tmp/pm_build.prop");
        return Utils.getCommandResult("cp /data/local/tmp/pm_build.prop /system/build.prop");
    }

    private void updateScreen() {
        final String wifi = Utils.findPropValue(Scripts.BUILD_PROP, WIFI_SCAN_PROP);
        if (!wifi.equals(DISABLE)) {
            mWifiScanPref.setValue(wifi);
            mWifiScanPref.setSummary(
                    String.format(getString(R.string.wifi_scan_alt_summary), wifi));
        } else {
            mWifiScanPref.setValue(WIFI_SCAN_DEFAULT);
        }
        final String maxE = Utils.findPropValue(Scripts.BUILD_PROP, MAX_EVENTS_PROP);
        if (!maxE.equals(DISABLE)) {
            mMaxEventsPref.setValue(maxE);
            mMaxEventsPref.setSummary(
                    String.format(getString(R.string.max_events_alt_summary), maxE));
        } else {
            mMaxEventsPref.setValue(MAX_EVENTS_DEFAULT);
        }
        final String ring = Utils.findPropValue(Scripts.BUILD_PROP, RING_DELAY_PROP);
        if (!ring.equals(DISABLE)) {
            mRingDelayPref.setValue(ring);
            mRingDelayPref.setSummary(
                    String.format(getString(R.string.ring_delay_alt_summary), ring));
        } else {
            mRingDelayPref.setValue(RING_DELAY_DEFAULT);
        }
        final String vm = Utils.findPropValue(Scripts.BUILD_PROP, VM_HEAPSIZE_PROP);
        if (!vm.equals(DISABLE)) {
            mVmHeapsizePref.setValue(vm);
            mVmHeapsizePref.setSummary(
                    String.format(getString(R.string.vm_heapsize_alt_summary), vm));
        } else {
            mVmHeapsizePref.setValue(VM_HEAPSIZE_DEFAULT);
        }
        final String fast = Utils.findPropValue(Scripts.BUILD_PROP, FAST_UP_PROP);
        if (!fast.equals(DISABLE)) {
            mFastUpPref.setValue(fast);
            mFastUpPref
                    .setSummary(String.format(getString(R.string.fast_up_alt_summary), fast));
        } else {
            mFastUpPref.setValue(FAST_UP_DEFAULT);
        }
        final String prox = Utils.findPropValue(Scripts.BUILD_PROP, PROX_DELAY_PROP);
        if (!prox.equals(DISABLE)) {
            mProxDelayPref.setValue(prox);
            mProxDelayPref.setSummary(
                    String.format(getString(R.string.prox_delay_alt_summary), prox));
        } else {
            mProxDelayPref.setValue(PROX_DELAY_DEFAULT);
        }
        final String sleep = Utils.findPropValue(Scripts.BUILD_PROP, SLEEP_PROP);
        if (!sleep.equals(DISABLE)) {
            mSleepPref.setValue(sleep);
            mSleepPref.setSummary(String.format(getString(R.string.sleep_alt_summary), sleep));
        } else {
            mSleepPref.setValue(SLEEP_DEFAULT);
        }
        final String tcp = Utils.findPropValue(Scripts.BUILD_PROP, TCP_STACK_PROP_0);
        if (tcp.equals(TCP_STACK_BUFFER)) {
            mTcpStackPref.setChecked(true);
        } else {
            mTcpStackPref.setChecked(false);
        }
        final String jit = Utils.findPropValue(Scripts.BUILD_PROP, JIT_PROP);
        if (jit.equals("int:jit")) {
            mJitPref.setChecked(true);
        } else {
            mJitPref.setChecked(false);
        }

        final String lcd = Utils.findPropValue(Scripts.BUILD_PROP, MOD_LCD_PROP);
        mLcdPref.setSummary(String.format(getString(R.string.lcd_density_alt_summary), lcd));

        final String gpu = Utils.findPropValue(Scripts.BUILD_PROP, GPU_PROP);
        mGpuPref.setChecked(Utils.isEnabled(gpu, false));
    }
}
