/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2013 Alexander "Evisceration" Martinz
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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

public class Utils implements DeviceConstants {

    /**
     * Reads a single line from a file.
     *
     * @param sFile The files to read from.
     * @return The read string OR null if not existing.
     */
    public static String readOneLine(String sFile) {
        String sLine = null;
        // don't even bother reading nothing, return null
        if (fileExists(sFile)) {
            if (Application.IS_SYSTEM_APP) {
                BufferedReader brBuffer;
                try {
                    brBuffer = new BufferedReader(new FileReader(sFile), 512);
                    try {
                        sLine = brBuffer.readLine();
                    } finally {
                        brBuffer.close();
                    }
                } catch (Exception e) {
                    return readFileViaShell(sFile);
                }
            } else {
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
    private static String readFileViaShell(String filePath) {
        List<String> mResult = Shell.SU.run("cat " + filePath);
        if (mResult != null) {
            if (mResult.size() != 0) {
                return mResult.get(0);
            } else {
                return "";
            }
        }
        return "";
    }

    /**
     * Write a string value to the specified file.
     *
     * @param filename The filename
     * @param value    The value
     */
    public static boolean writeValue(String filename, String value) {
        // the existence of the file is a requirement for the success ;)
        boolean success = fileExists(filename);
        if (success) {
            try {
                FileWriter fw = new FileWriter(filename);
                try {
                    fw.write(value);
                } finally {
                    fw.close();
                }
            } catch (IOException e) {
                logDebug("writeValue: writing failed: " + e.getMessage(),
                        Application.IS_LOG_DEBUG);
                success = writeValueViaShell(filename, value);
            }
        }

        return success;
    }

    /**
     * Fallback if everything fails
     *
     * @param filename The file to write
     * @param value    The value to write
     */
    private static boolean writeValueViaShell(String filename, String value) {
        boolean success = false;
        List<String> tmpList = Shell.SU.run("busybox echo " + value + " > " + filename);

        // if we dont get any result, it means it works, else we got a "permission denied"
        // message and thus it didnt succeed
        if (tmpList != null) {
            if (tmpList.size() == 0) success = true;
        }

        return success;
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
        logDebug("fileExists: " + (isExisting ? "true" : "false"), Application.IS_LOG_DEBUG);
        return isExisting;
    }

    /**
     * Restart the activity smoothly
     *
     * @param activity The activity to restart
     */
    public static void restartActivity(final Activity activity) {
        if (activity == null)
            return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    /**
     * Logs a message to logcat if boolean param is true.<br />
     * This is very usefull for debugging, just set debug to false on a release build<br />
     * and it wont show any debug messages.
     *
     * @param msg   The message to log
     * @param debug If true it logs, else not
     */
    public static void logDebug(String msg, boolean debug) {
        if (debug) {
            Log.e(TAG, msg);
        }
    }

    /**
     * Setup the directories for JF Control
     */
    public static void setupDirectories() {
        File dir;
        String[] dirList = new String[]{JfDirectories.JF_DATA_DIR, JfDirectories.JF_LOG_DIR,
                JfDirectories.JF_BACKUP_DIR};
        for (String s : dirList) {
            dir = new File(s);
            if (!dir.exists()) {
                logDebug("setupDirectories: creating " + s, Application.IS_LOG_DEBUG);
                final boolean isSuccess = dir.mkdirs();
                logDebug("setupDirectories: " + (isSuccess ? "true" : "false"),
                        Application.IS_LOG_DEBUG);
            }
        }
    }

    public static void createFiles(Context context) {
        if (!new File(context.getFilesDir() + "/utils").exists()) {
            get_assetsScript("utils", context, "", "");
            if (Application.IS_SYSTEM_APP) {
                Shell.SH.run("busybox chmod 750 " + context.getFilesDir() + "/utils");
            } else {
                Shell.SU.run("busybox chmod 750 " + context.getFilesDir() + "/utils");
            }
        }
    }

    private static String getBinPath(String b) {
        List<String> tmpList = Shell.SH.run("busybox which " + b);
        if (tmpList != null) {
            if (tmpList.size() > 0) {
                return tmpList.get(0);
            }
        }
        logDebug("getBinPath: found binary at: " + b, Application.IS_LOG_DEBUG);
        return "";
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
        });
        alertDialog.show();
    }
}
