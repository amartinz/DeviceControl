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

import android.text.TextUtils;

import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;

public class PreferenceHelper {

    private PreferenceHelper() { }

    /**
     * @deprecated
     */
    public static int getInt(final String name, final int defaultValue) {
        final String value = DatabaseHandler.getInstance()
                .getValueByName(name, DatabaseHandler.TABLE_DC);
        return (TextUtils.isEmpty(value) ? defaultValue : Utils.parseInt(value));
    }

    /**
     * @deprecated
     */
    public static boolean getBoolean(final String name, final boolean defaultValue) {
        final String value = DatabaseHandler.getInstance()
                .getValueByName(name, DatabaseHandler.TABLE_DC);
        return (TextUtils.isEmpty(value) ? defaultValue : value.equals("1"));
    }

    public static String getBootupValue(final String name) {
        return DatabaseHandler.getInstance().getValueByName(name, DatabaseHandler.TABLE_BOOTUP);
    }

    public static void setBootup(final DataItem dataItem) {
        DatabaseHandler.getInstance().updateBootup(dataItem);
    }
}
