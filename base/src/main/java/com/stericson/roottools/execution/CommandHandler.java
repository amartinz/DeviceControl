package com.stericson.roottools.execution;

import android.os.Handler;
import android.os.Message;

import org.namelessrom.devicecontrol.Logger;

public class CommandHandler extends Handler implements CommandListener {
    public static final String ACTION = "action";
    public static final String EXIT_CODE = "exit_code";
    public static final String ID = "id";
    public static final String TEXT = "text";

    public static final int COMMAND_OUTPUT = 0x01;
    public static final int COMMAND_COMPLETED = 0x02;
    public static final int COMMAND_TERMINATED = 0x03;

    @Override
    public void handleMessage(final Message msg) {
        if (msg == null || msg.getData() == null) return;

        final int action = msg.getData().getInt(CommandHandler.ACTION);
        final int id = msg.getData().getInt(CommandHandler.ID);
        final String text = msg.getData().getString(CommandHandler.TEXT);

        switch (action) {
            case CommandHandler.COMMAND_COMPLETED:
                final int exitcode = msg.getData().getInt(CommandHandler.EXIT_CODE);
                commandCompleted(id, exitcode);
                break;
            case CommandHandler.COMMAND_TERMINATED:
                commandTerminated(id, text);
                break;
            case CommandHandler.COMMAND_OUTPUT:
                commandOutput(id, text);
                break;
            default:
                Logger.wtf(this, "did not handle case - %s", action);
                break;
        }
    }

    @Override public void commandOutput(int id, String line) { }

    @Override public void commandTerminated(int id, String reason) { }

    @Override public void commandCompleted(int id, int exitCode) { }
}
