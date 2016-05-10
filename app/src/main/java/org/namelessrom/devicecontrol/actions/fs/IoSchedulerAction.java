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
package org.namelessrom.devicecontrol.actions.fs;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.BaseAction;
import org.namelessrom.devicecontrol.hardware.IoUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import at.amartinz.execution.Command;
import at.amartinz.execution.RootShell;

public class IoSchedulerAction extends BaseAction {

    public static final String NAME = "io_scheduler";

    public int id = -1;
    public String trigger = "";
    public String value = "";
    public boolean bootup = false;

    public IoSchedulerAction(final String value, final boolean bootup) {
        super();
        this.value = value;
        this.bootup = bootup;
    }

    @Override public String getName() { return NAME; }

    @Override public String getCategory() { return ActionProcessor.CATEGORY_FS; }

    @Override public String getTrigger() { return trigger; }

    @Override public String getValue() { return value; }

    @Override public boolean getBootup() { return bootup; }

    @Override protected void setupAction() {
        // TODO: what?
    }

    @Override public void triggerAction() {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        int c = 0;
        for (final String ioPath : IoUtils.IO_SCHEDULER_PATH) {
            sb.append(Utils.getWriteCommand(ioPath, value));
            if (bootup) {
                BootupConfig.setBootup(
                        new BootupItem(BootupConfig.CATEGORY_EXTRAS,
                                "io" + (c++), ioPath, value, true));
            }
        }

        RootShell.fireAndForget(new Command(sb.toString()));
    }

}
