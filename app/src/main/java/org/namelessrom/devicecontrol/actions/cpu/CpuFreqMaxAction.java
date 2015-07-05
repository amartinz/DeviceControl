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
package org.namelessrom.devicecontrol.actions.cpu;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.BaseAction;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

public class CpuFreqMaxAction extends BaseAction {

    public static final String NAME = "cpu_frequency_max";

    public int id = -1;
    public String trigger = "";
    public String value = "";
    public boolean bootup = false;

    public CpuFreqMaxAction(final String value, final boolean bootup) {
        super();
        this.value = value;
        this.bootup = bootup;
    }

    @Override public String getName() { return NAME; }

    @Override public String getCategory() { return ActionProcessor.CATEGORY_CPU; }

    @Override public String getTrigger() { return trigger; }

    @Override public String getValue() { return value; }

    @Override public boolean getBootup() { return bootup; }

    @Override protected void setupAction() {
        // TODO: what?
    }

    @Override public void triggerAction() {
        if (TextUtils.isEmpty(value)) {
            Logger.wtf(this, "No value for action!");
            return;
        }

        final boolean lockFreq = DeviceConfig.get().perfCpuLock;

        final int cpus = CpuUtils.get().getNumOfCpus();
        final StringBuilder sb = new StringBuilder(lockFreq ? cpus * 3 : cpus * 2);

        final BootupConfig configuration = BootupConfig.get();
        String path;
        for (int i = 0; i < cpus; i++) {
            if (i != 0) {
                sb.append(CpuUtils.get().onlineCpu(i));
            }
            path = CpuUtils.get().getMaxCpuFrequencyPath(i);
            sb.append(Utils.getWriteCommand(path, value));
            if (bootup) {
                configuration.addItem(new BootupItem(BootupConfig.CATEGORY_CPU,
                        "cpu_max" + i, CpuUtils.get().getMaxCpuFrequencyPath(i), value, true));
            }
            if (lockFreq) {
                sb.append(Utils.lockFile(path));
            }
        }
        configuration.save();

        Utils.runRootCommand(sb.toString());
    }

}
