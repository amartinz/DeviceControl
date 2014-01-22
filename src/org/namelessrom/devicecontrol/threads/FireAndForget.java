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

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A thread for easy execution of commands without returning of the result.
 */
public class FireAndForget extends Thread {

    private final boolean mUseRoot;
    private final String mCmd;

    /**
     * Runs the command with SH by default.
     *
     * @param cmd The command to execute
     */
    public FireAndForget(String cmd) {
        this(true, cmd);
    }

    /**
     * Runs the command with SU or SH.
     *
     * @param useRoot If SU or SH should be used
     * @param cmd     The command to execute
     */
    public FireAndForget(boolean useRoot, String cmd) {
        mUseRoot = useRoot;
        mCmd = cmd;
    }

    @Override
    public void run() {
        try {
            Shell shell = RootTools.getShell(mUseRoot);

            CommandCapture cmdCap = new CommandCapture(42, false, mCmd);

            shell.add(cmdCap);
        } catch (Exception exc) {
            logDebug("Error when executing: " + mCmd);
            logDebug("Output: " + exc.getMessage());
        }
    }
}
