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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;

import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.configuration.BootupConfiguration;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.configuration.ExtraConfiguration;
import org.namelessrom.devicecontrol.configuration.FlasherConfiguration;
import org.namelessrom.devicecontrol.configuration.TaskerConfiguration;
import org.namelessrom.devicecontrol.configuration.WebServerConfiguration;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.wizard.AddTaskActivity;

import java.io.File;

public class Application extends android.app.Application {

    public static final Handler HANDLER = new Handler();

    public static boolean IS_NAMELESS = false;

    private static Application sInstance;

    private static int sAccentColor = -1;
    private static int sPrimaryColor = -1;
    private static int sIsDarkTheme = -1;

    @Override public void onCreate() {
        super.onCreate();
        Application.sInstance = this;

        // force enable logger until we hit the user preference
        Logger.setEnabled(true);

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

        // initialize database
        DatabaseHandler.getInstance();

        // load configurations
        BootupConfiguration.get(this);
        DeviceConfiguration.get(this);
        ExtraConfiguration.get(this);
        FlasherConfiguration.get(this);
        TaskerConfiguration.get(this);
        WebServerConfiguration.get(this);

        Logger.setEnabled(DeviceConfiguration.get(this).extensiveLogging);

        dumpInformation();

        IS_NAMELESS = Utils.isNameless();
        Logger.v(this, String.format("is nameless: %s", IS_NAMELESS));

        final boolean showLauncher = !Application.IS_NAMELESS
                || DeviceConfiguration.get(this).showLauncher;
        toggleLauncherIcon(showLauncher);
    }

    private void dumpInformation() {
        if (!Logger.getEnabled()) {
            return;
        }

        final Resources res = getResources();
        final int gmsVersion = res.getInteger(R.integer.google_play_services_version);
        Logger.i(this, "Google Play Services -> %s", gmsVersion);
    }


    @Override public void onTerminate() {
        // do some placebo :P
        DatabaseHandler.tearDown();
        super.onTerminate();
    }

    public static Application get() { return Application.sInstance; }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + Application.get().getPackageName();
        }
    }

    public void toggleLauncherIcon(final boolean showLauncher) {
        if (Application.IS_NAMELESS) {
            final String pkg = "com.android.settings";
            final Resources res;
            try {
                res = getPackageManager().getResourcesForApplication(pkg);
            } catch (PackageManager.NameNotFoundException exc) {
                Logger.e(this, "You dont have settings? That's weird.", exc);
                Utils.enableComponent(getPackageName(), DummyLauncher.class.getName());
                return;
            }

            if (!showLauncher
                    && (res != null && res.getIdentifier("device_control", "string", pkg) > 0)) {
                Logger.v(this, "Implemented into system and showLauncher is not set!");
                Utils.disableComponent(getPackageName(), DummyLauncher.class.getName());
            } else {
                Logger.v(this, "Implemented into system and showLauncher is set!");
                Utils.enableComponent(getPackageName(), DummyLauncher.class.getName());
            }
        } else {
            Logger.v(this, "Not implemented into system!");
            Utils.enableComponent(getPackageName(), DummyLauncher.class.getName());
        }
    }

    public int getColor(final int resId) {
        return getResources().getColor(resId);
    }

    public String[] getStringArray(final int resId) {
        return getResources().getStringArray(resId);
    }

    public int getAccentColor() {
        if (sAccentColor == -1) {
            sAccentColor = isDarkTheme()
                    ? getColor(R.color.accent)
                    : getColor(R.color.accent_light);
        }
        return sAccentColor;
    }

    public int getPrimaryColor() {
        if (sPrimaryColor == -1) {
            sPrimaryColor = isDarkTheme()
                    ? getColor(R.color.dark_primary_dark)
                    : getColor(R.color.light_primary_dark);
        }
        return sPrimaryColor;
    }

    public Application setAccentColor(final int color) {
        sAccentColor = color;
        return this;
    }

    public boolean isDarkTheme() {
        if (sIsDarkTheme == -1) {
            sIsDarkTheme = DeviceConfiguration.get(this).darkTheme ? 1 : 0;
        }
        return (sIsDarkTheme == 1);
    }

    public Application setDarkTheme(final boolean isDark) {
        sIsDarkTheme = isDark ? 1 : 0;
        sAccentColor = -1;
        sPrimaryColor = -1;

        DeviceConfiguration.get(this).darkTheme = isDark;
        DeviceConfiguration.get(this).saveConfiguration(this);
        return this;
    }
}
