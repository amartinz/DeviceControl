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
 * Extra configuration which auto serializes itself to a file
 */
public class ExtraConfiguration extends BaseConfiguration<ExtraConfiguration> {
    public static final String RNG_STARTUP = "rng_startup";

    public boolean rngStartup;

    public String uv;
    public String vdd;

    private static ExtraConfiguration sInstance;

    private ExtraConfiguration(Context context) {
        loadConfiguration(context);
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

    @Override public ExtraConfiguration loadConfiguration(Context context) {
        final ExtraConfiguration config = loadRawConfiguration(context, ExtraConfiguration.class);
        if (config == null) {
            return this;
        }

        this.rngStartup = config.rngStartup;

        this.uv = config.uv;
        this.vdd = config.vdd;

        return this;
    }

    @Override public ExtraConfiguration saveConfiguration(Context context) {
        saveConfigurationInternal(context);
        return this;
    }
}
