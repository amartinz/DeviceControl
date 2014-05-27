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
        mContentTypes.put("3gp", "video/3gp");
        mContentTypes.put("7z", "application/x-7z-compressed");
        mContentTypes.put("aac", "audio/x-aac");
        mContentTypes.put("apk", "application/vnd.android.package-archive");
        mContentTypes.put("avi", "video/avi");
        mContentTypes.put("bin", "application/octet-stream");
        mContentTypes.put("bmp", "image/bmp");
        mContentTypes.put("bz", "application/x-bzip");
        mContentTypes.put("bz2", "application/x-bzip2");
        mContentTypes.put("css", "text/css");
        mContentTypes.put("deb", "application/x-debian-package");
        mContentTypes.put("doc", "application/msword");
        mContentTypes.put("dot", "application/msword");
        mContentTypes.put("exe", "application/octet-stream");
        mContentTypes.put("flv", "video/x-flv");
        mContentTypes.put("gif", "image/gif");
        mContentTypes.put("gz", "application/x-gzip");
        mContentTypes.put("gzip", "application/x-gzip");
        mContentTypes.put("htm", "text/html");
        mContentTypes.put("html", "text/html");
        mContentTypes.put("htmls", "text/html");
        mContentTypes.put("ico", "image/x-icon");
        mContentTypes.put("jpe", "image/jpeg");
        mContentTypes.put("jpeg", "image/jpeg");
        mContentTypes.put("jpg", "image/jpeg");
        mContentTypes.put("js", "application/javascript");
        mContentTypes.put("json", "application/json");
        mContentTypes.put("m4v", "video/x-m4v");
        mContentTypes.put("mov", "video/quicktime");
        mContentTypes.put("mp3", "audio/mpeg3");
        mContentTypes.put("mp4", "video/mp4");
        mContentTypes.put("mpeg", "video/mpeg");
        mContentTypes.put("ogg", "audio/ogg");
        mContentTypes.put("pdf", "application/pdf");
        mContentTypes.put("png", "image/png");
        mContentTypes.put("ppt", "application/powerpoint");
        mContentTypes.put("rar", "application/x-rar-compressed");
        mContentTypes.put("rss", "application/rss+xml");
        mContentTypes.put("rtf", "application/rtf");
        mContentTypes.put("shtml", "text/html");
        mContentTypes.put("swf", "application/x-shockwave-flash");
        mContentTypes.put("tar", "application/x-tar");
        mContentTypes.put("tgz", "application/x-compressed");
        mContentTypes.put("torrent", "application/x-bittorrent");
        mContentTypes.put("ttf", "application/x-font-ttf");
        mContentTypes.put("txt", "text/plain");
        mContentTypes.put("wav", "audio/wav");
        mContentTypes.put("webm", "video/webm");
        mContentTypes.put("wmv", "video/x-ms-wmv");
        mContentTypes.put("xhtml", "application/xhtml+xml");
        mContentTypes.put("xml", "application/rss+xml");
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
