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

/**
 * Flasher configuration which auto serializes itself to a file
 */
public class FlasherConfig {
    private transient static final String NAME = "FlasherConfig";

    public transient static final String PREF_RECOVERY_TYPE = "pref_recovery_type";
    public transient static final int RECOVERY_TYPE_BOTH = 0;
    public transient static final int RECOVERY_TYPE_CWM = 1;
    public transient static final int RECOVERY_TYPE_OPEN = 2;

    public int recoveryType;

    private transient static FlasherConfig instance;

    public static FlasherConfig get() {
        if (instance == null) {
            instance = Paper.get(NAME, new FlasherConfig());
        }
        return instance;
    }

    public FlasherConfig save() {
        Paper.put(NAME, FlasherConfig.this);
        return this;
    }

}
