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

package org.namelessrom.devicecontrol.modules.flasher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseArray;

import org.namelessrom.devicecontrol.modules.flasher.recovery.CwmBasedRecovery;
import org.namelessrom.devicecontrol.modules.flasher.recovery.RecoveryInfo;
import org.namelessrom.devicecontrol.modules.flasher.recovery.TwrpRecovery;
import org.namelessrom.devicecontrol.utils.IOUtils;

public class RecoveryHelper {

    private SparseArray<RecoveryInfo> mRecoveries = new SparseArray<>();

    public RecoveryHelper(Context context) {
        mRecoveries.put(RecoveryInfo.CWM_BASED, new CwmBasedRecovery(context));
        mRecoveries.put(RecoveryInfo.TWRP_BASED, new TwrpRecovery());
    }

    public RecoveryInfo getRecovery(int id) {
        for (int i = 0; i < mRecoveries.size(); i++) {
            int key = mRecoveries.keyAt(i);
            RecoveryInfo info = mRecoveries.get(key);
            if (info.getId() == id) {
                return info;
            }
        }
        return null;
    }

    public String getCommandsFile(int id) {
        return getRecovery(id).getCommandsFile();
    }

    @SuppressLint("SdCardPath")
    public String getRecoveryFilePath(final int id, String filePath) {
        RecoveryInfo info = getRecovery(id);

        String internalStorage = info.getInternalSdcard();
        String externalStorage = info.getExternalSdcard();

        String primarySdcard = IOUtils.get().getPrimarySdCard();
        String secondarySdcard = IOUtils.get().getSecondarySdCard();

        String[] internalNames = new String[]{
                primarySdcard,
                "/mnt/sdcard",
                "/storage/sdcard/",
                "/sdcard",
                "/storage/sdcard0",
                "/storage/emulated/0"
        };
        String[] externalNames = new String[]{
                secondarySdcard == null ? " " : secondarySdcard,
                "/mnt/extSdCard",
                "/storage/extSdCard/",
                "/extSdCard",
                "/storage/sdcard1",
                "/storage/emulated/1"
        };
        for (int i = 0; i < internalNames.length; i++) {
            String internalName = internalNames[i];
            String externalName = externalNames[i];
            if (filePath.startsWith(externalName)) {
                filePath = filePath.replace(externalName, "/" + externalStorage);
                break;
            } else if (filePath.startsWith(internalName)) {
                filePath = filePath.replace(internalName, "/" + internalStorage);
                break;
            }
        }

        while (filePath.startsWith("//")) {
            filePath = filePath.substring(1);
        }

        return filePath;
    }

    public String[] getCommands(int id, String[] items, boolean wipeData, boolean wipeCache,
            String backupFolder, String backupOptions) {
        return getRecovery(id).getCommands(items, wipeData, wipeCache, backupFolder, backupOptions);
    }
}
