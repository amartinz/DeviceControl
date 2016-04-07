/*
 * Copyright 2014 ParanoidAndroid Project
 * Modifications Copyright (C) 2014 - 2015 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.modules.flasher.recovery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CwmBasedRecovery extends RecoveryInfo {

    public CwmBasedRecovery(Context context) {
        super();

        setId(RecoveryInfo.CWM_BASED);
        setName("cwmbased");
        setInternalSdcard(internalStorage());
        setExternalSdcard(externalStorage(context));
    }

    @Override
    public String getCommandsFile() {
        return "extendedcommand";
    }

    @Override
    public String[] getCommands(String[] items, boolean wipeData, boolean wipeCaches,
            String backupFolder, String backupOptions) {
        final List<String> commands = new ArrayList<>();
        final String internalStorage = getInternalSdcard();

        if (!TextUtils.isEmpty(backupFolder)) {
            commands.add("assert(backup_rom(\"/data/media/clockworkmod/backup/"
                    + backupFolder + "\"));");
        }

        if (wipeData) {
            commands.add("format(\"/data\");");
            commands.add(String.format("format(\"%s/.android_secure\");", internalStorage));
        }
        if (wipeCaches) {
            commands.add("format(\"/cache\");");
            commands.add("format(\"/data/dalvik-cache\");");
            commands.add("format(\"/cache/dalvik-cache\");");
            commands.add("format(\"/sd-ext/dalvik-cache\");");
        }

        if (items.length > 0) {
            if (IOUtils.get().isExternalStorageAvailable()) {
                commands.add("run_program(\"/sbin/mount\", \"" + getExternalSdcard() + "\");");
            }
            for (final String item : items) {
                commands.add("assert(install_zip(\"" + item + "\"));");
            }
        }

        return commands.toArray(new String[commands.size()]);
    }

    @SuppressLint("SdCardPath")
    private String internalStorage() {
        if (Environment.getExternalStorageDirectory() == null) {
            return "sdcard";
        }
        final String sdcard = "/sdcard";
        String path, dirPath;
        dirPath = path = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath = replace(replace(replace(dirPath, "/mnt/sdcard", sdcard), "/mnt/emmc", "/emmc"),
                path, sdcard);

        final String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if ((emulatedStorageTarget != null) && (path.startsWith(emulatedStorageTarget))) {
            final String number = path.replace(emulatedStorageTarget, "");
            dirPath = replace(dirPath, sdcard, sdcard + number);
        }

        final String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
        if (emulatedStorageSource != null) {
            dirPath = replace(dirPath, emulatedStorageSource, "/data/media");
        }

        if (emulatedStorageTarget == null && emulatedStorageSource == null
                && "/storage/sdcard0".equals(path) && sdcard.equals(dirPath)) {
            dirPath = path;
        }
        return dirPath;
    }

    private String externalStorage(final Context context) {
        final StorageManager storageManager =
                (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

        final String primaryVolumePath = primaryVolumePath(storageManager);
        final String[] volumePaths = volumePaths(storageManager);
        final ArrayList<String> volumePathsList = new ArrayList<>();
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();

        final int i = volumePaths == null ? 0 : volumePaths.length;
        for (int j = 0; j < i; j++) {
            String volumePath = volumePaths[j];

            if ((volumePath.equals(System.getenv("EMULATED_STORAGE_SOURCE")))
                    || (volumePath.equals(System.getenv("EXTERNAL_STORAGE")))
                    || (volumePath.equals(path))
                    || (volumePath.equals(primaryVolumePath))
                    || (volumePath.toLowerCase().contains("usb"))) {
                continue;
            }
            volumePathsList.add(volumePath);
        }
        if (volumePathsList.size() == 1) {
            return volumePathsList.get(0);
        }
        return null;
    }

    private String[] volumePaths(final StorageManager storageManager) {
        try {
            return (String[]) storageManager.getClass()
                    .getMethod("getVolumePaths", new Class[0])
                    .invoke(storageManager);
        } catch (Exception ex) {
            Timber.e(ex, "error getting volume paths");
            return null;
        }
    }

    private String primaryVolumePath(final StorageManager storageManager) {
        try {
            Object localObject = storageManager.getClass()
                    .getMethod("getPrimaryVolume", new Class[0])
                    .invoke(storageManager);
            return (String) localObject.getClass().getMethod("getPath", new Class[0])
                    .invoke(localObject);
        } catch (Exception ex) {
            Timber.e(ex, "error getting primary volume path");
            return null;
        }
    }

    private String replace(String original, String starts, String replace) {
        return !original.startsWith(starts)
                ? original
                : replace + original.substring(starts.length());
    }

}
