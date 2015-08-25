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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.Application;

import alexander.martinz.libs.logger.Logger;

/**
 * Helper class for application interactions like cleaning the cache
 */
public class AppHelper {
    private static final String TAG = AppHelper.class.getSimpleName();

    public static void launchUrlViaTabs(@NonNull final Activity activity, @NonNull final String url) {
        final Application application = ((Application) activity.getApplicationContext());
        application.getCustomTabsHelper().launchUrl(activity, url);
    }

    public static void viewInBrowser(Context context, String url) {
        final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(i);
        } catch (Exception e) {
            Logger.e(AppHelper.class, "viewInBrowser", e);
        }
    }

}
