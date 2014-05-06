package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.GpuEvent;
import org.namelessrom.devicecontrol.widgets.preferences.CustomListPreference;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.GpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class GpuSettingsFragment extends AttachPreferenceFragment
        implements DeviceConstants, PerformanceConstants, Preference.OnPreferenceChangeListener {

    private PreferenceCategory mRoot;
    private CustomListPreference mGpuFrequency = null;
    private CustomListPreference mGpuGovernor  = null;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_PERFORMANCE_GPU_SETTINGS);
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
        addPreferencesFromResource(R.xml.gpu);

        final Context context = getActivity();

        mRoot = (PreferenceCategory) getPreferenceScreen().findPreference("gpu");

        if (Utils.fileExists(GPU_FOLDER)) {
            refreshPreferences();
        } else {
            isSupported(mRoot, context);
        }
    }

    private void refreshPreferences() {
        if (Utils.fileExists(GPU_FREQUENCIES_FILE)) {
            GpuUtils.getOnGpuEvent();
        }
    }

    @Subscribe
    public void onGpuEvent(final GpuEvent event) {
        if (event == null) { return; }

        final String[] frequencies = event.getAvailFreqs();
        final String[] gpuNames = GpuUtils.freqsToMhz(frequencies);

        final int freqsLength = frequencies.length;
        final int namesLength = gpuNames.length;
        String value = event.getMaxFreq();
        if (value != null && freqsLength == namesLength) {
            value = value.trim();
            for (int i = 0; i < freqsLength; i++) {
                if (frequencies[i].equals(value)) {
                    value = gpuNames[i];
                    break;
                }
            }

            if (mGpuFrequency != null) {
                mGpuFrequency.setValue(GpuUtils.fromMHz(value));
                mGpuFrequency.setSummary(value);
            } else {
                mGpuFrequency = new CustomListPreference(getActivity());
                mGpuFrequency.setKey(PREF_MAX_GPU);
                mGpuFrequency.setTitle(R.string.gpu_freq_max);
                mGpuFrequency.setEntries(gpuNames);
                mGpuFrequency.setEntryValues(frequencies);
                mGpuFrequency.setValue(GpuUtils.fromMHz(value));
                mGpuFrequency.setSummary(value);
                mGpuFrequency.setOnPreferenceChangeListener(this);
                mRoot.addPreference(mGpuFrequency);
            }
        }

        final String gov = event.getGovernor();
        if (GpuUtils.containsGov(gov)) {
            if (mGpuGovernor != null) {
                mGpuGovernor.setSummary(gov);
                mGpuGovernor.setValue(gov);
            } else {
                mGpuGovernor = new CustomListPreference(getActivity());
                mGpuGovernor.setKey(PREF_GPU_GOV);
                mGpuGovernor.setTitle(R.string.governor);
                mGpuGovernor.setEntries(GPU_GOVS);
                mGpuGovernor.setEntryValues(GPU_GOVS);
                mGpuGovernor.setSummary(gov);
                mGpuGovernor.setValue(gov);
                mGpuGovernor.setOnPreferenceChangeListener(this);
                mRoot.addPreference(mGpuGovernor);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        if (mGpuFrequency == preference) {
            final String value = String.valueOf(newValue);
            mGpuFrequency.setValue(value);
            mGpuFrequency.setSummary(GpuUtils.toMhz(value));
            Utils.writeValue(GPU_MAX_FREQ_FILE, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_GPU, "gpu_max",
                            GPU_MAX_FREQ_FILE, value)
            );
            changed = true;
        } else if (mGpuGovernor == preference) {
            final String value = String.valueOf(newValue);
            mGpuGovernor.setValue(value);
            mGpuGovernor.setSummary(value);
            Utils.writeValue(GPU_GOV_PATH, value);
            PreferenceHelper.setBootup(
                    new DataItem(DatabaseHandler.CATEGORY_GPU, "gpu_gov",
                            GPU_GOV_PATH, value)
            );
            changed = true;
        }

        if (changed) {
            refreshPreferences();
        }

        return changed;
    }

}


