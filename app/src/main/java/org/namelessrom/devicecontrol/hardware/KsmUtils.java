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
package org.namelessrom.devicecontrol.hardware;

public class KsmUtils {
    public static final String KSM_PATH = "/sys/kernel/mm/ksm/";
    public static final String KSM_SLEEP = KSM_PATH + "sleep_millisecs";
    public static final String KSM_PAGES_VOLATILE = KSM_PATH + "pages_volatile";
    public static final String KSM_PAGES_UNSHARED = KSM_PATH + "pages_unshared";
    public static final String KSM_PAGES_TO_SCAN = KSM_PATH + "pages_to_scan";
    public static final String KSM_PAGES_SHARING = KSM_PATH + "pages_sharing";
    public static final String KSM_PAGES_SHARED = KSM_PATH + "pages_shared";
    public static final String KSM_FULL_SCANS = KSM_PATH + "full_scans";
}
