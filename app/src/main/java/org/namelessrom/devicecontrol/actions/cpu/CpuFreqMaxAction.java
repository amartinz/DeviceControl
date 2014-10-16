package org.namelessrom.devicecontrol.actions.cpu;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.actions.BaseAction;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.hardware.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Created by alex on 16.10.14.
 */
public class CpuFreqMaxAction extends BaseAction {

    public static final String NAME = "cpu_frequency_max";

    public int     id      = -1;
    public String  trigger = "";
    public String  value   = "";
    public boolean bootup  = false;

    public CpuFreqMaxAction(final String value, final boolean bootup) {
        super();
        this.value = value;
        this.bootup = bootup;
    }

    @Override public String getName() { return NAME; }

    @Override public String getCategory() { return CATEGORY_CPU; }

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

        final int cpus = CpuUtils.get().getNumOfCpus();
        final StringBuilder sb = new StringBuilder(cpus * 2);

        String path;
        for (int i = 0; i < cpus; i++) {
            sb.append(CpuUtils.get().onlineCpu(i));
            path = CpuUtils.get().getMaxCpuFrequencyPath(i);
            sb.append(Utils.getWriteCommand(path, value));
            if (bootup) {
                PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_CPU,
                        "cpu_max" + i, CpuUtils.get().getMaxCpuFrequencyPath(i), value));
            }
        }

        Utils.runRootCommand(sb.toString());
    }

}
