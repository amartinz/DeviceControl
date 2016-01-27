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
package org.namelessrom.devicecontrol.modules.device.sub;

import android.os.Bundle;
import android.preference.Preference;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.AwesomeSeekBarPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class SoundControlFragment extends AttachPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private AwesomeSeekBarPreference mMicrophone;
    private AwesomeSeekBarPreference mSpeaker;
    private AwesomeSeekBarPreference mVolume;

    @Override protected int getFragmentId() { return DeviceConstants.ID_SOUND_CONTROL; }

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
