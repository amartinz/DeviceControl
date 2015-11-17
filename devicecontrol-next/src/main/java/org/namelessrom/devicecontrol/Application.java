/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.f2prateek.rx.preferences.RxSharedPreferences;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.namelessrom.devicecontrol.execution.ShellWriter;

import java.io.File;

import alexander.martinz.libs.logger.Logger;
import hugo.weaving.DebugLog;
import rx.functions.Action1;
import uk.co.senab.bitmapcache.BitmapLruCache;

@ReportsCrashes(
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUri = Constants.ACRA_REPORT_URL,
        formUriBasicAuthLogin = Constants.ACRA_REPORT_USER,
        formUriBasicAuthPassword = Constants.ACRA_REPORT_PASS,
        // work around OkHTTP bug, which will crash when sending a crash report on shutdown.
        // this is caused by the switch to HttpURLConnection for 6.0
        sendReportsAtShutdown = false
)
public class Application extends android.app.Application {
    private static final String TAG = Application.class.getSimpleName();

    public static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private CustomTabsHelper mCustomTabsHelper;

    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    private BitmapLruCache mBitmapCache;

    public CustomTabsHelper getCustomTabsHelper() {
        return mCustomTabsHelper;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public BitmapLruCache getBitmapCache() {
        return mBitmapCache;
    }

    @Override public void onLowMemory() {
        super.onLowMemory();
        if (mBitmapCache != null) {
            mBitmapCache.trimMemory();
        }
    }

    @Override public void onCreate() {
        super.onCreate();

        // initialize crash reporting
        ACRA.init(this);

        // enable until we hit the preference
        Logger.setEnabled(true);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final RxSharedPreferences rxPreferences = RxSharedPreferences.create(sharedPreferences);

        rxPreferences.getBoolean(getString(R.string.pref_enable_logger), BuildConfig.DEBUG).asObservable()
                .subscribe(new Action1<Boolean>() {
                    @Override public void call(Boolean enableLogger) {
                        Logger.setEnabled(enableLogger);
                        Logger.d(this, "Logger enabled -> %s", Logger.getEnabled());

                        if (Logger.getEnabled()) {
                            testShellWriter();
                        }
                    }
                });
        rxPreferences.getBoolean(getString(R.string.pref_cache_external), true).asObservable().subscribe(this::buildCache);

        // set up request queue for volley
        mRequestQueue = Volley.newRequestQueue(this);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);

            @Override public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });

        mCustomTabsHelper = new CustomTabsHelper(getApplicationContext());
    }

    @DebugLog private void buildCache(final boolean useExternal) {
        final File cacheLocation;
        if (useExternal && Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(getExternalFilesDir(null), "bitmapCache");
        } else {
            cacheLocation = new File(getFilesDir(), "bitmapCache");
        }
        //noinspection ResultOfMethodCallIgnored
        cacheLocation.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder(this);
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize(0.25f);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        mBitmapCache = builder.build();
    }

    private void testShellWriter() {
        final File testFile = new File(getApplicationContext().getFilesDir(), "testShellWriter");
        try {
            //noinspection ResultOfMethodCallIgnored
            testFile.createNewFile();
        } catch (Exception ignored) { }
        ShellWriter.with()
                .write("test")
                .into(testFile)
                .start((success) -> Logger.v(TAG, "Could write to %s -> %s", testFile.getAbsolutePath(), success));

        final String KMSG_PATH = "/dev/kmsg";
        ShellWriter.with()
                .disableRoot() // expected to fail to write to /dev/kmsg without root
                .write(String.format("%s: %s", TAG, "this is a test without root"))
                .into(KMSG_PATH)
                .start((success) -> Logger.v(this, "Could write to %s without root -> %s", KMSG_PATH, success));

        ShellWriter.with()
                .write(String.format("%s: %s", TAG, "this is a test as root"))
                .into(KMSG_PATH)
                .start((success) -> Logger.v(this, "Could write to %s with root -> %s", KMSG_PATH, success));
    }
}
