/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.configuration;

import android.content.Context;
import android.text.TextUtils;

import com.stericson.roottools.execution.Shell;

/**
 * Device configuration which auto serializes itself to a file
 */
public class DeviceConfiguration extends BaseConfiguration<DeviceConfiguration> {
    public boolean dcFirstStart;
    public boolean swipeOnContent;

    public boolean darkTheme;
    public boolean showPollfish;

    public boolean skipChecks;
    public boolean debugStrictMode;
    public boolean extensiveLogging;

    public boolean perfCpuLock;
    public boolean perfCpuGovLock;
    public boolean perfCpuInfo;

    public boolean monkey;

    public String suShellContext;

    private static DeviceConfiguration sInstance;

    private DeviceConfiguration(Context context) {
        loadConfiguration(context);
        ensureDefaults();
    }

    public static DeviceConfiguration get(Context context) {
        if (sInstance == null) {
            sInstance = new DeviceConfiguration(context);
        }
        return sInstance;
    }

    @Override protected String getConfigurationFile() {
        return "device_configuration.json";
    }

    @Override public DeviceConfiguration loadConfiguration(Context context) {
        final DeviceConfiguration config = loadRawConfiguration(context, DeviceConfiguration.class);
        if (config == null) {
            return this;
        }

        this.dcFirstStart = config.dcFirstStart;
        this.swipeOnContent = config.swipeOnContent;

        this.darkTheme = config.darkTheme;
        this.showPollfish = config.showPollfish;

        this.skipChecks = config.skipChecks;
        this.debugStrictMode = config.debugStrictMode;
        this.extensiveLogging = config.extensiveLogging;

        this.perfCpuLock = config.perfCpuLock;
        this.perfCpuGovLock = config.perfCpuGovLock;
        this.perfCpuInfo = config.perfCpuInfo;

        this.monkey = config.monkey;

        this.suShellContext = config.suShellContext;

        return this;
    }

    private void ensureDefaults() {
        if (TextUtils.isEmpty(suShellContext)) {
            suShellContext = Shell.CONTEXT_NORMAL;
        }
    }

    @Override public DeviceConfiguration saveConfiguration(Context context) {
        saveConfigurationInternal(context);
        return this;
    }
}
