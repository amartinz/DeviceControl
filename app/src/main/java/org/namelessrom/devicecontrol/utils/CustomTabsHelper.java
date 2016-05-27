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

package org.namelessrom.devicecontrol.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static android.support.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION;

public class CustomTabsHelper {
    private CustomTabsClient mClient;
    private CustomTabsSession mSession;

    public CustomTabsHelper(@NonNull final Context context) {
        try {
            tryConnectToService(context);
        } catch (IllegalArgumentException exc) {
            Timber.w(exc, "Could not connect to CustomTabs!");
        }
    }

    private void tryConnectToService(Context context) throws IllegalArgumentException {
        final ArrayList<ResolveInfo> customTabsPackages = getCustomTabsPackages(context);
        final String packageName;
        if (customTabsPackages.isEmpty()) {
            packageName = context.getPackageName();
        } else {
            packageName = customTabsPackages.get(0).activityInfo.packageName;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CustomTabsClient.bindCustomTabsService(context, packageName, mCustomTabsServiceConnection);
        }
    }

    private final CustomTabsServiceConnection mCustomTabsServiceConnection = new CustomTabsServiceConnection() {
        @Override public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
            mClient = client;
            mSession = mClient.newSession(new CustomTabsCallback());
        }

        @Override public void onServiceDisconnected(ComponentName name) {
            mSession = null;
            mClient = null;
        }
    };

    public boolean warmup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }
        if (mClient == null) {
            return false;
        }

        Timber.d("warming up -> %s", mClient.warmup(0));
        return true;
    }

    public boolean mayLaunchUrl(@NonNull final String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }
        if (mSession == null) {
            return false;
        }

        Timber.d("may launch url -> %s", mSession.mayLaunchUrl(Uri.parse(url), null, null));
        return true;
    }

    public void launchUrl(@NonNull final Activity activity, @NonNull final String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            try {
                createBuilder(activity).build().launchUrl(activity, Uri.parse(url));
                return;
            } catch (ActivityNotFoundException | IllegalArgumentException | SecurityException exc) {
                Timber.w(exc, "Could not launch url via CustomTabs!");
            }
        }

        AppHelper.viewInBrowser(activity, url);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private CustomTabsIntent.Builder createBuilder(@NonNull final Activity activity) {
        return new CustomTabsIntent.Builder(mSession)
                .enableUrlBarHiding()
                .setToolbarColor(AppResources.get(activity).getPrimaryColor())
                .setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Returns a list of packages that support Custom Tabs.
     */
    private static ArrayList<ResolveInfo> getCustomTabsPackages(Context context) {
        final ArrayList<ResolveInfo> packagesSupportingCustomTabs = new ArrayList<>();

        final PackageManager pm = context.getPackageManager();
        final Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://amartinz.at"));

        final List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        for (ResolveInfo info : resolvedActivityList) {
            final String packageName = info.activityInfo.packageName;
            switch (packageName) {
                case "com.android.chrome":
                case "com.chrome.beta":
                case "com.chrome.dev": {
                    break;
                }
                default: {
                    continue;
                }
            }

            final Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info);
            }
        }
        return packagesSupportingCustomTabs;
    }
}
