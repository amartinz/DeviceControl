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

import android.os.Build;
import android.text.Html;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

/**
 * Easier formatting of HTML pages
 */
public class HtmlHelper {

    public static String escapeHtml(final String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return Html.escapeHtml(html);
        } else {
            return TextUtils.htmlEncode(html);
        }
    }

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

    public static String loadPath(String path) {
        if (path.startsWith("./")) {
            path = path.replaceFirst("./", "");
        }
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        try {
            return Utils.loadFromAssets(path);
        } catch (Exception exc) {
            Logger.e(HtmlHelper.class, "loadPath", exc);
        }
        return "";
    }

    public static String getBreadcrumbs(final String path) {
        Logger.v(HtmlHelper.class, String.format("getBreadcrumbs(): %s", path));
        return getBreadcrumbs(Arrays.asList(path.split("/")));
    }

    public static String getBreadcrumbs(final List<String> breadcrumbs) {
        final StringBuilder sb = new StringBuilder();
        String paths = "/files/";
        sb.append("<ol class=\"breadcrumb\">");
        sb.append(
                String.format("<li><a class=\"loadAsync\" href=\"%s\">%s</a></li>", paths, "Home"));
        for (final String s : breadcrumbs) {
            paths += (s + '/');
            sb.append(
                    String.format("<li><a class=\"loadAsync\" href=\"%s\">%s</a></li>", paths, s));
            Logger.v(HtmlHelper.class, String.format("s: %s | paths: %s", s, paths));
        }
        sb.append("</ol>");
        return sb.toString();
    }

}
