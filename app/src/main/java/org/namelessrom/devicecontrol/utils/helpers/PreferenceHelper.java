/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.utils.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by alex on 18.12.13.
 */
public class PreferenceHelper {
    //==============================================================================================
    // Fields
    //==============================================================================================
    private static PreferenceHelper ourInstance;
    private static SharedPreferences mSharedPrefs;

    //==============================================================================================
    // Initialization
    //==============================================================================================

    public static PreferenceHelper getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new PreferenceHelper(context);
        }
        return ourInstance;
    }

    private PreferenceHelper(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //==============================================================================================
    // Generic
    //==============================================================================================

    public static void remove(String key) {
        mSharedPrefs.edit().remove(key).commit();
    }

    public static int getInt(String key) {
        return PreferenceHelper.getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        return mSharedPrefs.getInt(key, defaultValue);
    }

    public static String getString(String key) {
        return mSharedPrefs.getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        return mSharedPrefs.getString(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPrefs.getBoolean(key, defaultValue);
    }

    public static void setString(String key, String value) {
        mSharedPrefs.edit().putString(key, value).commit();
    }

    public static void setInt(String key, int value) {
        mSharedPrefs.edit().putInt(key, value).commit();
    }

    public static void setBoolean(String key, boolean value) {
        mSharedPrefs.edit().putBoolean(key, value).commit();
    }

    //==============================================================================================
    // Specific
    //==============================================================================================

    public static int getTransformerId() {
        return Integer.parseInt(mSharedPrefs.getString(
                "prefs_jf_appearance_custom_transformer", "0"));
    }

    public static void setTransformerId(String value) {
        mSharedPrefs.edit().putString("prefs_jf_appearance_custom_transformer", value).commit();
    }

    public static boolean getCustomAnimations() {
        return mSharedPrefs.getBoolean("prefs_jf_appearance_custom_animations", true);
    }

    public static void setCustomAnimations(boolean value) {
        mSharedPrefs.edit().putBoolean("prefs_jf_appearance_custom_animations", value).commit();
    }
}
