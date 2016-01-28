/*
 *  Copyright (C) 2013 h0rn3t
 *  Modifications Copyright (C) 2013 -2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.editor;

import android.support.annotation.NonNull;

public class Prop implements Comparable<Prop> {

    private String mName;
    private String mData;

    public Prop(final String n, final String d) {
        mName = n;
        mData = d;
    }

    public String getName() { return mName; }

    public void setName(final String d) { mName = d; }

    public String getVal() { return mData; }

    public void setVal(final String d) { mData = d; }

    public int compareTo(@NonNull final Prop o) {
        if (mName != null) {
            return mName.toLowerCase().compareTo(o.getName().toLowerCase());
        } else {
            throw new IllegalArgumentException();
        }
    }
}
