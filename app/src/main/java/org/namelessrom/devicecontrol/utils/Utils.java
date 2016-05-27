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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.Command;
import at.amartinz.execution.NormalShell;
import at.amartinz.execution.RootShell;
import timber.log.Timber;

import static org.namelessrom.devicecontrol.utils.ShellOutput.OnShellOutputListener;

public class Utils {
    private static final String[] BLACKLIST = App.get().getStringArray(R.array.file_black_list);
    private static final String[] ENABLED_STATES = { "Y", "TRUE", "1", "255" };

    public static boolean isNameless(Context context) {
        return context.getPackageManager().hasSystemFeature("org.namelessrom.android")
               || existsInFile(Scripts.BUILD_PROP, "ro.nameless.version");
    }

    public static boolean existsInFile(final String file, final String prop) {
        return !findPropValue(file, prop).isEmpty();
    }

    public static String findPropValue(final String file, final String prop) {
        try {
            return findPropValueOf(file, prop);
        } catch (Exception e) { return ""; }
    }

    private static String findPropValueOf(final String file, final String prop) throws Exception {
        final File f = new File(file);
        if (f.exists() && f.canRead()) {
            FileInputStream fis = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            try {
                fis = new FileInputStream(f);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);

                String s;
                while ((s = br.readLine()) != null) {
                    if (s.contains(prop)) { return s.replace(prop + '=', ""); }
                }
            } finally {
                Utils.closeQuietly(br);
                Utils.closeQuietly(isr);
                Utils.closeQuietly(fis);
            }
        }

