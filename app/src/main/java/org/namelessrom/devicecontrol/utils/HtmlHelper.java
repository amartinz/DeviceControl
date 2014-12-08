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

import org.namelessrom.devicecontrol.Logger;

import java.net.URLDecoder;
import java.net.URLEncoder;

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

}
