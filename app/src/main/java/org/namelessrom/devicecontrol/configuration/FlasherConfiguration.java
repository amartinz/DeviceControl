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

/**
 * Flasher configuration which auto serializes itself to a file
 */
public class FlasherConfiguration extends BaseConfiguration<FlasherConfiguration> {
    public static final String PREF_RECOVERY_TYPE = "pref_recovery_type";
    public static final int RECOVERY_TYPE_BOTH = 0;
    public static final int RECOVERY_TYPE_CWM = 1;
    public static final int RECOVERY_TYPE_OPEN = 2;

    public int recoveryType;

    private static FlasherConfiguration sInstance;

    private FlasherConfiguration(Context context) {
        loadConfiguration(context);
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

    @Override public FlasherConfiguration loadConfiguration(Context context) {
        final FlasherConfiguration config =
                loadRawConfiguration(context, FlasherConfiguration.class);
        if (config == null) {
            return this;
        }

        this.recoveryType = config.recoveryType;

        return this;
    }

    @Override public FlasherConfiguration saveConfiguration(Context context) {
        saveConfigurationInternal(context);
        return this;
    }
}
