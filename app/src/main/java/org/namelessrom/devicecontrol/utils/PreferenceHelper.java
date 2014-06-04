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

import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;

public class PreferenceHelper {
    //==============================================================================================
    // Fields
    //==============================================================================================
    private static PreferenceHelper ourInstance;
    private static DatabaseHandler  mDatabaseHandler;

    //==============================================================================================
    // Initialization
    //==============================================================================================

    public static PreferenceHelper getInstance(final Context context) {
        if (ourInstance == null) {
            ourInstance = new PreferenceHelper(context);
        }
        return ourInstance;
    }

    private PreferenceHelper(final Context context) {
        mDatabaseHandler = DatabaseHandler.getInstance(context);
    }

    //==============================================================================================
    // Generic
    //==============================================================================================

    public static void remove(final String name) {
        mDatabaseHandler.deleteItemByName(name, DatabaseHandler.TABLE_DC);
    }

    public static int getInt(final String name) {
        return getInt(name, 0);
    }

    public static int getInt(final String name, final int defaultValue) {
        final String value = mDatabaseHandler.getValueByName(name, DatabaseHandler.TABLE_DC);
        return (value == null || value.isEmpty() ? defaultValue : Integer.parseInt(value));
    }

    public static String getString(final String key) {
        return PreferenceHelper.getString(key, "");
    }

    public static String getString(final String name, final String defaultValue) {
        final String value = mDatabaseHandler.getValueByName(name, DatabaseHandler.TABLE_DC);
        return (value == null || value.isEmpty() ? defaultValue : value);
    }

    public static boolean getBoolean(final String name) {
        return PreferenceHelper.getBoolean(name, false);
    }

    public static boolean getBoolean(final String name, final boolean defaultValue) {
        final String value = mDatabaseHandler.getValueByName(name, DatabaseHandler.TABLE_DC);
        return (value == null || value.isEmpty() ? defaultValue : value.equals("1"));
    }

    public static void setString(final String name, final String value) {
        mDatabaseHandler.insertOrUpdate(name, value, DatabaseHandler.TABLE_DC);
    }

    public static void setInt(final String name, final int value) {
        mDatabaseHandler.insertOrUpdate(name, String.valueOf(value), DatabaseHandler.TABLE_DC);
    }

    public static void setBoolean(final String name, final boolean value) {
        mDatabaseHandler.insertOrUpdate(name, (value ? "1" : "0"), DatabaseHandler.TABLE_DC);
    }

    public static String getBootupValue(final String name) {
        return mDatabaseHandler.getValueByName(name, DatabaseHandler.TABLE_BOOTUP);
    }

    public static void setBootup(final DataItem dataItem) {
        mDatabaseHandler.updateBootup(dataItem);
    }
}
