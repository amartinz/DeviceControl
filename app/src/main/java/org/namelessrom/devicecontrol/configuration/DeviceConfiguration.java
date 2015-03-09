package org.namelessrom.devicecontrol.configuration;

import android.content.Context;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.services.BootupService;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Device configuration which auto serializes itself to file
 */
public class DeviceConfiguration extends BaseConfiguration {
    public static final String SHOW_LAUNCHER = "show_launcher";
    public static final String EXTENSIVE_LOGGING = "extensive_logging";
    public static final String DC_FIRST_START = "dc_first_start";
    public static final String SKIP_CHECKS = "skip_checks";
    public static final String SWIPE_ON_CONTENT = "swipe_on_content";
    public static final String DARK_THEME = "dark_theme";

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
    public boolean perfCpuInfo;

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

        dcFirstStart = PreferenceHelper.getBoolean(DC_FIRST_START);
        swipeOnContent = PreferenceHelper.getBoolean(SWIPE_ON_CONTENT);

        sobDevice = PreferenceHelper.getBoolean(BootupService.SOB_DEVICE);
        sobCpu = PreferenceHelper.getBoolean(BootupService.SOB_CPU);
        sobGpu = PreferenceHelper.getBoolean(BootupService.SOB_GPU);
        sobExtras = PreferenceHelper.getBoolean(BootupService.SOB_EXTRAS);
        sobSysctl = PreferenceHelper.getBoolean(BootupService.SOB_SYSCTL);
        sobVoltage = PreferenceHelper.getBoolean(BootupService.SOB_VOLTAGE);

        darkTheme = PreferenceHelper.getBoolean(DARK_THEME);
        showPollfish = PreferenceHelper.getBoolean("show_pollfish");

        showLauncher = PreferenceHelper.getBoolean(SHOW_LAUNCHER, true);
        skipChecks = PreferenceHelper.getBoolean(SKIP_CHECKS);
        extensiveLogging = PreferenceHelper.getBoolean(EXTENSIVE_LOGGING);

        perfCpuLock = PreferenceHelper.getBoolean("cpu_lock_freq");
        perfCpuInfo = PreferenceHelper.getBoolean("pref_show_cpu_info");

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public void loadConfiguration(Context context) {
        final DeviceConfiguration config =
                (DeviceConfiguration) loadRawConfiguration(context, DeviceConfiguration.class);
        if (config == null) {
            return;
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
        this.perfCpuInfo = config.perfCpuInfo;

        this.migrationLevel = config.migrationLevel;
    }
}
