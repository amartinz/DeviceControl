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

public class DataItem {

    public int _id;
    public String _category;
    public String _name;
    public String _filename;
    public String _value;
    public boolean _enabled;

    public DataItem() { }

    public DataItem(final String category, final String name, final String filename,
            final String value, final boolean enabled) {
        this(-1, category, name, filename, value, enabled);
    }

    public DataItem(final int id, final String category, final String name, final String filename,
            final String value, final boolean enabled) {
        _id = id;
        _category = category;
        _name = name;
        _value = value;
        _filename = filename;
        _enabled = enabled;
    }

}
