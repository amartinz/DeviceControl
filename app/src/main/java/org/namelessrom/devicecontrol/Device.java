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
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.namelessrom.devicecontrol.utils.Utils;

import alexander.martinz.libs.hardware.device.KernelInfo;
import alexander.martinz.libs.hardware.device.MemoryInfo;
import alexander.martinz.libs.hardware.device.ProcessorInfo;

public class Device extends alexander.martinz.libs.hardware.device.Device {
    @SerializedName("kernelinfo") public KernelInfo kernelInfo;
    @SerializedName("memoryinfo") public MemoryInfo memoryInfo;
    @SerializedName("processorinfo") public ProcessorInfo processorInfo;

    @SerializedName("has_busybox") public boolean hasBusyBox;
    @SerializedName("su_version") public String suVersion;

    private static Device sInstance;

    private Device(@NonNull Context context) {
        super(context);

        KernelInfo.feedWithInformation(context, kernelInfoListener);
        MemoryInfo.feedWithInformation(context, MemoryInfo.TYPE_MB, memoryInfoListener);
        ProcessorInfo.feedWithInformation(context, processorInfoListener);
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
        suVersion = hasRoot ? Utils.getRootShellResult("su -v", "-") : "-";

        // TODO: readd check
        hasBusyBox = true;
        // check busybox
        //hasBusyBox = ShellManager.isBusyboxAvailable();

        // update memory as cached / free may change
        MemoryInfo.feedWithInformation(mContext, MemoryInfo.TYPE_MB, memoryInfoListener);

        return this;
    }

    private final KernelInfoListener kernelInfoListener = new KernelInfoListener() {
        @Override public void onKernelInfoAvailable(@NonNull KernelInfo kernelInfo) {
            Device.this.kernelInfo = kernelInfo;
        }
    };

    private final MemoryInfoListener memoryInfoListener = new MemoryInfoListener() {
        @Override public void onMemoryInfoAvailable(@NonNull MemoryInfo memoryInfo) {
            Device.this.memoryInfo = memoryInfo;
        }
    };

    private final ProcessorInfoListener processorInfoListener = new ProcessorInfoListener() {
        @Override public void onProcessorInfoAvailable(@NonNull ProcessorInfo processorInfo) {
            Device.this.processorInfo = processorInfo;
        }
    };

    @Override public String toString() {
        return new Gson().toJson(this, Device.class);
    }
}
