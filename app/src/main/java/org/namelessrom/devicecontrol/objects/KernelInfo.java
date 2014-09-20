package org.namelessrom.devicecontrol.objects;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class which parses /proc/version and prepares information ready for usage
 */
public class KernelInfo {
    public String version;
    public String host;
    public String gcc;
    public String revision;
    public String extras;
    public String date;

    public KernelInfo() {
        // initialize all with null
        version = null;
        host = null;
        gcc = null;
        revision = null;
        extras = null;
        date = null;
    }

    @Override public String toString() {
        return String.format("version: %s, host: %s, gcc: %s, revision: %s, extras: %s, date: %s",
                version, host, gcc, revision, extras, date);
    }

    public boolean feedWithInformation() {
        final File kernelInfoFile = new File("/proc/version");
        if (!kernelInfoFile.exists()) {
            return false;
        }

        final StringBuilder sb = new StringBuilder();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(kernelInfoFile);
            br = new BufferedReader(fr);

            String aLine;
            while ((aLine = br.readLine()) != null) {
                aLine = aLine.trim();
                if (!TextUtils.isEmpty(aLine)) sb.append(aLine);
            }
        } catch (IOException e) {
            Logger.e(this, "could not get cpu information", e);
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception ignored) { }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception ignored) { }
            }
        }

        final String rawKernelVersion = sb.toString();
        if (TextUtils.isEmpty(rawKernelVersion)) return false;

        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
                "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
                        "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
                        "(\\(gcc.+? \\)) " +    /* group 3: GCC version information */
                        "(#\\d+) " +              /* group 4: "#1" */
                        "(.*?)?" +              /* group 5: optional SMP, PREEMPT, and any CONFIG_FLAGS */
                        "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 6: "Thu Jun 28 11:02:39 PDT 2012" */

        final Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Logger.e(this, "Regex did not match on /proc/version: " + rawKernelVersion);
            return false;
        } else if (m.groupCount() < 6) {
            Logger.e(this,
                    "Regex match on /proc/version only returned " + m.groupCount() + " groups");
            return false;
        }

        version = m.group(1);
        if (version != null) version = version.trim();

        host = m.group(2);
        if (host != null) host = host.trim();

        gcc = m.group(3);
        if (gcc != null) gcc = gcc.substring(1, gcc.length() - 2).trim();

        revision = m.group(4);
        if (revision != null) revision = revision.trim();

        extras = m.group(5);
        if (extras != null) extras = extras.trim();

        date = m.group(6);
        if (date != null) date = date.trim();

        return true;
    }

}
