package org.namelessrom.devicecontrol.actions.extras;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.BaseAction;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

public class PnPMgrAction extends BaseAction {
    public static final String PNPMGR_PATH = "/system/bin/pnpmgr";

    public static final String NAME = "pnpmgr_enabled";

    public int id = -1;
    public String trigger = "";
    public String value = "";
    public boolean bootup = false;

    public PnPMgrAction(final String value, final boolean bootup) {
        super();
        this.value = value;
        this.bootup = bootup;
    }

    @Override public String getName() { return NAME; }

    @Override public String getCategory() { return ActionProcessor.CATEGORY_EXTRAS; }

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

        if (bootup) {
            PreferenceHelper.setBootup(new DataItem(DatabaseHandler.CATEGORY_EXTRAS, getName(),
                    PNPMGR_PATH, value));
        }

        Utils.runRootCommand(enablePnPMgr(TextUtils.equals("1", value)));
    }

    private String enablePnPMgr(final boolean enable) {
        return (enable ? "start pnpmgr 2> /dev/null;\n" : "stop pnpmgr 2> /dev/null;\n");
    }

}
