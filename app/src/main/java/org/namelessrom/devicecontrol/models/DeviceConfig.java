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

import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import timber.log.Timber;

/**
 * Device configuration which auto serializes itself to a file
 */
public class DeviceConfig {
    private transient static final String NAME = "DeviceConfig";

    public static final int THEME_AUTO = 1;
    public static final int THEME_DAY = 2;
    public static final int THEME_NIGHT = 3;

    public boolean dcFirstStart;

    public int themeMode = THEME_NIGHT;
    public boolean showPollfish;

    public boolean skipChecks;

    public boolean perfCpuLock;
    public boolean perfCpuGovLock;
    public boolean perfCpuInfo;

    public boolean ignoreDialogWarningBusyBox;
    public boolean ignoreDialogWarningRoot;
    public boolean ignoreDialogWarningSuVersion;

    public boolean expertMode;

    // TODO: readd
    public String suShellContext = "normal"; //Shell.CONTEXT_NORMAL;

    public int appVersion;

    private transient static DeviceConfig instance;

    private DeviceConfig() {
        ensureDefaults();
    }

    public static DeviceConfig get() {
        if (instance == null) {
            final DeviceConfig config = new DeviceConfig();
            try {
                instance = Paper.book().read(NAME, config);
            } catch (PaperDbException pde) {
                instance = config;
                Timber.e(pde, "Could not read %s", NAME);
            }
        }
        return instance;
    }

    public DeviceConfig save() {
        try {
            Paper.book().write(NAME, DeviceConfig.this);
        } catch (PaperDbException pde) {
            Timber.e(pde, "Could not write %s", NAME);
        }
        return this;
    }

    private void ensureDefaults() {
        if (TextUtils.isEmpty(suShellContext)) {
            // TODO: readd
            suShellContext = "normal";//Shell.CONTEXT_NORMAL;
        }
    }

}
