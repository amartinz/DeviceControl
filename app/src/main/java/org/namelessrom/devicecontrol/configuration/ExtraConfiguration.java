package org.namelessrom.devicecontrol.configuration;

import android.content.Context;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Tasker configuration which auto serializes itself to file
 */
public class ExtraConfiguration extends BaseConfiguration {
    public static final String RNG_STARTUP = "rng_startup";

    public boolean rngStartup;

    public String uv;
    public String vdd;

    public int migrationLevel;

    private static final int MIGRATION_LEVEL_CURRENT = 1;

    private static ExtraConfiguration sInstance;

    private ExtraConfiguration(Context context) {
        loadConfiguration(context);
        migrateFromDatabase(context);
    }

    public static ExtraConfiguration get(Context context) {
        if (sInstance == null) {
            sInstance = new ExtraConfiguration(context);
        }
        return sInstance;
    }

    @Override protected String getConfigurationFile() {
        return "extra_configuration.json";
    }

    @Override protected boolean migrateFromDatabase(Context context) {
        if (MIGRATION_LEVEL_CURRENT == migrationLevel) {
            Logger.i(this, "already up to date :)");
            return false;
        }

        rngStartup = PreferenceHelper.getBoolean(RNG_STARTUP, false);

        uv = "";
        vdd = "";

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public void loadConfiguration(Context context) {
        final ExtraConfiguration config =
                (ExtraConfiguration) loadRawConfiguration(context, ExtraConfiguration.class);
        if (config == null) {
            return;
        }

        this.rngStartup = config.rngStartup;

        this.uv = config.uv;
        this.vdd = config.vdd;

        this.migrationLevel = config.migrationLevel;
    }
}
