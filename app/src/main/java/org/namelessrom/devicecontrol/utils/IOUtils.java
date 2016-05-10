/*
 * Copyright 2014 ParanoidAndroid Project
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

import at.amartinz.execution.RootShell;

public class IOUtils {
    public static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();

    private static IOUtils sInstance;

    private static boolean sSdcardsChecked;

    private String sPrimarySdcard;
    private String sSecondarySdcard;

    private IOUtils() {
        readMounts();
    }

    public static IOUtils get() {
        if (sInstance == null) {
            sInstance = new IOUtils();
        }
        return sInstance;
    }

    public boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    @NonNull public String getPrimarySdCard() {
        return sPrimarySdcard;
    }

    @Nullable public String getSecondarySdCard() {
        return sSecondarySdcard;
    }

    @SuppressLint("SdCardPath") private void readMounts() {
        if (sSdcardsChecked) {
            return;
        }

        ArrayList<String> mounts = new ArrayList<>();
        ArrayList<String> vold = new ArrayList<>();

        String result = RootShell.fireAndBlockString("cat /proc/mounts;");
        String[] output = ((result != null) ? result.split("\n") : new String[0]);
        for (final String s : output) {
            if (s.startsWith("/dev/block/vold/")) {
                String[] lineElements = s.split(" ");
                if (lineElements[1] == null) { continue; }
                String element = lineElements[1];
                mounts.add(element);
            }
        }

        boolean addExternal = mounts.size() == 1 && isExternalStorageAvailable();
        if (mounts.size() == 0 && addExternal) {
            mounts.add("/mnt/sdcard");
        }

        final File fstab = findFstab();
        if (fstab != null) {
            result = RootShell.fireAndBlockString(String.format("cat %s;", fstab.getAbsolutePath()));
            output = ((result != null) ? result.split("\n") : new String[0]);
            for (final String s : output) {
                //noinspection StatementWithEmptyBody
                if (TextUtils.isEmpty(s) || s.startsWith("#")) {
                    // do nothing
                } else if (s.startsWith("dev_mount")) {
                    String[] lineElements = s.split(" ");
                    if (lineElements.length < 3) { continue; }
                    String element = lineElements[2];
                    if (element == null) { continue; }

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    if (!element.toLowerCase().contains("usb")) {
                        vold.add(element);
                    }
                } else if (s.startsWith("/devices/platform")) {
                    String[] lineElements = s.split(" ");
                    if (lineElements.length < 2) { continue; }
                    String element = lineElements[1];
                    if (element == null) { continue; }

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    if (!element.toLowerCase().contains("usb")) {
                        vold.add(element);
                    }
                }
            }
        }

        if (addExternal && (vold.size() == 1 && isExternalStorageAvailable())) {
            mounts.add(vold.get(0));
        }

        if (vold.size() == 0 && isExternalStorageAvailable()) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount)
                || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (final String mount : mounts) {
            if (!mount.contains("sdcard0") && !mount.equalsIgnoreCase("/mnt/sdcard")
                && !mount.equalsIgnoreCase("/sdcard")) {
                sSecondarySdcard = mount;
            } else {
                sPrimarySdcard = mount;
            }
        }

        if (sPrimarySdcard == null) {
            sPrimarySdcard = "/sdcard";
        }

        sSdcardsChecked = true;
    }

    private File findFstab() {
        File file = new File("/system/etc/vold.fstab");
        if (file.exists()) {
            return file;
        }

        String fstab = RootShell.fireAndBlockString("grep -ls \"/dev/block/\" * --include=fstab.* --exclude=fstab.goldfish");
        if (!TextUtils.isEmpty(fstab)) {
            final String[] files = fstab.split("\n");
            for (final String s : files) {
                file = new File(s);
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }

    public double getSpaceLeft() {
        final StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        final double sdAvailSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            sdAvailSize = (double) stat.getAvailableBlocksLong() * (double) stat.getBlockSizeLong();
        } else {
            //noinspection deprecation
            sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        }
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    public boolean hasAndroidSecure() {
        return folderExists(SDCARD + "/.android-secure");
    }

    public boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    public boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}
