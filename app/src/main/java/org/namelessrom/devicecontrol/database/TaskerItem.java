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

import hugo.weaving.DebugLog;

public class TaskerItem implements Serializable {

    public static final String CATEGORY_SCREEN_ON  = "screen_on";
    public static final String CATEGORY_SCREEN_OFF = "screen_off";

    private int    _id;
    private String _category;
    private String _name;
    private String _filename;
    private String _value;
    private String _enabled;

    public TaskerItem() { }

    @DebugLog public TaskerItem(final String category, final String name, final String filename,
            final String value, final boolean enabled) {
        this._id = -1;
        this._category = category;
        this._name = name;
        this._value = value;
        this._filename = filename;
        this._enabled = (enabled ? "1" : "0");
    }

    public int getID() {
        return this._id;
    }

    public void setID(final int id) {
        this._id = id;
    }

    public String getCategory() {
        return this._category;
    }

    public void setCategory(final String category) {
        this._category = category;
    }

    public String getName() {
        return this._name;
    }

    public void setName(final String name) {
        this._name = name;
    }

    public String getValue() {
        return this._value;
    }

    public void setValue(final String value) {
        this._value = value;
    }

    public String getFileName() {
        return this._filename;
    }

    public void setFileName(final String filename) {
        this._filename = filename;
    }

    public boolean getEnabled() {
        return this._enabled.equals("1");
    }

    public void setEnabled(final boolean isEnabled) {
        this._enabled = (isEnabled ? "1" : "0");
    }

}
