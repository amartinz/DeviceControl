package org.namelessrom.devicecontrol.actions.extras;

import android.text.TextUtils;

import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.actions.BaseAction;

import at.amartinz.execution.Command;
import at.amartinz.execution.RootShell;

public class MpDecisionAction extends BaseAction {
    public static final String MPDECISION_PATH = "/system/bin/mpdecision";

    public static final String NAME = "mpdecision_enabled";

    public int id = -1;
    public String trigger = "";
    public String value = "";
    public boolean bootup = false;

    public MpDecisionAction(final String value, final boolean bootup) {
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
            return;
        }

        setBootup(MPDECISION_PATH);

        RootShell.fireAndForget(new Command(enableMpDecision(TextUtils.equals("1", value))));
    }

    private String enableMpDecision(final boolean enable) {
        return (enable ? "start mpdecision 2> /dev/null;" : "stop mpdecision 2> /dev/null;");
    }

}
