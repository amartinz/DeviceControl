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
package org.namelessrom.devicecontrol.objects;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which parses /proc/version and prepares information ready for usage
 */
public class KernelInfo {
    public String version;
    public String host;
    public String toolchain;
    public String revision;
    public String extras;
    public String date;

    public KernelInfo() {
        // initialize all with null
        version = null;
        host = null;
        toolchain = null;
        revision = null;
        extras = null;
        date = null;
    }

    @Override public String toString() {
        return String.format(
                "version: %s, host: %s, toolchain: %s, revision: %s, extras: %s, date: %s",
                version, host, toolchain, revision, extras, date);
    }

    public boolean feedWithInformation() {
        String rawKernelVersion = Utils.readFile("/proc/version");
        if (TextUtils.isEmpty(rawKernelVersion)) return false;

        // replace new lines as the readFile method appends a new line
        rawKernelVersion = rawKernelVersion.replace("\n", "");

        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (toolchain version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(\\(gcc.+? \\)) " +    /* group 3: GCC version information */
                        "(#\\d+) " +              /* group 4: "#1" */
                        "(.*?)?" +              /* group 5: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 6: "Thu Jun 28 11:02:39 PDT 2012" */

        final Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches() || m.groupCount() < 6) {
            Logger.e(this, "Regex does not match!");
            return false;
        }

        version = m.group(1);
        if (version != null) version = version.trim();

        host = m.group(2);
        if (host != null) host = host.trim();

        toolchain = m.group(3);
        if (toolchain != null) toolchain = toolchain.substring(1, toolchain.length() - 2).trim();

        revision = m.group(4);
        if (revision != null) revision = revision.trim();

        extras = m.group(5);
        if (extras != null) extras = extras.trim();

        date = m.group(6);
        if (date != null) date = date.trim();

        return true;
    }

}
