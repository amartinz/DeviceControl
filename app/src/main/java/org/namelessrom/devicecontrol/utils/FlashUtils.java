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

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Utilities for flashing shiny things
 */
public class FlashUtils implements DeviceConstants {

    private static final String CWM_PATH = "/cache/recovery/extendedcommand";
    private static final String OR_PATH  = "/cache/recovery/openrecoveryscript";

    @DebugLog private static String createOpenRecoveryScript(final List<String> files) {
        final StringBuilder sb = new StringBuilder();

        sb.append(String.format("echo \"set tw_signed_zip_verify 0\" > %s;\n", OR_PATH));

        for (String file : files) {
            if (file.startsWith("/sdcard")) file = getSdCardPath(file);
            sb.append(String.format("echo \"install %s\" >> %s;\n", file, OR_PATH));
        }
        sb.append(String.format("echo \"wipe cache\" >> %s;\n", OR_PATH));
        sb.append(Utils.setPermissions(OR_PATH, "0644", 0, 2001));

        return sb.toString();
    }

    @DebugLog private static String createCwmScript(final List<String> files) {
        final StringBuilder sb = new StringBuilder();

        for (String file : files) {
            if (file.startsWith("/sdcard")) file = getSdCardPath(file);
            sb.append(String.format("echo 'install_zip(\"%s\");' >> %s;\n", file, CWM_PATH));
        }
        sb.append(String.format("echo 'run_program(\"/sbin/busybox\", " +
                "\"rm\", \"-rf\", \"/cache/*\");' >> %s;\n", CWM_PATH));
        sb.append(Utils.setPermissions(CWM_PATH, "0644", 0, 2001));

        return sb.toString();
    }

    @DebugLog private static String getSdCardPath(String file) {
        if (!PreferenceHelper.getBoolean(PREF_FLASHER_MULTI_USER, false)) {
            file = file.replaceFirst("/sdcard", "/sdcard/0");
        }
        return file;
    }

    @DebugLog public static String triggerFlash(final List<String> files) {
        final StringBuilder sb = new StringBuilder();

        sb.append("mkdir -p /cache/recovery/;\n");

        final int flashType = PreferenceHelper.getInt(PREF_RECOVERY_TYPE, RECOVERY_TYPE_BOTH);
        if (RECOVERY_TYPE_CWM == flashType) {
            sb.append(createCwmScript(files));
        } else if (RECOVERY_TYPE_OPEN == flashType) {
            sb.append(createOpenRecoveryScript(files));
        } else {
            sb.append(createCwmScript(files));
            sb.append(createOpenRecoveryScript(files));
        }

        sb.append("reboot recovery;");

        final String cmd = sb.toString();
        Logger.v(FlashUtils.class, String.format("triggerUpdate():\n%s\n--------", cmd));
        Utils.runRootCommand(cmd);

        return cmd;
    }

}
