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
package org.namelessrom.devicecontrol.modules.filepicker;

import java.io.File;
import java.io.Serializable;

public class FlashItem implements Serializable {

    private String path;
    private String name;

    public FlashItem(final String name, final String path) {
        this.path = path;
        this.name = name;
    }

    public FlashItem(final String path) {
        this.path = path;
        String[] tmp = path.split(File.separator);
        this.name = tmp[tmp.length - 1];
    }

    public String getPath() { return path; }

    public void setPath(final String path) { this.path = path; }

    public String getName() { return name; }

    public void setName(final String name) { this.name = name; }

}
