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
package org.namelessrom.devicecontrol.actions;

import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;

public abstract class BaseAction {

    public BaseAction() {
        setupAction();
    }

    /**
     * The name of the action, eg "cpu_frequency_max"
     */
    public abstract String getName();

    /**
     * The category of the action, eg example "cpu"
     */
    public abstract String getCategory();

    /**
     * The type of trigger, which will trigger the action
     */
    public abstract String getTrigger();

    /**
     * The value which should be written to the file
     */
    public abstract String getValue();

    /**
     * Whether the action should be run on bootup
     */
    public abstract boolean getBootup();

    /**
     * A place to set up the action, for example loading values, setting up priority etc.
     */
    protected abstract void setupAction();

    /**
     * Called when the action should be triggered.
     */
    public abstract void triggerAction();

    @Override public String toString() {
        return String.format(
                "category: %s | name: %s | trigger: %s | value: %s | bootup: %s",
                getCategory(), getName(), getTrigger(), getValue(), getBootup());
    }

    public void setBootup(String filename) {
        if (getBootup()) {
            BootupConfig.setBootup(
                    new BootupItem(getCategory(), getName(), filename, getValue(), true));
        }
    }

}
