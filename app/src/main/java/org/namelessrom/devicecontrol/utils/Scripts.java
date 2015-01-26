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

/**
 * Defines and runs Scripts.
 */
public class Scripts {

    public static final String BUILD_PROP = "/system/build.prop";
    public static final String SYSCTL = "/system/etc/sysctl.conf";

    public static final String APPEND_CMD = "echo \"%s=%s\" >> %s;";
    public static final String COPY_CMD = "busybox cp %s %s;";
    public static final String KILL_PROP_CMD = "busybox sed -i \"/%s/D\" %s;";
    public static final String REPLACE_CMD = "busybox sed -i \"/%s/ c %<s=%s\" %s;";

    public static String copyFile(final String source, final String destination) {
        return String.format(COPY_CMD, source, destination);
    }

    public static String addOrUpdate(final String property, final String value) {
        return addOrUpdate(property, value, BUILD_PROP);
    }

    public static String addOrUpdate(final String property, final String value, final String file) {
        if (Utils.existsInFile(file, property)) {
            return String.format(REPLACE_CMD, property, value, file);
        } else {
            return String.format(APPEND_CMD, property, value, file);
        }
    }

    public static String removeProperty(final String property) {
        return removeProperty(property, BUILD_PROP);
    }

    public static String removeProperty(final String property, final String file) {
        if (Utils.existsInFile(file, property)) {
            return String.format(KILL_PROP_CMD, property, file);
        }
        return "";
    }

}
