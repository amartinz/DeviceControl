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
package org.namelessrom.devicecontrol.utils;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.ui.preferences.AwesomeCheckBoxPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeEditTextPreference;

import java.util.HashMap;
import java.util.Map;

public class PreferenceUtils {

    public static final int TYPE_EDITTEXT = 0;
    public static final int TYPE_CHECKBOX = 1;

    private static final HashMap<String, Integer> CONTENT_MAP = new HashMap<String, Integer>();

    static {
        CONTENT_MAP.put("enabled", TYPE_CHECKBOX);
    }

    public static int getType(final String fileName) {
        return getType(fileName, CONTENT_MAP);
    }

    public static int getType(final String fileName, final HashMap<String, Integer> map) {
        if (TextUtils.isEmpty(fileName)) return TYPE_EDITTEXT;

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (TextUtils.equals(entry.getKey(), fileName)) return entry.getValue();
        }

        return TYPE_EDITTEXT;
    }

    public static void addAwesomeEditTextPreference(final Context context, final String key,
            final String category, final String path, final String fileName,
            final PreferenceCategory prefCat,
            final Preference.OnPreferenceChangeListener listener) {
        final AwesomeEditTextPreference preference = new AwesomeEditTextPreference(context,
                path + fileName, null, category, false, true);
        preference.setKey(key + fileName);
        if (preference.isSupported()) {
            prefCat.addPreference(preference);
            preference.setTitle(Utils.getFileName(path + fileName));
            preference.initValue();
            preference.setOnPreferenceChangeListener(listener);
        }
    }

    public static void addAwesomeCheckboxPreference(final Context context, final String key,
            final String summary, final String category, final String path, final String fileName,
            final PreferenceCategory prefCat,
            final Preference.OnPreferenceChangeListener listener) {
        final AwesomeCheckBoxPreference preference = new AwesomeCheckBoxPreference(context,
                path + fileName, null, category, false, true);
        preference.setKey(key + fileName);
        if (preference.isSupported()) {
            prefCat.addPreference(preference);
            preference.setTitle(Utils.getFileName(path + fileName));
            if (!TextUtils.isEmpty(summary)) {
                preference.setSummary(summary);
            }
            preference.initValue();
            preference.setOnPreferenceChangeListener(listener);
        }
    }

}
