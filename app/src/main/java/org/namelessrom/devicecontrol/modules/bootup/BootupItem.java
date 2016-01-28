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
package org.namelessrom.devicecontrol.modules.bootup;

import android.support.annotation.NonNull;

public class BootupItem implements Comparable<BootupItem> {
    public String category;
    public String name;
    public String filename;
    public String value;
    public boolean enabled;
    public int titleResId = -1;

    public BootupItem() { }

    public BootupItem(String category, String name, String filename, String value,
            boolean enabled) {
        this(category, -1, name, filename, value, enabled);
    }

    public BootupItem(String category, int titleResId, String name, String filename, String value,
            boolean enabled) {
        this.category = category;
        this.titleResId = titleResId;
        this.name = name;
        this.value = value;
        this.filename = filename;
        this.enabled = enabled;
    }

    @Override public boolean equals(Object otherItem) {
        return otherItem instanceof BootupItem
                && this.category.equals(((BootupItem) otherItem).category)
                && this.name.equals(((BootupItem) otherItem).name)
                && this.filename.equals(((BootupItem) otherItem).filename);
        // we do not check enabled or value to be able to detect items when adding new bootup items
        //      && this.value.equals(((BootupItem) otherItem).value);
        //      && this.enabled == ((BootupItem) otherItem).enabled;
    }

    @Override public String toString() {
        return String.format("category: %s | name: %s | value: %s | filename: %s | enabled: %s",
                category, name, value, filename, enabled);
    }


    @Override public int compareTo(@NonNull BootupItem another) {
        // TODO: different comparables and allow use to switch listing
        int i = this.category.compareToIgnoreCase(another.category);
        if (i != 0) return i;

        return this.name.compareToIgnoreCase(another.name);
    }

}
