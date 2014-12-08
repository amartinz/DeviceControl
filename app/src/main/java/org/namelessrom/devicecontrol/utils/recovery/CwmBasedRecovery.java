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

package org.namelessrom.devicecontrol.utils.recovery;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import org.namelessrom.devicecontrol.utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

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
    public String[] getCommands(Context context, String[] items, String[] originalItems,
            boolean wipeData, boolean wipeCaches, String backupFolder, String backupOptions) {

        List<String> commands = new ArrayList<>();

        int size = items.length, i = 0;

        String internalStorage = getInternalSdcard();

        if (backupFolder != null) {
            commands.add("assert(backup_rom(\"/data/media/clockworkmod/backup/"
                    + backupFolder + "\"));");
        }

        if (wipeData) {
            commands.add("format(\"/data\");");
            commands.add("format(\"" + internalStorage + "/.android_secure\");");
        }
        if (wipeCaches) {
            commands.add("format(\"/cache\");");
            commands.add("format(\"/data/dalvik-cache\");");
            commands.add("format(\"/cache/dalvik-cache\");");
            commands.add("format(\"/sd-ext/dalvik-cache\");");
        }

        if (size > 0) {
            if (IOUtils.get().isExternalStorageAvailable()) {
                commands.add("run_program(\"/sbin/mount\", \"" + getExternalSdcard() + "\");");
            }
            for (; i < size; i++) {
                commands.add("assert(install_zip(\"" + items[i] + "\"));");
            }
        }

        return commands.toArray(new String[commands.size()]);
    }

    private String internalStorage() {
        if (Environment.getExternalStorageDirectory() == null) {
            return "sdcard";
        }
        String path, dirPath;
        dirPath = path = Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath = replace(
                replace(replace(dirPath, "/mnt/sdcard", "/sdcard"), "/mnt/emmc", "/emmc"), path,
                "/sdcard");
        if (Build.VERSION.SDK_INT > 16) {
            String emulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
            if ((emulatedStorageTarget != null) && (path.startsWith(emulatedStorageTarget))) {
                String number = path.replace(emulatedStorageTarget, "");
                dirPath = replace(dirPath, "/sdcard", "/sdcard" + number);
            }
            String emulatedStorageSource = System.getenv("EMULATED_STORAGE_SOURCE");
            if (emulatedStorageSource != null) {
                dirPath = replace(dirPath, emulatedStorageSource, "/data/media");
            }
            if (emulatedStorageTarget == null && emulatedStorageSource == null
                    && "/storage/sdcard0".equals(path) && "/sdcard".equals(dirPath)) {
                dirPath = path;
            }
        } else if (dirPath.startsWith("/mnt/emmc")) {
            dirPath = "emmc";
        }
        return dirPath;
    }

    private String externalStorage(Context paramContext) {
        String dirPath = null;
        try {
            String[] volumePaths = null;
            ArrayList<String> volumePathsList = null;
            String path = null;
            if (Build.VERSION.SDK_INT >= 14) {
                volumePaths = volumePaths(paramContext);
                if (volumePaths != null) {
                    volumePathsList = new ArrayList<>();
                    path = Environment.getExternalStorageDirectory().getAbsolutePath();
                }
            }
            try {
                String primaryVolumePath = primaryVolumePath(paramContext);
                int i = volumePaths.length;
                for (int j = 0; ; j++) {
                    if (j < i) {
                        String volumePath = volumePaths[j];
                        try {
                            if ((volumePath.equals(System.getenv("EMULATED_STORAGE_SOURCE")))
                                    || (volumePath.equals(System.getenv("EXTERNAL_STORAGE")))
                                    || (volumePath.equals(path))
                                    || (volumePath.equals(primaryVolumePath))
                                    || (volumePath.toLowerCase().contains("usb"))) { continue; }
                            volumePathsList.add(volumePath);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (volumePathsList.size() == 1) {
                            dirPath = (String) volumePathsList.get(0);
                        }
                        return dirPath;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dirPath;
    }

    private String[] volumePaths(Context context) {
        try {
            StorageManager localStorageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            return (String[]) (String[]) localStorageManager.getClass()
                    .getMethod("getVolumePaths", new Class[0])
                    .invoke(localStorageManager);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String primaryVolumePath(Context context) {
        try {
            StorageManager localStorageManager = (StorageManager) context
                    .getSystemService(Context.STORAGE_SERVICE);
            Object localObject = localStorageManager.getClass()
                    .getMethod("getPrimaryVolume", new Class[0])
                    .invoke(localStorageManager);
            return (String) localObject.getClass().getMethod("getPath", new Class[0])
                    .invoke(localObject);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String replace(String original, String starts, String replace) {
        return !original.startsWith(starts) ? original : replace
                + original.substring(starts.length());
    }

}
