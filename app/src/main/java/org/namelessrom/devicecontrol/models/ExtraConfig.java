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

import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import timber.log.Timber;

/**
 * Extra configuration which auto serializes itself to a file
 */
public class ExtraConfig {
    private transient static final String NAME = "ExtraConfig";

    public transient static final String RNG_STARTUP = "rng_startup";

    public boolean rngStartup;

    public String uv;
    public String vdd;

    private transient static ExtraConfig instance;

    private ExtraConfig() { }

    public static ExtraConfig get() {
        if (instance == null) {
            final ExtraConfig config = new ExtraConfig();
            try {
                instance = Paper.book().read(NAME, config);
            } catch (PaperDbException pde) {
                instance = config;
                Timber.e(pde, "Could not read %s", NAME);
            }
        }
        return instance;
    }

    public ExtraConfig save() {
        try {
            Paper.book().write(NAME, ExtraConfig.this);
        } catch (PaperDbException pde) {
            Timber.e(pde, "Could not write %s", NAME);
        }
        return this;
    }

}
