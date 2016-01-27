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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.preferences.AwesomeTogglePreference;

import java.util.HashMap;
import java.util.Map;

public class PreferenceUtils {

    public static final int TYPE_EDITTEXT = 0;
    public static final int TYPE_CHECKBOX = 1;

    private static final HashMap<String, Integer> CONTENT_MAP = new HashMap<>();

    static {
        // general
        CONTENT_MAP.put("enable", TYPE_CHECKBOX);
        CONTENT_MAP.put("enabled", TYPE_CHECKBOX);
        // intelli plug
        CONTENT_MAP.put("intelli_plug_active", TYPE_CHECKBOX);
        CONTENT_MAP.put("touch_boost_active", TYPE_CHECKBOX);
    }

    private static final HashMap<String, Integer> MAP_TITLE = new HashMap<>();

    static {
        // general
        MAP_TITLE.put("enable", R.string.enable);
        MAP_TITLE.put("enabled", R.string.enable);
        // intelli plug
        MAP_TITLE.put("intelli_plug_active", R.string.enable);
        MAP_TITLE.put("touch_boost_active", R.string.touch_boost);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final HashMap<String, Integer> MAP_SUMMARY = new HashMap<>();

    static {
        // TODO: fill up!
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

    @Nullable public static Integer getTitle(final String fileName) {
        return MAP_TITLE.get(fileName);
    }

    @Nullable public static Integer getSummary(final String fileName) {
        return MAP_SUMMARY.get(fileName);
    }

    public static AwesomeEditTextPreference addAwesomeEditTextPreference(final Context context,
            final String key, final String category, final String path, final String fileName,
            final PreferenceCategory prefCat,
            final Preference.OnPreferenceChangeListener listener) {
        final AwesomeEditTextPreference preference = new AwesomeEditTextPreference(context,
                path + fileName, null, category, false, true);
        if (!preference.isSupported()) {
            // not supported, end here
            return null;
        }
        prefCat.addPreference(preference);
        preference.setKey(key + fileName);
        preference.setTitle(fileName);
        preference.initValue();
        preference.setOnPreferenceChangeListener(listener);
        return preference;
    }

    public static AwesomeTogglePreference addAwesomeTogglePreference(final Context context,
            final String key, String summary, final String category, final String path,
            String fileName, final PreferenceCategory prefCat,
            final Preference.OnPreferenceChangeListener listener) {
        final AwesomeTogglePreference preference = new AwesomeTogglePreference(context,
                path + fileName, null, category, false, true);
        if (!preference.isSupported()) {
            // not supported, end here
            return null;
        }
        prefCat.addPreference(preference);
        preference.setKey(key + fileName);
        preference.setTitle(fileName);
        if (!TextUtils.isEmpty(summary)) {
            preference.setSummary(summary);
        }
        preference.initValue();
        preference.setOnPreferenceChangeListener(listener);
        return preference;
    }

}
