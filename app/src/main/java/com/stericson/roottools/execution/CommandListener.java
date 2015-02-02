package com.stericson.roottools.execution;

/**
 * Created by alex on 02.02.15.
 */
public interface CommandListener {
    public void commandOutput(int id, String line);

    public void commandTerminated(int id, String reason);

    public void commandCompleted(int id, int exitCode);
}
