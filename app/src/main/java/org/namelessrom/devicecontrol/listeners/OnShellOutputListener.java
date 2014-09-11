package org.namelessrom.devicecontrol.listeners;

import org.namelessrom.devicecontrol.events.ShellOutputEvent;

/**
 * Created by alex on 11.09.14.
 */
public interface OnShellOutputListener {
    public void onShellOutput(final ShellOutputEvent shellOutput);
}
