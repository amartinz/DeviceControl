/*
 *  Copyright (C) 2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol;

import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.stericson.roottools.RootTools;

import org.namelessrom.devicecontrol.objects.CpuInfo;
import org.namelessrom.devicecontrol.objects.KernelInfo;
import org.namelessrom.devicecontrol.objects.MemoryInfo;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Device {
    @SerializedName("platform_version") public final String platformVersion;
    @SerializedName("platform_id") public final String platformId;
    @SerializedName("platform_type") public final String platformType;
    @SerializedName("platform_tags") public final String platformTags;
    @SerializedName("platform_build_date") public final String platformBuildType;

    @SerializedName("vm_library") public final String vmLibrary;
    @SerializedName("vm_version") public final String vmVersion;

    @SerializedName("screen_width") public final int screenWidth;
    @SerializedName("screen_height") public final int screenHeight;

    @SerializedName("is_64_bit") public final boolean deviceIs64Bit;
    @SerializedName("supported_abis") public final String deviceSupportedAbis;

    @SerializedName("android_id") public final String androidId;
    @SerializedName("manufacturer") public final String manufacturer;
    @SerializedName("model") public final String model;
    @SerializedName("product") public final String product;
    @SerializedName("board") public final String board;
    @SerializedName("bootloader") public final String bootloader;
    @SerializedName("radio_version") public final String radio;

    @SerializedName("cpuinfo") public final CpuInfo cpuInfo;
    @SerializedName("kernelinfo") public final KernelInfo kernelInfo;
    @SerializedName("memoryinfo") public final MemoryInfo memoryInfo;

    @SerializedName("has_busybox") public boolean hasBusyBox;
    @SerializedName("has_root") public boolean hasRoot;
    @SerializedName("su_version") public String suVersion;
    @SerializedName("selinux_enforcing") public boolean isSELinuxEnforcing;

    private static final Device sInstance = new Device();

    private Device() {
        platformVersion = Build.VERSION.RELEASE;
        platformId = Build.DISPLAY;
        platformType = Build.VERSION.CODENAME + " " + Build.TYPE;
        platformTags = Build.TAGS;
        platformBuildType = Utils.getDate(Build.TIME);

        vmVersion = System.getProperty("java.vm.version", "-");
        vmLibrary = getRuntime();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            deviceIs64Bit = Build.SUPPORTED_64_BIT_ABIS.length != 0;
            String abis = "";
            int length = Build.SUPPORTED_ABIS.length;
            for (int i = 0; i < length; i++) {
                abis += Build.SUPPORTED_ABIS[i];
                if (i + 1 != length) {
                    abis += ", ";
                }
            }
            deviceSupportedAbis = abis;
        } else {
            deviceIs64Bit = false;
            deviceSupportedAbis = String.format("%s, %s", Build.CPU_ABI, Build.CPU_ABI2);
        }

        final Resources res = Application.get().getResources();
        screenWidth = res.getDisplayMetrics().widthPixels;
        screenHeight = res.getDisplayMetrics().heightPixels;

        androidId = Utils.getAndroidId();
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        product = Build.PRODUCT;
        board = Build.BOARD;
        bootloader = Build.BOOTLOADER;
        radio = Build.getRadioVersion();

        // initialize defaults
        hasBusyBox = false;
        hasRoot = false;
        suVersion = "-";
        isSELinuxEnforcing = isSELinuxEnforcing(); // ehm, alright, if you say so...

        cpuInfo = new CpuInfo();
        cpuInfo.feedWithInformation();

        kernelInfo = new KernelInfo();
        kernelInfo.feedWithInformation();

        memoryInfo = new MemoryInfo();
        memoryInfo.feedWithInformation(MemoryInfo.TYPE_MB);
    }

    public static Device get() {
        return sInstance;
    }

    public Device update() {
        // check root for common locations, then via roottools and as last resort the SuperSU ones
        final boolean binaryExists = new File("/system/bin/su").exists()
                || new File("/system/xbin/su").exists()
                || RootTools.isRootAvailable()
                || new File("/system/bin/.ext/.su").exists()
                || new File("/system/xbin/sugote").exists();
        hasRoot = binaryExists && RootTools.isAccessGiven();

        // get su version
        suVersion = Utils.getCommandResult("su -v", "-");

        // check busybox
        hasBusyBox = RootTools.isBusyboxAvailable();

        // selinux can be toggled when in development mode, so do not cache it
        isSELinuxEnforcing = isSELinuxEnforcing(); // ehm, alright, if you say so...

        // update memory as cached / free may change
        memoryInfo.feedWithInformation(MemoryInfo.TYPE_MB);

        return this;
    }

    public List<String> deviceAbisAsList() {
        final ArrayList<String> list = new ArrayList<>();
        final String[] abis = deviceSupportedAbis.split(",");
        for (final String abi : abis) {
            list.add(abi.trim());
        }
        return list;
    }

    private String getRuntime() {
        // check the vm lib
        String tmp = Utils.getCommandResult("getprop persist.sys.dalvik.vm.lib.2", "-");
        if ("-".equals(tmp)) {
            // if we do not get a result, try falling back to the old property
            tmp = Utils.getCommandResult("getprop persist.sys.dalvik.vm.lib", "-");
        }

        if ("-".equals(tmp)) {
            // if we still did not get a result, lets cheat a bit.
            // we know that ART starts with vm version 2.x
            tmp = vmVersion.startsWith("1") ? "libdvm.so" : "libart.so";
        }

        final String runtime =
                "libdvm.so".equals(tmp) ? "Dalvik" : "libart.so".equals(tmp) ? "ART" : "-";
        tmp = String.format("%s (%s)", runtime, tmp);

        return tmp;
    }

    private boolean isSELinuxEnforcing() {
        // We know about a 4.2 release, which has enforcing selinux
        if (Build.VERSION.SDK_INT >= 17) {
            String enforcing = Utils.readOneLine("/sys/fs/selinux/enforce");

            // 4.4+ builds (should) be enforcing by default
            if (TextUtils.isEmpty(enforcing)) {
                isSELinuxEnforcing = (Build.VERSION.SDK_INT >= 19);
            } else {
                isSELinuxEnforcing = Utils.isEnabled(enforcing, false);
            }
        }

        return isSELinuxEnforcing;
    }

    @Override public String toString() {
        return new Gson().toJson(this, Device.class);
    }
}
