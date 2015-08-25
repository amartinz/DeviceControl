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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.utils.AppHelper;

import alexander.martinz.libs.logger.Logger;

public class CustomTabsHelper {
    private CustomTabsClient mClient;
    private CustomTabsSession mSession;

    public CustomTabsHelper(@NonNull final Context context) {
        final String packageName = context.getPackageName();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            CustomTabsClient.bindCustomTabsService(context, packageName, new CustomTabsServiceConnection() {
                @Override public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                    mClient = client;
                    mSession = mClient.newSession(new CustomTabsCallback());
                }

                @Override public void onServiceDisconnected(ComponentName name) {
                    mClient = null;
                }
            });
        }
    }

    public boolean warmup() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }
        if (mClient == null) {
            return false;
        }

        Logger.d(this, "warming up -> %s", mClient.warmup(0));
        return true;
    }

    public boolean mayLaunchUrl(@NonNull final String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false;
        }
        if (mSession == null) {
            return false;
        }

        Logger.d(this, "may launch url -> %s", mSession.mayLaunchUrl(Uri.parse(url), null, null));
        return true;
    }

    public void launchUrl(@NonNull final Activity activity, @NonNull final String url) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            AppHelper.viewInBrowser(activity, url);
            return;
        }
        createBuilder(activity).build().launchUrl(activity, Uri.parse(url));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private CustomTabsIntent.Builder createBuilder(@NonNull final Activity activity) {
        final CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(mSession);
        builder.setToolbarColor(ContextCompat.getColor(activity, R.color.primary));
        builder.setStartAnimations(activity, R.anim.slide_in_right, R.anim.slide_out_left);
        builder.setExitAnimations(activity, R.anim.slide_in_left, R.anim.slide_out_right);

        return builder;
    }
}
