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

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.services.BootupService;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Device configuration which auto serializes itself to a file
 */
public class DeviceConfiguration extends BaseConfiguration<DeviceConfiguration> {
    // TODO: get rid of all these strings after migration
    public static final String SHOW_LAUNCHER = "show_launcher";
    public static final String EXTENSIVE_LOGGING = "extensive_logging";
    public static final String DC_FIRST_START = "dc_first_start";
    public static final String SKIP_CHECKS = "skip_checks";
    public static final String SWIPE_ON_CONTENT = "swipe_on_content";
    public static final String DARK_THEME = "dark_theme";
    public static final String MONKEY = "monkey";
    public static final String SHOW_POLLFISH = "show_pollfish";
    public static final String CPU_LOCK_FREQ = "cpu_lock_freq";
    public static final String CPU_LOCK_GOV = "cpu_lock_gov";
    public static final String CPU_SHOW_INFO = "pref_show_cpu_info";

    public boolean dcFirstStart;
    public boolean swipeOnContent;

    public boolean sobDevice;
    public boolean sobCpu;
    public boolean sobGpu;
    public boolean sobExtras;
    public boolean sobSysctl;
    public boolean sobVoltage;

    public boolean darkTheme;
    public boolean showPollfish;

    public boolean showLauncher = true;
    public boolean skipChecks;
    public boolean extensiveLogging;

    public boolean perfCpuLock;
    public boolean perfCpuGovLock;
    public boolean perfCpuInfo;

    public boolean monkey;

    public int migrationLevel;

    private static final int MIGRATION_LEVEL_CURRENT = 1;

    private static DeviceConfiguration sInstance;

    private DeviceConfiguration(Context context) {
        loadConfiguration(context);
        migrateFromDatabase(context);
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

    @Override protected boolean migrateFromDatabase(Context context) {
        if (MIGRATION_LEVEL_CURRENT == migrationLevel) {
            Logger.i(this, "already up to date :)");
            return false;
        }

        dcFirstStart = PreferenceHelper.getBoolean(DC_FIRST_START, false);
        swipeOnContent = PreferenceHelper.getBoolean(SWIPE_ON_CONTENT, false);

        sobDevice = PreferenceHelper.getBoolean(BootupService.SOB_DEVICE, false);
        sobCpu = PreferenceHelper.getBoolean(BootupService.SOB_CPU, false);
        sobGpu = PreferenceHelper.getBoolean(BootupService.SOB_GPU, false);
        sobExtras = PreferenceHelper.getBoolean(BootupService.SOB_EXTRAS, false);
        sobSysctl = PreferenceHelper.getBoolean(BootupService.SOB_SYSCTL, false);
        sobVoltage = PreferenceHelper.getBoolean(BootupService.SOB_VOLTAGE, false);

        darkTheme = PreferenceHelper.getBoolean(DARK_THEME, false);
        showPollfish = PreferenceHelper.getBoolean(SHOW_POLLFISH, false);

        showLauncher = PreferenceHelper.getBoolean(SHOW_LAUNCHER, true);
        skipChecks = PreferenceHelper.getBoolean(SKIP_CHECKS, false);
        extensiveLogging = PreferenceHelper.getBoolean(EXTENSIVE_LOGGING, false);

        perfCpuLock = PreferenceHelper.getBoolean(CPU_LOCK_FREQ, false);
        perfCpuInfo = PreferenceHelper.getBoolean(CPU_SHOW_INFO, false);

        monkey = PreferenceHelper.getBoolean(MONKEY, false);

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public DeviceConfiguration loadConfiguration(Context context) {
        final DeviceConfiguration config = loadRawConfiguration(context, DeviceConfiguration.class);
        if (config == null) {
            return this;
        }

        this.dcFirstStart = config.dcFirstStart;
        this.swipeOnContent = config.swipeOnContent;

        this.sobDevice = config.sobDevice;
        this.sobCpu = config.sobCpu;
        this.sobGpu = config.sobGpu;
        this.sobExtras = config.sobExtras;
        this.sobSysctl = config.sobSysctl;
        this.sobVoltage = config.sobVoltage;

        this.darkTheme = config.darkTheme;
        this.showPollfish = config.showPollfish;

        this.showLauncher = config.showLauncher;
        this.skipChecks = config.skipChecks;
        this.extensiveLogging = config.extensiveLogging;

        this.perfCpuLock = config.perfCpuLock;
        this.perfCpuGovLock = config.perfCpuGovLock;
        this.perfCpuInfo = config.perfCpuInfo;

        this.monkey = config.monkey;

        this.migrationLevel = config.migrationLevel;

        return this;
    }

    @Override public DeviceConfiguration saveConfiguration(Context context) {
        saveConfigurationInternal(context);
        return this;
    }
}
