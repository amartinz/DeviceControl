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
import android.text.TextUtils;

import timber.log.Timber;

public class Constants {
    public static final String[] EMPTY_STRINGS = new String[0];

    public static final String URL_DONATE_FLATTR =
            "https://flattr.com/submit/auto?user_id=amartinz&url=https://github.com/Evisceration/DeviceControl&title=DeviceControl&language=en_GB&tags=github&category=software";
    public static final String URL_DONATE_PAYPAL =
            "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZSN2SW53JJQJY";
    public static final String URL_GITHUB_BASE = "https://github.com/Evisceration";
    public static final String URL_GITHUB_DC_COMMITS_BASE = URL_GITHUB_BASE + "/DeviceControl/commits/%s";
    public static final String URL_SENSE360 = "http://sense360.com";

    public static final String KEY_LOW_END_GFX = "low_end_gfx";
    public static final String KEY_USE_SENSE360 = "use_sense360";

    private static final String[] SENSE_360_COUNTRY_LIST = { "us" };
    public static final int SENSE360_OK = 0;
    public static final int SENSE360_NO = 1;
    public static final int SENSE360_FAILED_DETECTION = 2;

    public static int canUseSense360(Context context) {
        // always enable on debug builds
        if (BuildConfig.DEBUG) {
            Timber.v("canUseSense360: debug mode, returning true");
            return SENSE360_OK;
        }

        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String simCountryIso = telephonyManager.getSimCountryIso();
            if (!TextUtils.isEmpty(simCountryIso)) {
                simCountryIso = simCountryIso.trim().toLowerCase();
            }
            Timber.v("SimCountryIso: %s", simCountryIso);

            // if we can get the sim country iso and have a whitelisted user, we are able to use Sense360
            int returnCode = SENSE360_NO;
            for (final String country : SENSE_360_COUNTRY_LIST) {
                if (TextUtils.equals(country, simCountryIso)) {
                    returnCode = SENSE360_OK;
                    break;
                }
            }
            Timber.v("canUseSense360: %s", returnCode);
            return returnCode;
        }

        // TODO: verify on non sim device
        // if we could not detect the sim country iso (no telephony devices) assume we can use it
        Timber.v("canUseSense360: detection failed, returning true");
        return SENSE360_FAILED_DETECTION;
    }

    public static boolean useSense360(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        // enable by default, if we can use sense360 and have not toggled the preference, it resolves in true
        return prefs.getBoolean(KEY_USE_SENSE360, canUseSense360(context) == SENSE360_OK);
    }
}
