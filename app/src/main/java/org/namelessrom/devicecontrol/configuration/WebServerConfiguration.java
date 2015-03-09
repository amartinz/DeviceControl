package org.namelessrom.devicecontrol.configuration;

import android.content.Context;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Tasker configuration which auto serializes itself to file
 */
public class WebServerConfiguration extends BaseConfiguration {
    public static final String ROOT = "wfm_root";
    public static final String PORT = "wfm_port";

    public static final String USE_AUTH = "wfm_auth";
    public static final String USERNAME = "wfm_username";
    public static final String PASSWORD = "wfm_password";

    public boolean root;
    public int port = 8080;

    public boolean useAuth;
    public String username = "root";
    public String password = "toor";

    public int migrationLevel;

    private static final int MIGRATION_LEVEL_CURRENT = 1;

    private static WebServerConfiguration sInstance;

    private WebServerConfiguration(Context context) {
        loadConfiguration(context);
        migrateFromDatabase(context);
    }

    public static WebServerConfiguration get(Context context) {
        if (sInstance == null) {
            sInstance = new WebServerConfiguration(context);
        }
        return sInstance;
    }

    @Override protected String getConfigurationFile() {
        return "webserver_configuration.json";
    }

    @Override protected boolean migrateFromDatabase(Context context) {
        if (MIGRATION_LEVEL_CURRENT == migrationLevel) {
            Logger.i(this, "already up to date :)");
            return false;
        }

        root = PreferenceHelper.getBoolean(ROOT, false);

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public void loadConfiguration(Context context) {
        final WebServerConfiguration config =
                (WebServerConfiguration) loadRawConfiguration(context,
                        WebServerConfiguration.class);
        if (config == null) {
            return;
        }

        this.root = config.root;

        this.migrationLevel = config.migrationLevel;
    }
}
