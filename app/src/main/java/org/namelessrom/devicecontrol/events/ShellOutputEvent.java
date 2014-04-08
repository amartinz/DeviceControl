package org.namelessrom.devicecontrol.events;

public class ShellOutputEvent {

    private final int    mId;
    private final String mOutput;

    public ShellOutputEvent(final int id, final String output) {
        mId = id;
        mOutput = output;
    }

    public int getId() { return mId; }

    public String getOutput() { return mOutput; }

}
