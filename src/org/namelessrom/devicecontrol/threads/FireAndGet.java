/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.devicecontrol.threads;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A thread for easy execution of commands without returning of the result.
 */
public class FireAndGet extends Thread implements DeviceConstants {

    private final String mCmd;
    private final Handler mHandler;
    private final boolean mNeedsNewline;
    private final StringBuilder sb = new StringBuilder();

    public FireAndGet(String cmd, Handler handler) {
        this(cmd, handler, false);
    }

    public FireAndGet(String cmd, Handler handler, boolean needsNewline) {
        mCmd = cmd;
        mHandler = handler;
        mNeedsNewline = needsNewline;
    }

    @Override
    public void run() {
        try {
            final Shell shell = RootTools.getShell(true);

            CommandCapture cmdCap = new CommandCapture(42, false, mCmd) {
                @Override
                public void commandOutput(int id, String line) {
                    sb.append(line);
                    if (mNeedsNewline) {
                        sb.append("\n");
                    }
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    if (mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putInt(READ_VALUE_ACTION, READ_VALUE_ACTION_RESULT);
                        bundle.putString(READ_VALUE_TEXT, sb.toString());
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }
            };

            shell.add(cmdCap);
        } catch (Exception exc) {
            logDebug("Error when executing: " + mCmd);
            logDebug("Output: " + exc.getMessage());
        }
    }
}
