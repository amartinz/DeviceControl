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
package org.namelessrom.devicecontrol.utils;

import org.namelessrom.devicecontrol.modules.appmanager.AppItem;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;

public class SortHelper {

    public static final Comparator<AppItem> sAppComparator = new Comparator<AppItem>() {
        public final int compare(final AppItem a, final AppItem b) {
            return collator.compare(a.getLabel(), b.getLabel());
        }

        private final Collator collator = Collator.getInstance();
    };

    public static final Comparator<File> sFileComparator = new Comparator<File>() {
        @Override
        public int compare(final File lhs, final File rhs) {
            // if we have a directory and a file, we want the directory to win
            if (lhs.isDirectory() && !rhs.isDirectory()) return -1;
            // same for a file and a directory
            if (!lhs.isDirectory() && rhs.isDirectory()) return 1;
            // if we have two files or two directories, we let the filename decide
            return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
        }
    };

}
