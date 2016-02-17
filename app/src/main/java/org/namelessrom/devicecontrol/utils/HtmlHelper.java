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
package org.namelessrom.devicecontrol.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.App;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

import timber.log.Timber;

/**
 * Easier formatting of HTML pages
 */
public class HtmlHelper {

    public static String urlDecode(final String s) {
        String decoded;
        try {
            decoded = URLDecoder.decode(s, "UTF-8");
        } catch (Exception ignored) {
            //noinspection deprecation
            decoded = URLDecoder.decode(s);
        }
        return decoded;
    }

    public static String urlEncode(final String s) {
        String encoded;
        try {
            encoded = URLEncoder.encode(s, "UTF-8");
        } catch (Exception ignored) {
            //noinspection deprecation
            encoded = URLEncoder.encode(s);
        }
        return encoded;
    }

    @NonNull private static String cleanupPath(final String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        if (path.startsWith("./")) {
            return path.replaceFirst("./", "");
        }
        if (path.startsWith("/")) {
            return path.replaceFirst("/", "");
        }
        return path;
    }

    @Nullable public static InputStream loadPath(String path) {
        path = cleanupPath(path);
        return loadPathInternal(path);
    }

    @NonNull public static String loadPathAsString(String path) {
        path = cleanupPath(path);
        return loadPathAsStringInternal(path);
    }

    @Nullable private static InputStream loadPathInternal(final String path) {
        try {
            return App.get().getAssets().open(path);
        } catch (Exception exc) {
            Timber.e(exc, "loadPathInternal");
        }
        return null;
    }

    @NonNull private static String loadPathAsStringInternal(final String path) {
        try {
            return Utils.loadFromAssets(path);
        } catch (Exception exc) {
            Timber.e(exc, "loadPathAsStringInternal");
        }
        return "";
    }

}
