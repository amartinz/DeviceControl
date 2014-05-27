package org.namelessrom.devicecontrol.utils;

/**
 * Easier formatting of HTML pages
 */
public class HtmlHelper {

    public static String getHtmlContainer(final String title, final String body) {
        return String.format("<html><head><title>%s</title></head><body>%s</body></html>",
                title, body);
    }

}
