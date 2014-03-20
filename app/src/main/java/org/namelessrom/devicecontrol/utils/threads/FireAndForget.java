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

package org.namelessrom.devicecontrol.utils.threads;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A thread for easy execution of commands without returning of the result.
 */
public class FireAndForget extends Thread {

    private final boolean mUseRoot;
    private final String  mCmd;
    private final boolean mRemountSystem;

    /**
     * Runs the command with SU by default.
     *
     * @param cmd The command to execute
     */
    public FireAndForget(String cmd) {
        this(cmd, true, false);
    }

    /**
     * Runs the command with SU or SH.
     *
     * @param cmd     The command to execute
     * @param useRoot If SU or SH should be used
     */
    public FireAndForget(String cmd, boolean useRoot) {
        this(cmd, useRoot, false);
    }

    /**
     * Runs the command with SU or SH and optional system remount.
     *
     * @param cmd           The command to execute
     * @param useRoot       If SU or SH should be used
     * @param remountSystem If /system should be remounted
     */
    public FireAndForget(String cmd, boolean useRoot, boolean remountSystem) {
        mUseRoot = useRoot;
        mCmd = cmd;
        mRemountSystem = remountSystem;
    }

    @Override
    public void run() {
        try {
            Shell shell = RootTools.getShell(mUseRoot);

            if (mRemountSystem) {
                RootTools.remount("/system", "rw");
            }

            CommandCapture cmdCap = new CommandCapture(42, false, mCmd) {
                @Override
                public void commandCompleted(int id, int exitcode) {
                    if (mRemountSystem) {
                        RootTools.remount("/system", "ro");
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
