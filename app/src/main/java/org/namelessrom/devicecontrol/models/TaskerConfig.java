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

import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;

import java.util.ArrayList;
import java.util.Iterator;

import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import timber.log.Timber;

/**
 * Tasker configuration which auto serializes itself to a file
 */
public class TaskerConfig {
    private transient static final String NAME = "TaskerConfig";

    public transient static final String FSTRIM = "fstrim";
    public transient static final String FSTRIM_INTERVAL = "fstrim_interval";

    public boolean enabled;

    public boolean fstrimEnabled;
    public int fstrimInterval = 480;

    public ArrayList<TaskerItem> items = new ArrayList<>();

    private transient static TaskerConfig instance;

    private TaskerConfig() { }

    public static TaskerConfig get() {
        if (instance == null) {
            final TaskerConfig config = new TaskerConfig();
            try {
                instance = Paper.book().read(NAME, config);
            } catch (PaperDbException pde) {
                instance = config;
                Timber.e(pde, "Could not read %s", NAME);
            }
        }
        return instance;
    }

    public TaskerConfig save() {
        try {
            Paper.book().write(NAME, TaskerConfig.this);
        } catch (PaperDbException pde) {
            Timber.e(pde, "Could not write %s", NAME);
        }
        return this;
    }

    public ArrayList<TaskerItem> getItemsByTrigger(String trigger) {
        final ArrayList<TaskerItem> filteredItems = new ArrayList<>();

        for (final TaskerItem item : items) {
            if (item != null && trigger.equals(item.trigger)) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    public synchronized TaskerConfig addItem(@NonNull TaskerItem taskerItem) {
        items.add(taskerItem);
        Timber.d("added item -> %s", taskerItem.toString());

        return this;
    }

    public TaskerConfig deleteItem(@NonNull TaskerItem taskerItem) {
        final Iterator<TaskerItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            TaskerItem item = iterator.next();
            if (taskerItem.equals(item)) {
                iterator.remove();
                Timber.d("removed item -> %s", item.toString());
            }
        }

        return this;
    }

}
