package org.namelessrom.devicecontrol.actions;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.configuration.BootupConfiguration;
import org.namelessrom.devicecontrol.objects.BootupItem;

/**
 * Created by alex on 16.10.14.
 */
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
            BootupConfiguration.setBootup(Application.get(),
                    new BootupItem(getCategory(), getName(), filename, getValue(), true));
        }
    }

}
