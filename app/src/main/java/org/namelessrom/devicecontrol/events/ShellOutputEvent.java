package org.namelessrom.devicecontrol.events;

public class ShellOutputEvent {

    private final String mOutput;

    public ShellOutputEvent(final String output) {
        mOutput = output;
    }

    public String getOutput() {
        return mOutput;
    }

}
