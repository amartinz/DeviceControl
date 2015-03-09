package org.namelessrom.devicecontrol.configuration;

import android.content.Context;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Tasker configuration which auto serializes itself to file
 */
public class TaskerConfiguration extends BaseConfiguration {
    public static final String USE_TASKER = "use_tasker";

    public static final String FSTRIM = "fstrim";
    public static final String FSTRIM_INTERVAL = "fstrim_interval";

    public boolean enabled;

    public boolean fstrimEnabled;
    public int fstrimInterval = 480;

    public int migrationLevel;

    private static final int MIGRATION_LEVEL_CURRENT = 1;

    private static TaskerConfiguration sInstance;

    private TaskerConfiguration(Context context) {
        loadConfiguration(context);
        migrateFromDatabase(context);
    }

    public static TaskerConfiguration get(Context context) {
        if (sInstance == null) {
            sInstance = new TaskerConfiguration(context);
        }
        return sInstance;
    }

    @Override protected String getConfigurationFile() {
        return "tasker_configuration.json";
    }

    @Override protected boolean migrateFromDatabase(Context context) {
        if (MIGRATION_LEVEL_CURRENT == migrationLevel) {
            Logger.i(this, "already up to date :)");
            return false;
        }

        enabled = PreferenceHelper.getBoolean(USE_TASKER);

        fstrimEnabled = PreferenceHelper.getBoolean(FSTRIM);
        fstrimInterval = PreferenceHelper.getInt(FSTRIM_INTERVAL, 480);

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public void loadConfiguration(Context context) {
        final TaskerConfiguration config =
                (TaskerConfiguration) loadRawConfiguration(context, TaskerConfiguration.class);
        if (config == null) {
            return;
        }

        this.enabled = config.enabled;

        this.fstrimEnabled = config.fstrimEnabled;
        this.fstrimInterval = config.fstrimInterval;

        this.migrationLevel = config.migrationLevel;
    }
}
