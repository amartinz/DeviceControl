package org.namelessrom.devicecontrol.fragments.device.sub;

import android.os.Bundle;
import android.preference.Preference;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.AwesomeSeekBarPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class SoundControlFragment extends AttachPreferenceFragment implements DeviceConstants,
        Preference.OnPreferenceChangeListener {

    private AwesomeSeekBarPreference mMicrophone;
    private AwesomeSeekBarPreference mSpeaker;
    private AwesomeSeekBarPreference mVolume;

    @Override protected int getFragmentId() { return ID_SOUND_CONTROL; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_extras_sound_control);

        // update if we support other sound stuffs
        final CustomPreference mVersion = (CustomPreference) findPreference("sc_version");
        mVersion.setSummary("Franciso Franco");

        mMicrophone = (AwesomeSeekBarPreference) findPreference("microphone_gain");
        if (mMicrophone.isSupported()) {
            mMicrophone.initValue();
            mMicrophone.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mMicrophone);
        }

        mSpeaker = (AwesomeSeekBarPreference) findPreference("speaker_gain");
        if (mSpeaker.isSupported()) {
            mSpeaker.initValue();
            mSpeaker.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mSpeaker);
        }

        mVolume = (AwesomeSeekBarPreference) findPreference("volume_gain");
        if (mVolume.isSupported()) {
            mVolume.initValue();
            mVolume.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(mVolume);
        }


        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mMicrophone == preference) {
            mMicrophone.writeValue((Integer) o);
            return true;
        } else if (mSpeaker == preference) {
            mSpeaker.writeValue((Integer) o);
            return true;
        } else if (mVolume == preference) {
            mVolume.writeValue((Integer) o);
            return true;
        }

        return false;
    }

}
