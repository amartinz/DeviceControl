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

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
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

public class Device extends alexander.martinz.libs.hardware.device.Device {
    @SerializedName("cpuinfo") public final CpuInfo cpuInfo;
    @SerializedName("kernelinfo") public final KernelInfo kernelInfo;
    @SerializedName("memoryinfo") public final MemoryInfo memoryInfo;

    @SerializedName("has_busybox") public boolean hasBusyBox;
    @SerializedName("su_version") public String suVersion;

    private static Device sInstance;

    private Device(@NonNull Context context) {
        super(context);

        cpuInfo = new CpuInfo();
        cpuInfo.feedWithInformation();

        kernelInfo = new KernelInfo();
        kernelInfo.feedWithInformation();

        memoryInfo = new MemoryInfo();
        memoryInfo.feedWithInformation(MemoryInfo.TYPE_MB);
    }

    public static Device get(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new Device(context);
        }
        return sInstance;
    }

    public Device update() {
        super.update();

        // get su version
        suVersion = hasRoot ? Utils.getCommandResult("su -v", "-") : "-";

        // check busybox
        hasBusyBox = RootTools.isBusyboxAvailable();

        // update memory as cached / free may change
        memoryInfo.feedWithInformation(MemoryInfo.TYPE_MB);

        return this;
    }

    @Override public String toString() {
        return new Gson().toJson(this, Device.class);
    }
}
