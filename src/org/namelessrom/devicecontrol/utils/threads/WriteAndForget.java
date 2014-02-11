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

import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A thread for easy writing to the filesystem without returning of the result.
 */
public class WriteAndForget extends Thread {

    private final StringBuilder mCmd = new StringBuilder();


    public WriteAndForget(String filename, String value) {
        mCmd.append("busybox echo ").append(value).append(" > ").append(filename)
                .append(";\n");
    }

    public WriteAndForget(final List<String> filenames, final List<String> values) {
        for (int i = 0; i < filenames.size(); i++) {
            mCmd.append("busybox echo ").append(values.get(i))
                    .append(" > ").append(filenames.get(i))
                    .append(";\n");
        }
    }

    public WriteAndForget append(final String filename, final String value) {
        mCmd.append("busybox echo ").append(value).append(" > ").append(filename)
                .append(";\n");
        return this;
    }

    @Override
    public void run() {

        try {
            Shell shell = RootTools.getShell(true);

            CommandCapture cmdCap = new CommandCapture(40, false, mCmd.toString());

            shell.add(cmdCap);
        } catch (Exception exc) {
            logDebug("Error when executing: " + mCmd);
            logDebug("Output: " + exc.getMessage());
        }
    }
}
