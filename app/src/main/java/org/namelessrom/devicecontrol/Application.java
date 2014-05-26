/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol;

import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;

import com.stericson.roottools.RootTools;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

import java.io.File;

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formKey = "",
        formUri = "https://reports.nameless-rom.org" +
                "/acra-devicecontrol/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "namelessreporter",
        formUriBasicAuthPassword = "weareopentoeveryone",
        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text,
        resDialogText = R.string.crash_dialog_text,
        resDialogOkToast = R.string.crash_dialog_ok_toast)
public class Application extends android.app.Application implements DeviceConstants {

    public static final boolean IS_NAMELESS = Utils.isNameless();

    public static boolean IS_LOG_DEBUG = false;

    public static AlarmManager alarmManager;
    public static Context      applicationContext;

    public static final Handler HANDLER = new Handler();

    private static PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        applicationContext = getApplicationContext();

        PreferenceHelper.getInstance(this);
        IS_LOG_DEBUG = PreferenceHelper.getBoolean(EXTENSIVE_LOGGING, false);

        if (Utils.existsInFile(Scripts.BUILD_PROP, "ro.nameless.debug=1")) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .detectCustomSlowCalls()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .detectActivityLeaks()
                    .detectFileUriExposure()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit(AddTaskActivity.class, 100)
                    .penaltyLog()
                    .build());

            RootTools.debugMode = true;
        }

        logDebug("Is Nameless: " + (IS_NAMELESS ? "true" : "false"));

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        packageManager = getPm();

        final boolean showLauncher =
                PreferenceHelper.getBoolean(SHOW_LAUNCHER, true) || !Application.IS_NAMELESS;
        toggleLauncherIcon(showLauncher);
    }

    public static PackageManager getPm() {
        if (packageManager == null) {
            packageManager = Application.applicationContext.getPackageManager();
        }
        return packageManager;
    }

    public static LayoutInflater getLayoutInflater() {
        return (LayoutInflater) Application.applicationContext.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    public static File getFiles() {
        return Application.applicationContext.getFilesDir();
    }

    public static String getFilesDirectory() {
        final File tmp = getFiles();
        if (tmp != null && tmp.exists()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + Application.applicationContext.getPackageName();
        }
    }

    public static void toggleLauncherIcon(final boolean showLauncher) {
        try {
            if (getPm() == null) { return; }
            if (Application.IS_NAMELESS) {
                final Resources res = getPm().getResourcesForApplication("com.android.settings");
                if (res != null
                        && res.getIdentifier("device_control_settings", "string",
                        "com.android.settings") > 0
                        && !showLauncher) {
                    logDebug("Implemented into system and showLauncher is not set!");
                    Utils.disableComponent(PACKAGE_NAME, ".DummyLauncher");
                } else {
                    logDebug("Implemented into system and showLauncher is set!");
                    Utils.enableComponent(PACKAGE_NAME, ".DummyLauncher");
                }
            } else {
                logDebug("Not implemented into system!");
                Utils.enableComponent(PACKAGE_NAME, ".DummyLauncher");
            }
        } catch (PackageManager.NameNotFoundException exc) {
            logDebug("You dont have settings? That's weird.");
            Utils.enableComponent(PACKAGE_NAME, ".DummyLauncher");
        }
    }

    /**
     * Logs a message to logcat if boolean param is true.<br />
     * This is very useful for debugging, just set debug to false on a release build<br />
     * and it wont show any debug messages.
     *
     * @param msg The message to log
     */
    public static void logDebug(final String msg) {
        if (IS_LOG_DEBUG) {
            Log.e("DeviceControl", msg);
        }
    }

}
