package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.helpers.GpuUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import java.io.IOException;

public class PerformanceGpuFragment extends AttachPreferenceFragment
        implements PerformanceConstants, Preference.OnPreferenceChangeListener {

    public static final int ID = 215;

    private PreferenceCategory   mRoot;
    private CustomListPreference mGpuFrequency;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.performance_gpu);

        final Context context = getActivity();

        mRoot = (PreferenceCategory) getPreferenceScreen().findPreference("gpu");

        if (Utils.fileExists(GPU_FOLDER)) {
            Utils.setPermissions(GPU_FOLDER);
            try {
                addPreferences();
            } catch (Exception ignored) { }
        }

        if (mRoot.getPreferenceCount() == 0) {
            final CustomPreference pref = new CustomPreference(context);
            pref.setTitle(R.string.no_tweakable_values);
            pref.setSummary(R.string.no_tweakable_gpu);
            pref.setTitleColor("#ffffff");
            pref.setSummaryColor("#ffffff");
            mRoot.addPreference(pref);
        }

        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    private void addPreferences() throws IOException {
        final Activity activity = getActivity();

        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            Utils.setPermissions(GPU_FREQUENCIES_FILE);
            Utils.setPermissions(GPU_MAX_FREQ_FILE);

            mGpuFrequency = new CustomListPreference(activity);

            final String[] frequencies = Utils.readStringArray(GPU_FREQUENCIES_FILE);
            final String[] gpuNames = GpuUtils.getFreqToMhz(GPU_FREQUENCIES_FILE);

            final int freqsLength = frequencies.length;
            final int namesLength = gpuNames.length;
            String value = Utils.readFileViaShell(GPU_MAX_FREQ_FILE);
            if (value != null && freqsLength == namesLength) {
                value = value.trim();
                for (int i = 0; i < freqsLength; i++) {
                    if (frequencies[i].equals(value)) {
                        value = gpuNames[i];
                        break;
                    }
                }
            } else {
                value = getString(R.string.unknown);
            }

            mGpuFrequency.setKey("gpu_freq");
            mGpuFrequency.setTitle(R.string.gpu_freq_max);
            mGpuFrequency.setEntries(gpuNames);
            mGpuFrequency.setEntryValues(frequencies);
            mGpuFrequency.setSummary(value);
            mGpuFrequency.setValue(value);
            mGpuFrequency.setOnPreferenceChangeListener(this);

            mRoot.addPreference(mGpuFrequency);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mGpuFrequency == preference) {
            final String value = String.valueOf(newValue);
            mGpuFrequency.setValue(value);
            mGpuFrequency.setSummary(GpuUtils.toMhz(value));
            Utils.writeValue(GPU_MAX_FREQ_FILE, value);
            updateSharedPrefs(PREF_MAX_GPU, value);
            return true;
        }
        return false;
    }

    private void updateSharedPrefs(final String var, final String value) {
        PreferenceHelper.setString(var, value);
    }
}


