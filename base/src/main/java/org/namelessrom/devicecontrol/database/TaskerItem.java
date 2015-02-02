/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.database;

import java.io.Serializable;

public class TaskerItem implements Serializable {

    public int id;
    public String category;
    public String name;
    public String trigger;
    public String value;
    public boolean enabled;

    public TaskerItem() { }

    public TaskerItem(final int id, final String category, final String name, final String trigger,
            final String value, final boolean enabled) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.value = value;
        this.trigger = trigger;
        this.enabled = enabled;
    }

    @Override public String toString() {
        return String.format(
                "id: %s | category: %s | name: %s | value: %s | trigger: %s | enabled: %s",
                id, category, name, value, trigger, enabled);
    }
}
