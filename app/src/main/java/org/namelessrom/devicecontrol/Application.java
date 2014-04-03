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
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import com.stericson.roottools.RootTools;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formKey = "",
        formUri = "http://reports.nameless-rom.org" +
                "/acra-devicecontrol/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "namelessreporter",
        formUriBasicAuthPassword = "weareopentoeveryone",
        mode = ReportingInteractionMode.SILENT)
public class Application extends android.app.Application implements DeviceConstants {

    public static final boolean IS_NAMELESS = Utils.isNameless();

    public static boolean IS_LOG_DEBUG = false;
    public static boolean IS_DEBUG     = false;
    public static boolean HAS_ROOT     = false;

    public static AlarmManager alarmManager;
    public static Context      applicationContext;

    private static PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);

        applicationContext = getApplicationContext();

        PreferenceHelper.getInstance(this);
        IS_LOG_DEBUG = PreferenceHelper.getBoolean(EXTENSIVE_LOGGING, false);
        IS_DEBUG = Utils.existsInBuildProp("ro.nameless.debug=1");

        if (Application.IS_DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

            RootTools.debugMode = true;
        }

        logDebug("Is Nameless: " + (IS_NAMELESS ? "true" : "false"));

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        packageManager = getPackageManager();

        // we need to detect SU for some features :)
        HAS_ROOT = RootTools.isRootAvailable() && RootTools.isAccessGiven();
        if (HAS_ROOT) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RootTools.remount(Environment.getExternalStorageDirectory()
                            .getAbsolutePath(), "rw");
                }
            }).start();
        }

        final boolean showLauncher =
                PreferenceHelper.getBoolean(SHOW_LAUNCHER, true) || !Application.IS_NAMELESS;
        toggleLauncherIcon(showLauncher);
    }

    public static void toggleLauncherIcon(final boolean showLauncher) {
        try {
            if (packageManager == null) { return; }
            if ((packageManager.getResourcesForApplication("com.android.settings")
                    .getIdentifier("device_control_settings", "string",
                            "com.android.settings") > 0)
                    && (!showLauncher && Application.IS_NAMELESS)) {
                logDebug("Implemented into system!");
                disableLauncher(true);
            } else {
                logDebug("Not implemented into system!");
                disableLauncher(false);
            }
        } catch (PackageManager.NameNotFoundException exc) {
            logDebug("You dont have settings? That's weird.");
            disableLauncher(false);
        }
    }

    private static void disableLauncher(boolean shouldDisable) {
        final ComponentName component = new ComponentName(PACKAGE_NAME,
                PACKAGE_NAME + ".activities.DummyLauncher");
        packageManager.setComponentEnabledSetting(component,
                (shouldDisable
                        ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
                PackageManager.DONT_KILL_APP
        );
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

    /**
     * You dont have to be a genius to break that.
     * Well, its a public key so the world doesnt die if someone gets it.
     * Still, add some pseudo obfuscation for proguard as well as make it
     * not just a copy-paste action to get it.
     */
    public static class Iab {
        private static final String IAB_PREF = "iab_pref";

        private static final String a = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgK";
        private static final String b = "CAQEAw20oxkT9x/QZJKYArXPneHGkFYmk0Hd5PI1A0R89Ns3GAKKp";
        private static final String c = "HTkTy2eLggt1bfUq67IXkNzv3/GNPrSypFvuaxW4RL/kX";
        private static final String d = "GWGffWgMm7ohoG1MQKmzLbrVP4MsQ9Gji2olo";
        private static final String e = "43B3K5+Oku0GzjZfj/BTWu0N";
        private static final String f = "MkxcPh9BIEaqwBLfwO81IFNBDnYjC";
        private static final String g = "+K64fpxvdWG0w3SrYQRFVYFVd5D3WgZtjMWHF22ehOt0wN8U7TsT";
        private static final String h = "f+fZV/XkZJVlE+P5onxqxaKUCqYZaNbXFKN/";
        private static final String i = "R+oT8ybucbRPjKv3knc5/BRw8JassdEoe";
        private static final String j = "xCfHhciU00K9UaD+D+n0TH9zDhfcduuzNfz4FQIDAQAB";

        public static String getKey() {
            String key = "<<";
            final StringBuilder sb = new StringBuilder(a + b);
            sb.append(c).append(d);
            sb.append(f).append(e);
            sb.append(g).append(h).append(i);
            sb.append(j);
            key = key + sb.toString() + ">1>";
            String tmp = key.replace("<<", "");
            key = tmp.replace(">" + String.valueOf(1) + ">", "");

            return key;
        }

        public static String getPref() { return IAB_PREF; }

    }
}
