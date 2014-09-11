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
package org.namelessrom.devicecontrol.fragments.device;

import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

public class DeviceInformationFragment extends AttachPreferenceFragment implements DeviceConstants {

    private CustomPreference mPlatformVersion;
    private CustomPreference mBuildId;
    private CustomPreference mBuildType;
    private CustomPreference mBuildTags;

    private CustomPreference mAndroidId;

    private long[] mHits = new long[3];

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return ID_DEVICE; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_information);

        mPlatformVersion = (CustomPreference) findPreference("platform_version");
        mPlatformVersion.setSummary(Build.VERSION.RELEASE);

        mBuildId = (CustomPreference) findPreference("platform_id");
        mBuildId.setSummary(Build.VERSION.CODENAME);

        mBuildType = (CustomPreference) findPreference("platform_type");
        mBuildType.setSummary(Build.TYPE);

        mBuildTags = (CustomPreference) findPreference("platform_tags");
        mBuildTags.setSummary(Build.TAGS);

        // TODO: save / restore / check
        final String deviceId = Settings.Secure.getString(
                Application.applicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        mAndroidId = (CustomPreference) findPreference("android_id");
        mAndroidId.setSummary(deviceId);
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (mPlatformVersion == preference) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                Utils.runRootCommand("am start android/com.android.internal.app.PlatLogoActivity");
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
