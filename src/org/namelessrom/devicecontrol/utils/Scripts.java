/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

import java.util.List;

import eu.chainfire.libsuperuser.Application;
import eu.chainfire.libsuperuser.Shell;

import static org.namelessrom.devicecontrol.utils.Utils.logDebug;

/**
 * Defines and runs Scripts.
 */
public class Scripts {

    public static List<String> runScript(String mCommands) {
        List<String> tmpList;

        if (Application.IS_SYSTEM_APP) {
            logDebug("runScript: SH", Application.IS_LOG_DEBUG);
            tmpList = Shell.SH.run(mCommands);
        } else {
            logDebug("runScript: SU", Application.IS_LOG_DEBUG);
            tmpList = Shell.SU.run(mCommands);
        }

        // System app fallback, in case we need to enforce root
        if (tmpList.get(0) == null) {
            logDebug("runScript: tmpList == null", Application.IS_LOG_DEBUG);
            tmpList = Shell.SU.run(mCommands);
        }

        logDebug("runScript: tmpList.get(0) == " + tmpList.get(0), Application.IS_LOG_DEBUG);
        return tmpList;
    }

    //

    public static boolean getForceNavBar() {
        boolean tmpBool = false;
        List<String> tmpList;

        tmpList = Scripts.runScript(
                "if [ \"`grep 'qemu\\.hw\\.mainkeys\\=0' /system/build.prop`\" ];" +
                        "then echo \"1\";" +
                        "elif [ -z \"`grep 'qemu\\.hw\\.mainkeys\\=0' /system/build.prop`\" ];" +
                        "then echo \"0\";" +
                        "fi");

        if (!(tmpList.get(0) == null)) {
            logDebug("getForceNavBar: tmpList.get(0) == " + tmpList.get(0),
                    Application.IS_LOG_DEBUG);
            tmpBool = tmpList.get(0).equals("1");
        }

        logDebug("getForceNavBar: tmpBool == " + (tmpBool ? "true" : "false"),
                Application.IS_LOG_DEBUG);
        return tmpBool;
    }

    public static String toggleForceNavBar() {

        return ("if [ \"`grep 'qemu\\.hw\\.mainkeys\\=0' /system/build.prop`\" ];" +
                "then /system/bin/mount -o remount,rw /system;" +
                "sed -i '/qemu\\.hw\\.mainkeys\\=0/d' /system/build.prop;" +
                "/system/bin/mount -o remount,ro /system;" +
                "elif [ -z \"`grep 'qemu\\.hw\\.mainkeys\\=0' /system/build.prop`\" ];" +
                "then /system/bin/mount -o remount,rw /system;" +
                "echo \"qemu.hw.mainkeys=0\" >> /system/build.prop;" +
                "/system/bin/mount -o remount,ro /system;" +
                "fi");
    }
}