        return "";
    }

    public static String loadFromAssets(final String path) throws Exception {
        final StringBuilder sb = new StringBuilder();

        InputStream htmlStream = null;
        InputStreamReader reader = null;
        BufferedReader br = null;
        try {
            htmlStream = App.get().getAssets().open(path);
            reader = new InputStreamReader(htmlStream);
            br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } finally {
            Utils.closeQuietly(br);
            Utils.closeQuietly(reader);
            Utils.closeQuietly(htmlStream);
        }

        return sb.toString();
    }

    /**
     * Reads a single line from a file.
     *
     * @param sFile The file to read from.
     * @return The read string OR null if not existing.
     */
    public static String readOneLine(final String sFile) {
        return readOneLine(sFile, false);
    }

    @Nullable public static String readOneLine(final String sFile, final boolean trim) {
        if (fileExists(sFile)) {
            FileReader fileReader = null;
            BufferedReader brBuffer = null;
            try {
                fileReader = new FileReader(sFile);
                brBuffer = new BufferedReader(fileReader, 512);

                final String value = brBuffer.readLine();
                return ((trim && value != null) ? value.trim() : value);
            } catch (Exception e) {
                if (e instanceof FileNotFoundException) {
                    Timber.v("file exists but can not be read, trying with root");
                } else {
                    Timber.e(e, "could not read file: %s", sFile);
                }
                return readFileViaShell(sFile, true);
            } finally {
                Utils.closeQuietly(brBuffer);
                Utils.closeQuietly(fileReader);
            }
        } else {
            Timber.v("File does not exist or is not readable -> %s", sFile);
        }
        return null;
    }

    /**
     * Reads a file.
     *
     * @param sFile The file to read from.
     * @return The read string OR null if not existing.
     */
    @Nullable public static String readFile(final String sFile) {
        if (fileExists(sFile)) {
            FileReader reader = null;
            BufferedReader brBuffer = null;
            try {
                reader = new FileReader(sFile);
                brBuffer = new BufferedReader(reader, 512);

                final StringBuilder sb = new StringBuilder();
                String s;
                while ((s = brBuffer.readLine()) != null) {
                    sb.append(s).append('\n');
                }
                return sb.toString();
            } catch (Exception e) {
                return readFileViaShell(sFile, true);
            } finally {
                Utils.closeQuietly(brBuffer);
                Utils.closeQuietly(reader);
            }
        } else {
            Timber.v("File does not exist or is not readable -> %s", sFile);
        }
        return null;
    }

    public static String readFileViaShell(final String filePath, final boolean useSu) {
        final Command command = new Command(String.format("cat %s;", filePath));
        return useSu ? RootShell.fireAndBlockString(command) : NormalShell.fireAndBlock(command);
    }

    /**
     * Write a string value to the specified file.
     *
     * @param filename The filename
     * @param value    The value
     */
    public static boolean writeValue(final String filename, final String value) {
        if (fileExists(filename)) {
            try {
                final FileWriter fw = new FileWriter(filename);
                //noinspection TryFinallyCanBeTryWithResources
                try {
                    fw.write(value);
                } finally {
                    Utils.closeQuietly(fw);
                }
            } catch (IOException ignored) {
                writeValueViaShell(filename, value);
                return false;
            }
        }
        return true;
    }

    /**
     * Fallback if everything fails
     *
     * @param filename The file to write
     * @param value    The value to write
     */
    private static void writeValueViaShell(final String filename, final String value) {
        RootShell.fireAndForget(Utils.getWriteCommand(filename, value));
    }

    /**
     * Check if the specified file exists.
     *
     * @param filename The filename
     * @return Whether the file exists or not
     */
    public static boolean fileExists(final String filename) {
        //noinspection SimplifiableIfStatement
        if (TextUtils.isEmpty(filename) || TextUtils.equals("-", filename)) {
            return false;
        }
        return new File(filename).exists();
    }

    /**
     * Check if one of the specified files exists.
     *
     * @param files The list of filenames
     * @return Whether one of the files exists or not
     */
    public static boolean fileExists(final String[] files) {
        for (final String s : files) { if (fileExists(s)) { return true; } }
        return false;
    }

    /**
     * Checks if the given paths in a string array are existing and returns the existing path.
     *
     * @param paths The string array, containing the file paths
     * @return The path of the existing file as string
     */
    public static String checkPaths(final String[] paths) {
        for (final String s : paths) {
            if (fileExists(s)) { return s; }
        }
        return "";
    }

    /**
     * Checks if the given path is existing and returns the existing path.
     *
     * @param path The file path
     * @return The path or an empty string if not existent
     */
    public static String checkPath(final String path) {
        if (fileExists(path)) { return path; }
        return "";
    }

    /**
     * Reads string array from file
     *
     * @param path File to read from
     * @return string array
     */
    @Nullable public static String[] readStringArray(final String path) {
        final String line = readOneLine(path);
        if (line != null) {
            return line.split(" ");
        }
        return null;
    }

    public static String[] listFiles(final String path, final boolean blacklist) {
        return listFiles(path, blacklist ? BLACKLIST : null);
    }

    public static String[] listFiles(final String path, final String[] blacklist) {
        final String output = RootShell.fireAndBlockStringNewline(String.format("ls %s", path));
        Timber.v("listFiles --> output: %s", output);
        if (TextUtils.isEmpty(output)) {
            return Constants.EMPTY_STRINGS;
        }

        final String[] files = output.trim().split("\n");
        if (blacklist != null) {
            final ArrayList<String> filtered = new ArrayList<>();
            for (final String s : files) {
                if (!Utils.isFileBlacklisted(s, blacklist)) {
                    filtered.add(s);
                }
            }
            return filtered.toArray(new String[filtered.size()]);
        }
        return files;
    }

    public static boolean isFileBlacklisted(final String file, final String[] blacklist) {
        for (final String s : blacklist) {
            if (TextUtils.equals(s, file)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileName(final String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        final String[] splitted = path.trim().split("/");
        Timber.v("getFileName(%s) --> %s", path, splitted[splitted.length - 1]);
        return splitted[splitted.length - 1];
    }

    public static void getCommandResult(final OnShellOutputListener listener, final String cmd) {
        getCommandResult(listener, -1, cmd);
    }

    public static void getCommandResult(final OnShellOutputListener listener, final int id, final String cmd) {
        getCommandResult(listener, id, cmd, false);
    }

    public static void getCommandResult(final OnShellOutputListener listener, final int id, String cmd, boolean NEWLINE) {
        final Command command = new Command(id, cmd) {
            @Override public void onCommandCompleted(final int id, int exitcode) {
                super.onCommandCompleted(id, exitcode);
                final String result = getOutput();
                App.HANDLER.post(new CommandListenerRunnable(listener, id, result));
            }
        };
        if (NEWLINE) {
            command.setOutputType(Command.OUTPUT_STRING_NEWLINE);
        } else {
            command.setOutputType(Command.OUTPUT_STRING);
        }

        RootShell.fireAndBlock(command);
    }

    public static String getReadCommand(final String path) {
        return String.format("cat %s 2> /dev/null", path);
    }

    public static String getWriteCommand(final String path, final String value) {
        return String.format("chmod 644 %s;", path) +
               String.format("echo \"%s\" > %s;", value, path);
    }

    public static String lockFile(String path) {
        return String.format("chmod 444 %s;", path);
    }

    public static void toggleComponent(final ComponentName component, final boolean disable) {
        final PackageManager pm = App.get().getPackageManager();
        if (pm != null) {
            pm.setComponentEnabledSetting(component,
                    (disable
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
                    PackageManager.DONT_KILL_APP
            );
        }
    }

    public static boolean isEnabled(String s, final boolean contains) {
        if (s != null) {
            s = s.trim().toUpperCase();
            for (final String state : ENABLED_STATES) {
                if (contains) {
                    if (s.contains(state)) { return true; }
                } else {
                    if (s.equals(state)) { return true; }
                }
            }
        }
        return false;
    }

    public static String getBatteryHealth(final int healthInt) {
        int health;

        switch (healthInt) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                health = R.string.cold;
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                health = R.string.good;
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                health = R.string.dead;
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                health = R.string.over_voltage;
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                health = R.string.overheat;
                break;
            default:
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                health = R.string.unknown;
                break;
        }

        return App.get().getString(health);
    }

    public static void remount(final String path, final String mode) {
        final String args = String.format("-o %s,remount %s;", mode, path);
        final String cmd = BusyBox.callBusyBoxApplet("mount", args);
        // we need to wait for the remount to be done
        RootShell.fireAndBlock(cmd);
    }

    public static String setPermissions(final String path, final String mask, final int user, final int group) {
        final String chownArgs = String.format("%s.%s %s;", user, group, path);
        final String chmodArgs = String.format("%s %s;", mask, path);
        return BusyBox.callBusyBoxApplet("chown", chownArgs) + BusyBox.callBusyBoxApplet("chmod", chmodArgs);
    }

    public static void restartActivity(final Activity activity) {
        if (activity == null) {
            return;
        }
        activity.finish();
        activity.startActivity(activity.getIntent());
    }

    public static Integer tryValueOf(final String value, final int def) {
        try { return Integer.valueOf(value); } catch (Exception exc) { return def; }
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateAndTime() {
        final Date date = new Date(System.currentTimeMillis());
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

    public static int parseInt(final String integer) {
        return parseInt(integer, -1);
    }

    public static int parseInt(String integer, final int def) {
        try {
            if (integer != null) { integer = integer.trim(); }
            return Integer.parseInt(integer);
        } catch (NumberFormatException exc) {
            Timber.e(exc, "parseInt(%s, %s)", integer, def);
            return def;
        }
    }

    public static void closeQuietly(final Object closeable) {
        if (closeable instanceof Flushable) {
            try {
                ((Flushable) closeable).flush();
            } catch (IOException ignored) { }
        }
        if (closeable instanceof Closeable) {
            try {
                ((Closeable) closeable).close();
            } catch (IOException ignored) { }
        }
    }

    public static void patchSEPolicy(Context context) {
        StringBuilder sb = new StringBuilder();

        // supolicy --live "allow untrusted_app proc_touchpanel dir { search }"
        sb.append("supolicy --live \"allow untrusted_app proc_touchpanel dir { search }\";");

        if (!Utils.isNameless(context)) {
            // supolicy --live "allow platform_app proc_touchpanel dir { search }"
            sb.append("supolicy --live \"allow platform_app proc_touchpanel dir { search }\";");
        }

        RootShell.fireAndForget(sb.toString());
    }

    private static class CommandListenerRunnable implements Runnable {
        private final OnShellOutputListener listener;
        private final int id;
        private final String result;

        public CommandListenerRunnable(OnShellOutputListener listener, int id, String result) {
            this.listener = listener;
            this.id = id;
            this.result = result;
        }

        @Override public void run() {
            if (listener != null) {
                listener.onShellOutput(new ShellOutput(id, result));
            }
        }
    }
}
