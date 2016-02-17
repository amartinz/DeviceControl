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

import android.content.Context;
import android.os.Environment;
import android.os.PowerManager;

import org.namelessrom.devicecontrol.utils.CustomTabsHelper;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;
import uk.co.senab.bitmapcache.BitmapLruCache;

@Module
public class AppModule {
    App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton App providesApplication() {
        return app;
    }

    @Provides @Singleton BitmapLruCache providesBitmapLruCache() {
        File cacheLocation = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(app.getExternalCacheDir(), "bitmapCache");

            // if we can not read or write, use the internal storage for caches
            try {
                if (!cacheLocation.canRead() || !cacheLocation.canWrite()) {
                    cacheLocation = null;
                }
            } catch (SecurityException sex) {
                cacheLocation = null;
                Timber.e(sex, "can not read or write directory");
            }
        }

        if (cacheLocation == null) {
            cacheLocation = new File(app.getFilesDir(), "bitmapCache");
        }

        try {
            Timber.d("Setting up cache: %s\nNeed to create dirs: %s", cacheLocation.getAbsolutePath(), cacheLocation.mkdirs());
        } catch (SecurityException sex) {
            Timber.wtf(sex, "can not create directory");
        }

        final BitmapLruCache.Builder builder = new BitmapLruCache.Builder(app);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(0.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        return builder.build();
    }

    @Provides @Singleton CustomTabsHelper providesCustomTabsHelper() {
        return new CustomTabsHelper(app);
    }

    @Provides @Singleton PowerManager providesPowerManager() {
        return (PowerManager) app.getSystemService(Context.POWER_SERVICE);
    }

}
