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
package org.namelessrom.devicecontrol;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;

import com.stericson.roottools.RootTools;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
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

    public static AlarmManager alarmManager;
    public static Context      applicationContext;

    public static final Handler HANDLER = new Handler();

    private static PackageManager packageManager;

    @Override public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        applicationContext = getApplicationContext();

        DatabaseHandler.getInstance();
        Logger.setEnabled(PreferenceHelper.getBoolean(EXTENSIVE_LOGGING, false));

        if (Utils.existsInFile(Scripts.BUILD_PROP, "ro.nameless.debug=1")) {
            // setup thread policy
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .detectCustomSlowCalls()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .penaltyFlashScreen()
                    .build());

            // setup vm policy
            final StrictMode.VmPolicy.Builder vmpolicy = new StrictMode.VmPolicy.Builder();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                vmpolicy.detectLeakedRegistrationObjects();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    vmpolicy.detectFileUriExposure();
                }
            }
            vmpolicy
                    .detectAll()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedSqlLiteObjects()
                    .setClassInstanceLimit(AddTaskActivity.class, 100)
                    .penaltyLog();
            StrictMode.setVmPolicy(vmpolicy.build());

            // enable debug mode at root tools
            RootTools.debugMode = true;
        }

        Logger.v(this, String.format("is nameless: %s", IS_NAMELESS));

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        packageManager = getPm();

        final boolean showLauncher =
                PreferenceHelper.getBoolean(SHOW_LAUNCHER, true) || !Application.IS_NAMELESS;
        toggleLauncherIcon(showLauncher);
    }


    @Override public void onTerminate() {
        // do some placebo :P
        DatabaseHandler.tearDown();
        super.onTerminate();
    }

    public static PackageManager getPm() {
        if (packageManager == null) {
            packageManager = Application.applicationContext.getPackageManager();
        }
        return packageManager;
    }

    public static File getFiles() { return Application.applicationContext.getFilesDir(); }

    @SuppressLint("SdCardPath") public static String getFilesDirectory() {
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
                if (!showLauncher && res != null && res.getIdentifier(
                        "device_control", "string", "com.android.settings") > 0) {
                    Logger.v(Application.class,
                            "Implemented into system and showLauncher is not set!");
                    Utils.disableComponent(getPkgName(), DummyLauncher.class.getName());
                } else {
                    Logger.v(Application.class, "Implemented into system and showLauncher is set!");
                    Utils.enableComponent(getPkgName(), DummyLauncher.class.getName());
                }
            } else {
                Logger.v(Application.class, "Not implemented into system!");
                Utils.enableComponent(getPkgName(), DummyLauncher.class.getName());
            }
        } catch (PackageManager.NameNotFoundException exc) {
            Logger.e(Application.class, "You dont have settings? That's weird.");
            Utils.enableComponent(getPkgName(), DummyLauncher.class.getName());
        }
    }

    public static String getPkgName() {
        if (applicationContext != null) return applicationContext.getPackageName();
        return "org.namelessrom.devicecontrol";
    }

    public static String getStr(final int resId) { return applicationContext.getString(resId); }

    public static String getStr(final int resId, final String... extras) {
        return applicationContext.getString(resId, (Object[]) extras);
    }

    public static String[] getStringArray(final int resId) {
        return applicationContext.getResources().getStringArray(resId);
    }

    public static int getColor(final int color) {
        return Application.applicationContext.getResources().getColor(color);
    }
}
