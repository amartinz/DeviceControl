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
package org.namelessrom.devicecontrol.log;

import java.io.Serializable;

public class LogConfiguration implements Serializable {

    public static final int TYPE_KERNEL_LOG_NONE      = 0;
    public static final int TYPE_KERNEL_LOG_LAST_KMSG = 1;
    public static final int TYPE_KERNEL_LOG_KMSG      = 2;
    public static final int TYPE_KERNEL_LOG_BOTH      = 3;

    public static final int TYPE_COMPRESSION_TAR    = 0;
    public static final int TYPE_COMPRESSION_TAR_GZ = 1;

    private int kernelLogType;

    private int logCompression;

    public LogConfiguration(final int kernelLogType, final int logCompression) {
        this.kernelLogType = kernelLogType;
        this.logCompression = logCompression;
    }

    public int getKernelLogType() {
        return kernelLogType;
    }

    public LogConfiguration setKernelLogType(final int kernelLogType) {
        this.kernelLogType = kernelLogType;
        return this;
    }

    public int getLogCompression() {
        return logCompression;
    }

    public LogConfiguration setLogCompression(final int logCompression) {
        this.logCompression = logCompression;
        return this;
    }
}
