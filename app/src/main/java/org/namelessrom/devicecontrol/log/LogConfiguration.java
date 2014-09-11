package org.namelessrom.devicecontrol.log;

import java.io.Serializable;

/**
 * Created by alex on 11.09.14.
 */
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
