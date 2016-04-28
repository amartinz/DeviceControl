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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.namelessrom.devicecontrol.modules.bootup.BootupItem;

import java.util.ArrayList;
import java.util.Iterator;

import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import timber.log.Timber;

/**
 * Bootup configuration which auto serializes itself to a file
 */
public class BootupConfig {
    private transient static final String NAME = "BootupConfig";

    public transient static final String CATEGORY_DEVICE = "device";
    public transient static final String CATEGORY_CPU = "cpu";
    public transient static final String CATEGORY_GPU = "gpu";
    public transient static final String CATEGORY_EXTRAS = "extras";
    public transient static final String CATEGORY_SYSCTL = "sysctl";
    public transient static final String CATEGORY_VOLTAGE = "voltage";

    public transient static final String CATEGORY_INTELLI_HOTPLUG = "intelli_hotplug";
    public transient static final String CATEGORY_MAKO_HOTPLUG = "mako_hotplug";

    public ArrayList<BootupItem> items = new ArrayList<>();
    public boolean isEnabled;

    public boolean isAutomatedRestoration;
    public int automatedRestorationDelay;

    private transient static BootupConfig instance;

    private BootupConfig() { }

    public static BootupConfig get() {
        if (instance == null) {
            final BootupConfig config = new BootupConfig();
            try {
                instance = Paper.book().read(NAME, config);
            } catch (PaperDbException pde) {
                instance = config;
                Timber.e(pde, "Could not read %s", NAME);
            }
        }
        return instance;
    }

    public BootupConfig save() {
        try {
            Paper.book().write(NAME, BootupConfig.this);
        } catch (PaperDbException pde) {
            Timber.e(pde, "Could not write %s", NAME);
        }
        return this;
    }

    public ArrayList<BootupItem> getItemsByCategory(String category) {
        final ArrayList<BootupItem> filteredItems = new ArrayList<>();

        for (final BootupItem item : items) {
            if (item != null && category.equals(item.category)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    @Nullable public BootupItem getItemByName(String name) {
        for (final BootupItem item : items) {
            if (item != null && name.equals(item.name)) {
                return item;
            }
        }
        return null;
    }

    public synchronized BootupConfig addItem(@NonNull BootupItem bootupItem) {
        deleteItem(bootupItem);
        items.add(bootupItem);
        Timber.d("added item -> %s", bootupItem.toString());
        return this;
    }

    public BootupConfig deleteItem(@NonNull BootupItem bootupItem) {
        final Iterator<BootupItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            BootupItem item = iterator.next();
            if (bootupItem.equals(item)) {
                iterator.remove();
                Timber.d("removed item -> %s", item.toString());
            }
        }
        return this;
    }

    public static synchronized BootupConfig setBootup(@NonNull BootupItem item) {
        final BootupConfig config = BootupConfig.get();
        config.deleteItem(item);
        config.addItem(item);
        config.save();
        return config;
    }
}
