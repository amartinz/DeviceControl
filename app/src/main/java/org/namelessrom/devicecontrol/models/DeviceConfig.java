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

package org.namelessrom.devicecontrol.models;

import android.text.TextUtils;

import com.stericson.roottools.execution.Shell;

import io.paperdb.Paper;

/**
 * Device configuration which auto serializes itself to a file
 */
public class DeviceConfig {
    private transient static final String NAME = "DeviceConfig";

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

    public String suShellContext = Shell.CONTEXT_NORMAL;

    private transient static DeviceConfig instance;

    private DeviceConfig() {
        ensureDefaults();
    }

    public static DeviceConfig get() {
        if (instance == null) {
            instance = Paper.get(NAME, new DeviceConfig());
        }
        return instance;
    }

    public DeviceConfig save() {
        Paper.put(NAME, DeviceConfig.this);
        return this;
    }

    private void ensureDefaults() {
        if (TextUtils.isEmpty(suShellContext)) {
            suShellContext = Shell.CONTEXT_NORMAL;
        }
    }

}
