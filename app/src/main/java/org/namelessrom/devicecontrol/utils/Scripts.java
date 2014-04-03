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

import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Defines and runs Scripts.
 */
public class Scripts {

    public static boolean getForceNavBar() {
        return Utils.existsInBuildProp("qemu.hw.mainkeys=0");
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

    //

    public static boolean getForceHighEndGfx() {
        return Utils.existsInBuildProp("persist.sys.force_highendgfx=1");
    }

    public static String toggleForceHighEndGfx() {

        return ("if [ \"`grep 'persist\\.sys\\.force_highendgfx=1' /system/build.prop`\" ];" +
                "then /system/bin/mount -o remount,rw /system;" +
                "sed -i '/persist\\.sys\\.force_highendgfx=1/d' /system/build.prop;" +
                "/system/bin/mount -o remount,ro /system;" +
                "elif [ -z \"`grep 'persist\\.sys\\.force_highendgfx=1' /system/build.prop`\" ];" +
                "then /system/bin/mount -o remount,rw /system;" +
                "echo \"persist\\.sys\\.force_highendgfx=1\" >> /system/build.prop;" +
                "/system/bin/mount -o remount,ro /system;" +
                "fi");
    }
}
