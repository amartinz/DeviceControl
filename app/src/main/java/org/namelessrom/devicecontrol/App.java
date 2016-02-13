/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.utils.CustomTabsHelper;

import java.io.File;

import javax.inject.Inject;

import alexander.martinz.libs.execution.ShellManager;
import hugo.weaving.DebugLog;
import io.paperdb.Paper;
import uk.co.senab.bitmapcache.BitmapLruCache;

// XXX: DO NOT USE ROOT HERE! NEVER!
public class App extends android.app.Application {
    public static final Handler HANDLER = new Handler();

    private static App sInstance;

    private AppComponent appComponent;

    @Inject BitmapLruCache bitmapLruCache;
    @Inject CustomTabsHelper customTabsHelper;

    public static App get() {
        return App.sInstance;
    }

    public static App get(Context context) {
        return ((App) context.getApplicationContext());
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (bitmapLruCache != null) {
            bitmapLruCache.trimMemory();
        }
    }

    @Override public void onCreate() {
        super.onCreate();

        if (App.sInstance == null) {
            App.sInstance = this;

            buildComponentAndInject();

            // force enable logger until we hit the user preference
            Logger.setEnabled(true);
            ShellManager.enableDebug(true);

            Paper.init(this);

            AsyncTask.execute(new Runnable() {
                @Override public void run() {
                    setupEverythingAsync();
                }
            });
        }
    }

    @DebugLog public void buildComponentAndInject() {
        appComponent = AppComponent.Initializer.init(this);
        appComponent.inject(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public CustomTabsHelper getCustomTabsHelper() {
        return customTabsHelper;
    }

    @WorkerThread private void setupEverythingAsync() {
        final DeviceConfig deviceConfig = DeviceConfig.get();
        if (deviceConfig.debugStrictMode) {
            Logger.setStrictModeEnabled(true);
        }

        Logger.setEnabled(deviceConfig.extensiveLogging);
        ShellManager.enableDebug(deviceConfig.extensiveLogging);

        final String basePath = getFilesDirectory();
        final String[] dirList = new String[]{ basePath + DeviceConstants.DC_LOG_DIR };
        for (final String s : dirList) {
            final File dir = new File(s);
            if (!dir.exists()) {
                Logger.v(this, String.format("setupDirectories: creating %s -> %s", s, dir.mkdirs()));
            }
        }

        if (Logger.getEnabled()) {
            final Resources res = getResources();
            final int gmsVersion = res.getInteger(R.integer.google_play_services_version);
            Logger.i(this, "Google Play Services -> %s", gmsVersion);
        }
    }

    @SuppressLint("SdCardPath") public String getFilesDirectory() {
        final File tmp = getFilesDir();
        if (tmp != null && tmp.isDirectory()) {
            return tmp.getPath();
        } else {
            return "/data/data/" + getPackageName();
        }
    }

    public int getColorApplication(@ColorRes final int resId) {
        return ContextCompat.getColor(this, resId);
    }

    public Drawable getDrawableApplication(@DrawableRes final int resId) {
        return ContextCompat.getDrawable(this, resId);
    }

    public String[] getStringArray(final int resId) {
        return getResources().getStringArray(resId);
    }

}
