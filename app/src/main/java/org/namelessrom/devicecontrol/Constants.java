/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class Constants {
    private static final String TAG = Constants.class.getSimpleName();

    public static final String URL_GITHUB_BASE = "https://github.com/Evisceration";
    public static final String URL_GITHUB_DC_COMMITS_BASE = URL_GITHUB_BASE + "/DeviceControl/commits/%s";
    public static final String URL_SENSE360 = "http://sense360.com";

    public static final String KEY_LOW_END_GFX = "low_end_gfx";
    public static final String KEY_USE_SENSE360 = "use_sense360";

    public static boolean canUseSense360(Context context) {
        // always enable on debug builds
        if (BuildConfig.DEBUG) {
            return true;
        }

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            final String simCountryIso = telephonyManager.getSimCountryIso().toLowerCase();
            Logger.v(TAG, "SimCountryIso: %s", simCountryIso);
            // if we can get the sim country iso and have an US user, we are able to use Sense360
            return "us".equals(simCountryIso);
        }

        // TODO: verify on non sim device
        // if we could not detect the sim country iso (no telephony devices) assume we can use it
        return true;
    }

    public static boolean useSense360(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // enable by default, if we can use sense360 and have not toggled the preference, it resolves in true
        return prefs.getBoolean(KEY_USE_SENSE360, canUseSense360(context));
    }
}
