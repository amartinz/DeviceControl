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

import java.io.File;
import java.util.Hashtable;

import timber.log.Timber;

/**
 * Helper class to get the content type of files via checking their extension
 */
public class ContentTypes {
    private static ContentTypes contentTypes;

    private final Hashtable<String, String> contentTypeTable;

    private ContentTypes() {
        contentTypeTable = new Hashtable<>();
        contentTypeTable.put("3gp", "video/3gp");
        contentTypeTable.put("7z", "application/x-7z-compressed");
        contentTypeTable.put("aac", "audio/x-aac");
        contentTypeTable.put("apk", "application/vnd.android.package-archive");
        contentTypeTable.put("avi", "video/avi");
        contentTypeTable.put("bin", "application/octet-stream");
        contentTypeTable.put("bmp", "image/bmp");
        contentTypeTable.put("bz", "application/x-bzip");
        contentTypeTable.put("bz2", "application/x-bzip2");
        contentTypeTable.put("css", "text/css");
        contentTypeTable.put("deb", "application/x-debian-package");
        contentTypeTable.put("doc", "application/msword");
        contentTypeTable.put("dot", "application/msword");
        contentTypeTable.put("exe", "application/octet-stream");
        contentTypeTable.put("flv", "video/x-flv");
        contentTypeTable.put("gif", "image/gif");
        contentTypeTable.put("gz", "application/x-gzip");
        contentTypeTable.put("gzip", "application/x-gzip");
        contentTypeTable.put("htm", "text/html");
        contentTypeTable.put("html", "text/html");
        contentTypeTable.put("htmls", "text/html");
        contentTypeTable.put("ico", "image/x-icon");
        contentTypeTable.put("jpe", "image/jpeg");
        contentTypeTable.put("jpeg", "image/jpeg");
        contentTypeTable.put("jpg", "image/jpeg");
        contentTypeTable.put("js", "application/javascript");
        contentTypeTable.put("json", "application/json");
        contentTypeTable.put("m4v", "video/x-m4v");
        contentTypeTable.put("mov", "video/quicktime");
        contentTypeTable.put("mp3", "audio/mpeg3");
        contentTypeTable.put("mp4", "video/mp4");
        contentTypeTable.put("mpeg", "video/mpeg");
        contentTypeTable.put("ogg", "audio/ogg");
        contentTypeTable.put("pdf", "application/pdf");
        contentTypeTable.put("png", "image/png");
        contentTypeTable.put("ppt", "application/powerpoint");
        contentTypeTable.put("rar", "application/x-rar-compressed");
        contentTypeTable.put("rss", "application/rss+xml");
        contentTypeTable.put("rtf", "application/rtf");
        contentTypeTable.put("shtml", "text/html");
        contentTypeTable.put("swf", "application/x-shockwave-flash");
        contentTypeTable.put("tar", "application/x-tar");
        contentTypeTable.put("tgz", "application/x-compressed");
        contentTypeTable.put("torrent", "application/x-bittorrent");
        contentTypeTable.put("ttf", "application/x-font-ttf");
        contentTypeTable.put("txt", "text/plain");
        contentTypeTable.put("wav", "audio/wav");
        contentTypeTable.put("webm", "video/webm");
        contentTypeTable.put("wmv", "video/x-ms-wmv");
        contentTypeTable.put("xhtml", "application/xhtml+xml");
        contentTypeTable.put("xml", "application/rss+xml");
        contentTypeTable.put("zip", "application/zip");
    }

    public static ContentTypes getInstance() {
        if (contentTypes == null) {
            contentTypes = new ContentTypes();
        }
        return contentTypes;
    }

    @NonNull public String getContentType(final String path) {
        final String type = tryGetContentType(path);
        if (type != null) {
            return type;
        }
        return "text/plain";
    }

    @Nullable private String tryGetContentType(final String path) {
        final int index = path.lastIndexOf(".");
        if (index != -1) {
            final String fileExtension = path.substring(index + 1);
            Timber.v("fileExtension: %s", fileExtension);
            final String ct = contentTypeTable.get(fileExtension);
            if (ct != null) {
                return ct;
            }
        }
        return null;
    }

    public static boolean isFiletypeMatching(@NonNull final File file, @Nullable final String fileType) {
        return isFiletypeMatching(file.getName(), fileType);
    }

    public static boolean isFiletypeMatching(@Nullable final String name, @Nullable final String fileType) {
        if (TextUtils.isEmpty(fileType) || TextUtils.isEmpty(name)) {
            return true;
        }
        final String[] tmpString = name.split("\\.");
        final String tmp = tmpString.length > 0 ? tmpString[tmpString.length - 1] : null;
        return (tmp != null && tmp.equals(fileType));
    }

}
