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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.cmdprocessor.CMDProcessor;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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
    public static void writeValue(String filename, String value) {
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
            } catch (IOException e) {
                logDebug("writeValue: writing failed: " + e.getMessage());
                writeValueViaShell(filename, value);
            }
        }
    }

    /**
     * Fallback if everything fails
     *
     * @param filename The file to write
     * @param value    The value to write
     */
    private static void writeValueViaShell(String filename, String value) {
        runRootCommand("busybox echo " + value + " > " + filename);
    }

    /**
     * Write the "color value" to the specified file. The value is scaled from
     * an integer to an unsigned integer by multiplying by 2.
     *
     * @param filename The filename
     * @param value    The value of max value Integer.MAX
     */
    public static void writeColor(String filename, int value) {
        writeValue(filename, String.valueOf((long) value * 2));
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
     * Restart the activity smoothly
     *
     * @param activity The activity to restart
     */
    public static void restartActivity(final Activity activity) {
        if (activity == null) { return; }
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    /**
     * Setup the directories for JF Control
     */
    public static void setupDirectories() {
        File dir;
        String[] dirList = new String[]{DC_DATA_DIR, DC_LOG_DIR, DC_BACKUP_DIR};
        for (String s : dirList) {
            dir = new File(s);
            if (!dir.exists()) {
                logDebug("setupDirectories: creating " + s);
                final boolean isSuccess = dir.mkdirs();
                logDebug("setupDirectories: " + (isSuccess ? "true" : "false"));
            }
        }
    }

    public static void createFiles(Context context, boolean force) {
        final String filepath = context.getFilesDir().getPath() + "/utils";
        logDebug("createFiles path: " + filepath);
        if (!new File(filepath).exists() || force) {
            RootTools.installBinary(context, R.raw.utils, "utils");
            logDebug("createFiles installed: utils");
        }
    }

    public static String getBinPath(String b) {
        final CMDProcessor.CommandResult2 cr =
                new CMDProcessor().sh.runWaitFor("busybox which " + b);
        if (cr.success()) {
            return cr.stdout;
        } else {
            return "";
        }
    }

    private static void get_assetsScript(String fn, Context c, String prefix, String postfix) {
        byte[] buffer;
        final AssetManager assetManager = c.getAssets();
        try {
            InputStream f = assetManager.open(fn);
            buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            final String s = new String(buffer);
            final StringBuilder sb = new StringBuilder(s);
            if (!postfix.equals("")) {
                sb.append("\n\n").append(postfix);
            }
            if (!prefix.equals("")) {
                sb.insert(0, prefix + "\n");
            }
            sb.insert(0, "#!" + getBinPath("sh") + "\n\n");
            try {
                FileOutputStream fos;
                fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
                fos.write(sb.toString().getBytes());
                fos.close();

            } catch (IOException e) {
                Log.d(TAG, "error write " + fn + " file");
                e.printStackTrace();
            }

        } catch (IOException e) {
            Log.d(TAG, "error read " + fn + " file");
            e.printStackTrace();
        }
    }

    public static void showDialog(Context ctx, String title, String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                }
        );
        alertDialog.show();
    }

    public static boolean getLowRamDevice(Context context) {
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
     * @param fname
     * @return string array
     */
    public static String[] readStringArray(String fname) {
        String line = readOneLine(fname);
        if (line != null) {
            return line.split(" ");
        }
        return null;
    }

    public static void setPermissions(String file) {
        if (new File(file).exists()) {
            runRootCommand("chmod 655 " + file);
        }
    }


    public static void runRootCommand(String command) {
        final CommandCapture comm = new CommandCapture(0, false, command);
        try {
            RootTools.getShell(true).add(comm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getWriteCommand(final String path, final String value) {
        final String cmd = String.format("busybox echo \"%s\" > %s;\n", value, path);
        logDebug("WriteCommand: " + cmd);
        return cmd;
    }

}
