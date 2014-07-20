/*
 * <!--
 *    Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */

package org.namelessrom.devicecontrol;

import android.util.Log;

/**
 * A Logging utility
 */
public class Logger {

    private static boolean DEBUG = false;

    public static synchronized void setEnabled(final boolean enable) { DEBUG = enable; }

    public static boolean getEnabled() { return DEBUG; }

    public static void d(final Object object, final String msg) {
        if (DEBUG) Log.d(getTag(object), "--> " + msg);
    }

    public static void e(final Object object, final String msg) {
        if (DEBUG) Log.e(getTag(object), "--> " + msg);
    }

    public static void i(final Object object, final String msg) {
        if (DEBUG) Log.i(getTag(object), "--> " + msg);
    }

    public static void v(final Object object, final String msg) {
        if (DEBUG) Log.v(getTag(object), "--> " + msg);
    }

    public static void w(final Object object, final String msg) {
        if (DEBUG) Log.w(getTag(object), "--> " + msg);
    }

    public static void wtf(final Object object, final String msg) {
        if (DEBUG) Log.wtf(getTag(object), "--> " + msg);
    }

    public static String getTag(final Object object) {
        if (object instanceof Class) {
            return ((Class) object).getSimpleName();
        } else if (object instanceof String) {
            return ((String) object);
        } else {
            return object.getClass().getSimpleName();
        }
    }

}
