package com.stericson.roottools.execution;

public interface CommandListener {
    void commandOutput(int id, String line);

    void commandTerminated(int id, String reason);

    void commandCompleted(int id, int exitCode);
}
