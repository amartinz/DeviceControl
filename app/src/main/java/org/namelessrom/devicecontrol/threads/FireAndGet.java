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

import android.app.Activity;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A thread for easy execution of commands without returning of the result.
 */
public class FireAndGet extends Thread implements DeviceConstants {

    private final String  mCmd;
    private final boolean mNeedsNewline;
    private final StringBuilder sb = new StringBuilder();
    private final Activity mActivity;

    public FireAndGet(String cmd, boolean needsNewline, Activity activity) {
        mCmd = cmd;
        mNeedsNewline = needsNewline;
        mActivity = activity;
    }

    @Override
    public void run() {
        try {
            final Shell shell = RootTools.getShell(true);

            final CommandCapture cmdCap = new CommandCapture(0, false, mCmd) {
                @Override
                public void commandOutput(int id, String line) {
                    sb.append(line);
                    if (mNeedsNewline) {
                        sb.append("\n");
                    }
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    if (mActivity != null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BusProvider.getBus().post(new ShellOutputEvent(sb.toString()));
                            }
                        });
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
