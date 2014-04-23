package org.namelessrom.devicecontrol.events;

public class ShellOutputEvent {

    private final int    mId;
    private final String mOutput;
    private final String mExtras;

    public ShellOutputEvent(final int id, final String output, final String extras) {
        mId = id;
        mOutput = output;
        mExtras = extras;
    }

    public int getId() { return mId; }

    public String getOutput() { return mOutput; }

    public String getExtras() { return mExtras; }

}
