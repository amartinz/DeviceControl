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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DeviceInformationFragment extends AttachPreferenceFragment implements DeviceConstants {

    private CustomPreference mPlatformVersion;
    private CustomPreference mBuildId;
    private CustomPreference mBuildType;
    private CustomPreference mBuildTags;

    private CustomPreference mVmLibrary;
    private CustomPreference mVmVersion;

    private CustomPreference mAndroidId;

    private SensorManager mSensorManager;

    private long[] mHits = new long[3];

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return ID_DEVICE; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.device_information);

        mSensorManager = (SensorManager) Application.get().getSystemService(Context.SENSOR_SERVICE);

        mPlatformVersion = (CustomPreference) findPreference("platform_version");
        mPlatformVersion.setSummary(Build.VERSION.RELEASE);

        mBuildId = (CustomPreference) findPreference("platform_id");
        mBuildId.setSummary(Build.VERSION.CODENAME);

        mBuildType = (CustomPreference) findPreference("platform_type");
        mBuildType.setSummary(Build.TYPE);

        mBuildTags = (CustomPreference) findPreference("platform_tags");
        mBuildTags.setSummary(Build.TAGS);

        // TODO: save / restore / check
        mAndroidId = (CustomPreference) findPreference("android_id");
        mAndroidId.setSummary(Utils.getAndroidId());

        mVmLibrary = (CustomPreference) findPreference("vm_library");
        final String library = Utils.getCommandResult("getprop persist.sys.dalvik.vm.lib", "-");
        if (!TextUtils.equals(library, "-")) {
            final String runtime = TextUtils.equals(library, "libdvm.so")
                    ? "Dalvik"
                    : TextUtils.equals(library, "libart.so") ? "ART" : "-";
            mVmLibrary.setSummary(String.format("%s (%s)", runtime, library));
        } else {
            mVmLibrary.setSummary(library);
        }

        mVmVersion = (CustomPreference) findPreference("vm_version");
        final String vmVersion = System.getProperty("java.vm.version", "-");
        mVmVersion.setSummary(vmVersion);

        PreferenceCategory category = (PreferenceCategory) findPreference("sensors");

        // we need an array list to be able to sort it, a normal list throws
        // java.lang.UnsupportedOperationException when sorting
        final ArrayList<Sensor> sensorList =
                new ArrayList<Sensor>(mSensorManager.getSensorList(Sensor.TYPE_ALL));

        Collections.sort(sensorList, new SortIgnoreCase());

        for (final Sensor s : sensorList) {
            final CustomPreference preference = new CustomPreference(getActivity());
            preference.setTitle(s.getName());
            preference.setSummary(s.getVendor());
            preference.setSelectable(false);
            category.addPreference(preference);
        }

        if (category.getPreferenceCount() == 0) {
            getPreferenceScreen().removePreference(category);
        }
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (mPlatformVersion == preference) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis() - 500)) {
                Utils.runRootCommand("am start android/com.android.internal.app.PlatLogoActivity");
                mPlatformVersion.setSelectable(false);
            }
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private class SortIgnoreCase implements Comparator<Sensor> {
        public int compare(final Sensor sensor1, final Sensor sensor2) {
            final String s1 = sensor1 != null ? sensor1.getName() : "";
            final String s2 = sensor2 != null ? sensor2.getName() : "";
            return s1.compareToIgnoreCase(s2);
        }
    }

}
