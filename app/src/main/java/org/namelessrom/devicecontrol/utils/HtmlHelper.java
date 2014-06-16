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

/**
 * Easier formatting of HTML pages
 */
public class HtmlHelper {

    public static String getHtmlContainer(final String title, final String body) {
        String html = "<!DOCTYPE html><html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, " +
                "maximum-scale=1, user-scalable=no\">" +
                "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">" +
                "<title>${title}</title><style>${css}</style>" +
                "</head>" +
                "<body role=\"document\" style=\"overflow-y:scroll;\">" +
                "${navigationbar}" +
                "<div class=\"container\" role=\"main\">" +
                "${body}" +
                "</div>" +
                loadFooter() +
                loadScripts() +
                "</body>" +
                "</html>";

        return html
                .replace("${css}", loadCss())
                .replace("${title}", title)
                .replace("${navigationbar}", loadNavigationBar())
                .replace("${body}", body);
    }

    public static String loadCss() {
        String css = "";
        try {
            css += Utils.loadFromAssets("css/bootstrap.min.css");
        } catch (Exception ignored) { }

        try {
            css += Utils.loadFromAssets("css/main.css");
        } catch (Exception ignored) { }

        return css;
    }

    public static String loadScripts() {
        String scripts = "";

        try {
            scripts += "<script>" + Utils.loadFromAssets("js/jquery.min.js") + "</script>";
        } catch (Exception ignored) { }

        try {
            scripts += "<script>" + Utils.loadFromAssets("js/bootstrap.min.js") + "</script>";
        } catch (Exception ignored) { }

        return scripts;
    }

    public static String loadNavigationBar() {
        String navbar = "";
        try {
            navbar += Utils.loadFromAssets("html/navbar.html");
        } catch (Exception ignored) { }

        return navbar;
    }

    public static String loadFooter() {
        String footer = "";
        try {
            footer += Utils.loadFromAssets("html/footer.html");
        } catch (Exception ignored) { }

        return footer;
    }

    public static String escapeHtml(final String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return Html.escapeHtml(html);
        } else {
            return TextUtils.htmlEncode(html);
        }
    }

    public static String getDirectoryLine(final String path, final String name) {
        return String.format("<li>Directory: <a href=\"/files%s\">%s</a></li>", path, name);
    }

    public static String getFileLine(final String path, final String name) {
        return String.format("<li>File: <a href=\"/files%s\">%s</a></li>", path, name);
    }

}
