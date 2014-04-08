/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.namelessrom.devicecontrol.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.services.TaskerService;
import org.namelessrom.devicecontrol.utils.cmdprocessor.CMDProcessor;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static org.namelessrom.devicecontrol.Application.HANDLER;
import static org.namelessrom.devicecontrol.Application.logDebug;

public class Utils implements DeviceConstants, FileConstants {

    private static int isLowRamDevice = -1;

    public static boolean isNameless() {
        return existsInBuildProp("ro.nameless.version");
    }

    public static boolean existsInBuildProp(String filter) {
        final File f = new File("/system/build.prop");
        BufferedReader bufferedReader = null;
        if (f.exists() && f.canRead()) {
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains(filter)) return true;
                }
            } catch (Exception whoops) {
                return false;
            } finally {
                try {
                    if (bufferedReader != null) bufferedReader.close();
                } catch (Exception ignored) {
                    // mepmep
                }
            }
        }
        return false;
    }

    public static String findBuildPropValueOf(String prop) {
        final String mBuildPath = "/system/build.prop";
        final String DISABLE = "disable";
        String value = null;
        try {
            //create properties construct and load build.prop
            Properties mProps = new Properties();
            mProps.load(new FileInputStream(mBuildPath));
            //get the property
            value = mProps.getProperty(prop, DISABLE);
            logDebug(String.format("Helpers:findBuildPropValueOf found {%s} with the value (%s)",
                    prop, value));
        } catch (IOException ioe) {
            Log.d("TAG", "failed to load input stream");
        } catch (NullPointerException npe) {
            //swallowed thrown by ill formatted requests
        }

        if (value != null) {
            return value;
        } else {
            return DISABLE;
        }
    }

    /**
     * Reads a single line from a file.
     *
     * @param sFile The files to read from.
     * @return The read string OR null if not existing.
     */
    public static String readOneLine(String sFile) {
        String sLine = null;
        if (fileExists(sFile)) {
            BufferedReader brBuffer;
            try {
                brBuffer = new BufferedReader(new FileReader(sFile), 512);
                try {
                    sLine = brBuffer.readLine();
                } finally {
                    brBuffer.close();
                }
            } catch (Exception e) {
                logDebug("readOneLine: reading failed: " + e.getMessage());
                return readFileViaShell(sFile);
            }
        }
        return sLine;
    }

    /**
     * Fallback if everything fails
     *
     * @param filePath The file to read
     * @return The file's content
     */
    public static String readFileViaShell(String filePath) {
        return readFileViaShell(filePath, true);
    }

    public static String readFileViaShell(String filePath, boolean useSu) {
        final String command = "cat " + filePath;
        return useSu ? CMDProcessor.runSuCommand(command).getStdout()
                : CMDProcessor.runShellCommand(command).getStdout();
    }


    public static boolean getMount(final String mount) {
        final String[] mounts = getMounts("/system");
        if (mounts != null && mounts.length >= 3) {
            final String device = mounts[0];
            final String path = mounts[1];
            final String point = mounts[2];
            final String preferredMountCmd =
                    "mount -o " + mount + ",remount -t " + point + ' ' + device + ' ' + path;
            if (CMDProcessor.runSuCommand(preferredMountCmd).success()) {
                return true;
            }
        }
        final String fallbackMountCmd = "busybox mount -o remount," + mount + " /system";
        return CMDProcessor.runSuCommand(fallbackMountCmd).success();
    }

    public static String[] getMounts(CharSequence path) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"), 256);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(path)) {
                    return line.split(" ");
                }
            }
        } catch (FileNotFoundException ignored) {
            logDebug("/proc/mounts does not exist");
        } catch (IOException ignored) {
            logDebug("Error reading /proc/mounts");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
        return null;
    }

    /**
     * Write a string value to the specified file.
     *
     * @param filename The filename
     * @param value    The value
     */
    public static void writeValue(final String filename, final String value) {
        // the existence of the file is a requirement for the success ;)
        boolean success = fileExists(filename);
        if (success) {
            try {
                final FileWriter fw = new FileWriter(filename);
                try {
                    fw.write(value);
                } finally {
                    fw.close();
                }
            } catch (IOException ignored) { writeValueViaShell(filename, value); }
        }
    }

    /**
     * Fallback if everything fails
     *
     * @param filename The file to write
     * @param value    The value to write
     */
    private static void writeValueViaShell(final String filename, final String value) {
        runRootCommand(Utils.getWriteCommand(filename, value));
    }

    /**
     * Check if the specified file exists.
     *
     * @param filename The filename
     * @return Whether the file exists or not
     */
    public static boolean fileExists(String filename) {
        final boolean isExisting = new File(filename).exists();
        logDebug("fileExists: " + filename + ": " + (isExisting ? "true" : "false"));
        return isExisting;
    }

    /**
     * Checks if the given paths in a string array are existing and returns the existing path.
     *
     * @param paths The string array, containing the file paths
     * @return The path of the existing file as string
     */
    public static String checkPaths(final String[] paths) {
        for (String s : paths) {
            if (fileExists(s)) {
                return s;
            }
        }
        return "";
    }

    /**
     * Setup the directories for Device Control
     */
    public static void setupDirectories(final Activity activity) {
        File dir;
        final String logDir = activity.getFilesDir().getPath() + DC_LOG_DIR;
        final String backupDir = activity.getFilesDir().getPath() + DC_BACKUP_DIR;
        String[] dirList = new String[]{logDir, backupDir};
        for (String s : dirList) {
            dir = new File(s);
            if (!dir.exists()) {
                logDebug("setupDirectories: creating " + s);
                final boolean isSuccess = dir.mkdirs();
                logDebug("setupDirectories: " + (isSuccess ? "true" : "false"));
            }
        }
    }

    public static void createFiles(final Context context, final boolean force) {
        final String filepath = context.getFilesDir().getPath() + "/utils";
        logDebug("createFiles path: " + filepath);
        if (!new File(filepath).exists() || force) {
            RootTools.installBinary(context, R.raw.utils, "utils");
            logDebug("createFiles installed: utils");
        }
    }

    public static boolean getLowRamDevice(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return false;
        } else {
            if (isLowRamDevice == -1) {
                final ActivityManager activityManager
                        = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                isLowRamDevice = activityManager.isLowRamDevice() ? 1 : 0;
            }
            return (isLowRamDevice == 1);
        }
    }

    /**
     * Reads string array from file
     *
     * @param path File to read from
     * @return string array
     */
    public static String[] readStringArray(final String path) {
        final String line = readOneLine(path);
        if (line != null) {
            return line.split(" ");
        }
        return null;
    }

    public static void setPermissions(String file) {
        if (new File(file).exists()) {
            runRootCommand("chmod 644 " + file);
        }
    }

    public static void runRootCommand(String command) {
        final CommandCapture comm = new CommandCapture(0, false, command);
        try {
            RootTools.getShell(true).add(comm);
        } catch (Exception e) {
            logDebug("runRootCommand: " + e.getMessage());
        }
    }

    public static void getCommandResult(final int ID, final String COMMAND) {
        getCommandResult(ID, COMMAND, false);
    }

    public static void getCommandResult(final int ID, final String COMMAND, final boolean NEWLINE) {
        final StringBuilder sb = new StringBuilder();
        final CommandCapture comm = new CommandCapture(0, false, COMMAND) {
            @Override
            public void commandOutput(int id, String line) {
                sb.append(line);
                if (NEWLINE) {
                    sb.append('\n');
                }
            }

            @Override
            public void commandCompleted(int id, int exitcode) {
                final String result = sb.toString();
                logDebug(String.format("Generic Output for %s: %s", String.valueOf(ID), result));
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getBus().post(new ShellOutputEvent(ID, result));
                    }
                });
            }
        };
        try {
            RootTools.getShell(true).add(comm);
        } catch (Exception e) {
            logDebug("runRootCommand: " + e.getMessage());
        }
    }

    public static String getReadCommand(final String path) {
        final String cmd = String.format("cat %s 2> /dev/null", path);
        logDebug("ReadCommand: " + cmd);
        return cmd;
    }

    public static String getWriteCommand(final String path, final String value) {
        final String cmd = String.format("chmod 644 %s;\n", path) +
                String.format("busybox echo \"%s\" > %s;\n", value, path);
        logDebug("WriteCommand: " + cmd);
        return cmd;
    }

    public static boolean isPackageInstalled(final Context context, final String packageName) {
        try {
            final PackageManager pm = context.getPackageManager();
            if (pm != null) {
                pm.getPackageInfo(packageName, 0);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static void disableComponent(final Context context, final String packageName,
            final String componentName) {
        toggleComponent(context, packageName, componentName, true);
    }

    public static void enableComponent(final Context context, final String packageName,
            final String componentName) {
        toggleComponent(context, packageName, componentName, false);
    }

    private static void toggleComponent(final Context context, final String packageName,
            final String componentName, boolean disable) {
        final ComponentName component = new ComponentName(packageName,
                packageName + componentName);
        final PackageManager pm = context.getPackageManager();
        if (pm != null) {
            pm.setComponentEnabledSetting(component,
                    (disable
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
                    PackageManager.DONT_KILL_APP
            );
        }
    }

    public static void startTaskerService(final Context context) {
        final String packageName = context.getPackageName();
        final String componentName = TaskerService.class.getName().replace(packageName, "");
        final DatabaseHandler db = DatabaseHandler.getInstance(context);
        if (db.getTableCount(DatabaseHandler.TABLE_TASKER) > 0) {
            enableComponent(context, packageName, componentName);
            final Intent tasker = new Intent(context, TaskerService.class);
            tasker.setAction(TaskerService.ACTION_START);
            context.startService(tasker);
            Log.i("DeviceControl", "Service Started: " + componentName);
        } else {
            final Intent tasker = new Intent(context, TaskerService.class);
            tasker.setAction(TaskerService.ACTION_STOP);
            context.startService(tasker);
            disableComponent(context, packageName, componentName);
            Log.i("DeviceControl", "Service NOT Started: " + componentName);
        }
    }

}
