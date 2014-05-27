package org.namelessrom.devicecontrol.utils;

import java.util.Hashtable;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Helper class to get the content type of files via checking their extension
 */
public class ContentTypes {

    private final  Hashtable<String, String> mContentTypes;
    private static ContentTypes              contentTypes;

    private ContentTypes() {
        mContentTypes = new Hashtable<String, String>();
        mContentTypes.put("css", "text/css");
        mContentTypes.put("gzip", "application/x-gzip");
        mContentTypes.put("html", "text/html");
        mContentTypes.put("jpg", "image/jpeg");
        mContentTypes.put("js", "application/javascript");
        mContentTypes.put("json", "application/json");
        mContentTypes.put("mov", "video/quicktime");
        mContentTypes.put("mp4", "video/mp4");
        mContentTypes.put("pdf", "application/pdf");
        mContentTypes.put("png", "image/png");
        mContentTypes.put("txt", "text/plain");
        mContentTypes.put("wmv", "video/x-ms-wmv");
        mContentTypes.put("zip", "application/zip");
    }

    public static ContentTypes getInstance() {
        if (contentTypes == null) contentTypes = new ContentTypes();
        return contentTypes;
    }

    public String getContentType(final String path) {
        final String type = tryGetContentType(path);
        if (type != null) return type;
        return "text/plain";
    }

    private String tryGetContentType(final String path) {
        final int index = path.lastIndexOf(".");
        if (index != -1) {
            final String fileExtension = path.substring(index + 1);
            logDebug("fileExtension: " + fileExtension);
            final String ct = mContentTypes.get(fileExtension);
            if (ct != null) {return ct; }
        }
        return null;
    }

}
