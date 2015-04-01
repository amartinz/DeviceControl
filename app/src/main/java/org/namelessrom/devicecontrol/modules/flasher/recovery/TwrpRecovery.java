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

import org.namelessrom.devicecontrol.utils.IOUtils;

import java.util.ArrayList;
import java.util.List;

public class TwrpRecovery extends RecoveryInfo {

    public TwrpRecovery() {
        super();

        setId(RecoveryInfo.TWRP_BASED);
        setName("twrp");
        setInternalSdcard("sdcard");
        setExternalSdcard("external_sd");
    }

    @Override
    public String getCommandsFile() {
        return "openrecoveryscript";
    }

    @Override
    public String[] getCommands(String[] items, boolean wipeData, boolean wipeCaches,
            String backupFolder, String backupOptions) {
        final List<String> commands = new ArrayList<>();
        commands.add("set tw_signed_zip_verify 0");

        if (backupFolder != null) {
            String str = "backup ";
            if (backupOptions != null) {
                if (backupOptions.contains("S")) {
                    str += "S";
                }
                if (backupOptions.contains("D")) {
                    str += "D";
                }
                if (backupOptions.contains("C")) {
                    str += "C";
                }
                if (backupOptions.contains("R")) {
                    str += "R";
                }
                if (backupOptions.contains("B")) {
                    str += "B";
                }
                if (backupOptions.contains("A") && IOUtils.get().hasAndroidSecure()) {
                    str += "A";
                }
                if (backupOptions.contains("E") && IOUtils.get().hasSdExt()) {
                    str += "E";
                }
            }
            commands.add(str + "O " + backupFolder);
        }

        if (wipeData) {
            commands.add("wipe data");
        }
        if (wipeCaches) {
            commands.add("wipe cache");
            commands.add("wipe dalvik");
        }

        for (final String item : items) {
            commands.add("install " + item);
        }

        return commands.toArray(new String[commands.size()]);

    }
}
