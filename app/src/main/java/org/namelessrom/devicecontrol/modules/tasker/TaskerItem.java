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
package org.namelessrom.devicecontrol.modules.tasker;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class TaskerItem implements Comparable<TaskerItem>, Serializable {
    public String category;
    public String name;
    public String trigger;
    public String value;
    public boolean enabled;

    public TaskerItem() { }

    public TaskerItem(final String category, final String name, final String trigger,
            final String value, final boolean enabled) {
        this.category = category;
        this.name = name;
        this.value = value;
        this.trigger = trigger;
        this.enabled = enabled;
    }

    @Override public boolean equals(Object otherItem) {
        return otherItem instanceof TaskerItem
                && this.trigger.equals(((TaskerItem) otherItem).trigger)
                && this.category.equals(((TaskerItem) otherItem).category)
                && this.name.equals(((TaskerItem) otherItem).name);
        // we do not check value and enabled to be able to detect items when adding new tasks
        //      && this.value.equals(((TaskerItem) otherItem).value)
        //      && this.enabled == ((TaskerItem) otherItem).enabled;
    }

    @Override public String toString() {
        return String.format("category: %s | name: %s | value: %s | trigger: %s | enabled: %s",
                category, name, value, trigger, enabled);
    }


    @Override public int compareTo(@NonNull TaskerItem another) {
        // TODO: different comparables and allow use to switch listing
        int i = this.trigger.compareToIgnoreCase(another.trigger);
        if (i != 0) return i;

        i = this.category.compareToIgnoreCase(another.category);
        if (i != 0) return i;

        return this.name.compareToIgnoreCase(another.name);
    }
}
