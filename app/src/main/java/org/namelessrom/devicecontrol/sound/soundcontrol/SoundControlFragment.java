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
package org.namelessrom.devicecontrol.sound.soundcontrol;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.MenuItem;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.preferences.SeekBarPreference;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.Locale;

/**
 * Controls Faux123's SoundControl Kernel Modules
 */
public class SoundControlFragment extends AttachPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    //=========================
    // Fields
    //=========================
    private ListPreference    mPresets;
    // Headphone Normal
    private SeekBarPreference mHeadphoneLeft;
    private SeekBarPreference mHeadphoneRight;
    // Headphone PowerAmp
    private SeekBarPreference mHeadphonePaLeft;
    private SeekBarPreference mHeadphonePaRight;
    // Microphone
    private SeekBarPreference mMicrophoneHandset;
    private SeekBarPreference mMicrophoneCamcorder;
    // Speaker
    private SeekBarPreference mSpeaker;
    //=========================
    // Preference Keys
    //=========================
    private static final String PREF_VERSION              = "sc_version";
    private static final String PREF_PRESETS              = "sc_presets";
    private static final String PREF_HEADPHONE_LEFT       = "sc_headphone_left";
    private static final String PREF_HEADPHONE_RIGHT      = "sc_headphone_right";
    private static final String PREF_HEADPHONE_PA_LEFT    = "sc_headphone_pa_left";
    private static final String PREF_HEADPHONE_PA_RIGHT   = "sc_headphone_pa_right";
    private static final String PREF_MICROPHONE_HANDSET   = "sc_microphone_handset";
    private static final String PREF_MICROPHONE_CAMCORDER = "sc_microphone_camcorder";
    private static final String PREF_SPEAKER              = "sc_speaker";

    //=========================
    // Overridden Methods
    //=========================

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, DeviceConstants.ID_SOUND_CONTROL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(DeviceConstants.ID_RESTORE_FROM_SUB));
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

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.features_sound_control);

        // Version
        Preference mVersion = findPreference(PREF_VERSION);
        mVersion.setTitle(R.string.version);
        String version = getScHelper().getVersion();
        if (TextUtils.isEmpty(version)) {
            version = getString(R.string.unknown);
        }
        mVersion.setSummary(version.toUpperCase(Locale.getDefault()));

        // Presets
        mPresets = (ListPreference) findPreference(PREF_PRESETS);
        if (mPresets.getValue() == null) mPresets.setValueIndex(0);
        mPresets.setSummary(mPresets.getEntry());
        mPresets.setOnPreferenceChangeListener(this);

        // Headphone
        mHeadphoneLeft = (SeekBarPreference) findPreference(PREF_HEADPHONE_LEFT);
        mHeadphoneRight = (SeekBarPreference) findPreference(PREF_HEADPHONE_RIGHT);
        updateHeadphone();
        mHeadphoneLeft.setOnPreferenceChangeListener(this);
        mHeadphoneRight.setOnPreferenceChangeListener(this);

        // Headphone PowerAmp
        mHeadphonePaLeft = (SeekBarPreference) findPreference(PREF_HEADPHONE_PA_LEFT);
        mHeadphonePaRight = (SeekBarPreference) findPreference(PREF_HEADPHONE_PA_RIGHT);
        updateHeadphonePa();
        mHeadphonePaLeft.setOnPreferenceChangeListener(this);
        mHeadphonePaRight.setOnPreferenceChangeListener(this);

        // Microphone Handset
        mMicrophoneHandset = (SeekBarPreference) findPreference(PREF_MICROPHONE_HANDSET);
        updateMicrophoneHandset();
        mMicrophoneHandset.setOnPreferenceChangeListener(this);

        // Microphone Camcorder
        mMicrophoneCamcorder = (SeekBarPreference) findPreference(PREF_MICROPHONE_CAMCORDER);
        updateMicrophoneCamcorder();
        mMicrophoneCamcorder.setOnPreferenceChangeListener(this);

        // Speaker
        mSpeaker = (SeekBarPreference) findPreference(PREF_SPEAKER);
        updateSpeaker();
        mSpeaker.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean changed = false;

        final int value = (newValue instanceof String)
                ? Integer.parseInt((String) newValue) : ((Integer) newValue);
        if (preference == mPresets) {
            loadPreset(value);
            mPresets.setSummary(getScHelper().getPresetsList()[value]);
            changed = true;
        } else if (preference == mHeadphoneLeft) {
            getScHelper().applyHeadphone(value, false);
            changed = true;
        } else if (preference == mHeadphoneRight) {
            getScHelper().applyHeadphone(value, true);
            changed = true;
        } else if (preference == mHeadphonePaLeft) {
            getScHelper().applyHeadphonePowerAmp(value, false);
            changed = true;
        } else if (preference == mHeadphonePaRight) {
            getScHelper().applyHeadphonePowerAmp(value, true);
            changed = true;
        } else if (preference == mMicrophoneHandset) {
            getScHelper().applyMicrophoneHandset(value);
            changed = true;
        } else if (preference == mMicrophoneCamcorder) {
            getScHelper().applyMicrophoneCamcorder(value);
            changed = true;
        } else if (preference == mSpeaker) {
            getScHelper().applySpeaker(value);
            changed = true;
        }

        return changed;
    }

    //=========================
    // Methods
    //=========================

    /**
     * Update values for <b>Headphone</b> or disable if not available
     */
    private void updateHeadphone() {
        if (getScHelper().getHeadphone()) {
            mHeadphoneLeft.setValue(getScHelper().readHeadphone(false));
            mHeadphoneRight.setValue(getScHelper().readHeadphone(true));
        } else {
            mHeadphoneLeft.setEnabled(false);
            mHeadphoneRight.setEnabled(false);
        }
    }

    /**
     * Update values for <b>Headphone PowerAmp</b> or disable if not available
     */
    private void updateHeadphonePa() {
        if (getScHelper().getHeadphonePa()) {
            mHeadphonePaLeft.setValue(getScHelper().readHeadphonePowerAmp(false));
            mHeadphonePaRight.setValue(getScHelper().readHeadphonePowerAmp(true));
        } else {
            mHeadphonePaLeft.setEnabled(false);
            mHeadphonePaRight.setEnabled(false);
        }
    }

    /**
     * Update values for <b>Microphone Handset</b> or disable if not available
     */
    private void updateMicrophoneHandset() {
        if (getScHelper().getMicrophoneHandset()) {
            mMicrophoneHandset.setValue(getScHelper().readMicrophoneHandset() - 40);
        } else {
            mMicrophoneHandset.setEnabled(false);
        }
    }

    /**
     * Update values for <b>Microphone Camcorder</b> or disable if not available
     */
    private void updateMicrophoneCamcorder() {
        if (getScHelper().getMicrophoneCam()) {
            mMicrophoneCamcorder.setValue(getScHelper().readMicrophoneCamcorder() - 40);
        } else {
            mMicrophoneCamcorder.setEnabled(false);
        }
    }

    /**
     * Update values for <b>Speaker</b> or disable if not available
     */
    private void updateSpeaker() {
        if (getScHelper().getSpeaker()) {
            mSpeaker.setValue(getScHelper().readSpeaker() - 40);
        } else {
            mSpeaker.setEnabled(false);
        }
    }

    /**
     * Switches presets and updates values
     *
     * @param i The id of the preset
     */
    private void loadPreset(final int i) {
        PreferenceHelper.setInt("preset_profile", i);
        getScHelper().restore();
        updateHeadphone();
        updateHeadphonePa();
        updateMicrophoneHandset();
        updateMicrophoneCamcorder();
        updateSpeaker();
    }

    /**
     * Everyone hates typing much, so we created a method for doing the same with less typing.
     *
     * @return An instance of the Sound Control Helper
     */
    private SoundControlHelper getScHelper() {
        return SoundControlHelper.getSoundControlHelper();
    }
}
