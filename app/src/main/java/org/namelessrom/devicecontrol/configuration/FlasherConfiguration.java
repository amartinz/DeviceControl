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
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

/**
 * Tasker configuration which auto serializes itself to file
 */
public class FlasherConfiguration extends BaseConfiguration {
    public static final String PREF_RECOVERY_TYPE = "pref_recovery_type";
    public static final int RECOVERY_TYPE_BOTH = 0;
    public static final int RECOVERY_TYPE_CWM = 1;
    public static final int RECOVERY_TYPE_OPEN = 2;

    public int recoveryType;

    public int migrationLevel;

    private static final int MIGRATION_LEVEL_CURRENT = 1;

    private static FlasherConfiguration sInstance;

    private FlasherConfiguration(Context context) {
        loadConfiguration(context);
        migrateFromDatabase(context);
    }

    public static FlasherConfiguration get(Context context) {
        if (sInstance == null) {
            sInstance = new FlasherConfiguration(context);
        }
        return sInstance;
    }

    @Override protected String getConfigurationFile() {
        return "flasher_configuration.json";
    }

    @Override protected boolean migrateFromDatabase(Context context) {
        if (MIGRATION_LEVEL_CURRENT == migrationLevel) {
            Logger.i(this, "already up to date :)");
            return false;
        }

        recoveryType = PreferenceHelper.getInt(PREF_RECOVERY_TYPE, RECOVERY_TYPE_OPEN);

        // always bump if we need to further migrate
        migrationLevel = MIGRATION_LEVEL_CURRENT;

        saveConfiguration(context);
        return true;
    }

    @Override public void loadConfiguration(Context context) {
        final FlasherConfiguration config =
                (FlasherConfiguration) loadRawConfiguration(context, FlasherConfiguration.class);
        if (config == null) {
            return;
        }

        this.recoveryType = config.recoveryType;

        this.migrationLevel = config.migrationLevel;
    }
}
