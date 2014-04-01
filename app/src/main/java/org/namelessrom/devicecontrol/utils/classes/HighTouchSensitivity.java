/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Modifications Copyright (C) 2013 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.utils.classes;

import org.namelessrom.devicecontrol.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Glove mode / high touch sensitivity (on Samsung Galaxy S4)
 */
public class HighTouchSensitivity {

    public final static  String COMMAND_PATH        = "/sys/class/sec/tsp/cmd";
    private final static String COMMAND_LIST_PATH   = "/sys/class/sec/tsp/cmd_list";
    private final static String COMMAND_RESULT_PATH = "/sys/class/sec/tsp/cmd_result";
    private final static String GLOVE_MODE          = "glove_mode";
    public final static  String GLOVE_MODE_ENABLE   = GLOVE_MODE + ",1";
    public final static  String GLOVE_MODE_DISABLE  = GLOVE_MODE + ",0";

    /**
     * Whether device supports high touch sensitivity.
     *
     * @return boolean Supported devices must return always true
     */
    public static boolean isSupported() throws IOException {
        boolean supported = false;
        final File f = new File(COMMAND_PATH);

        // Check to make sure that the kernel supports glove mode
        if (f.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(COMMAND_LIST_PATH));
                String currentLine;
                while ((currentLine = reader.readLine()) != null) {
                    if (currentLine.equals(GLOVE_MODE)) {
                        supported = true;
                        break;
                    }
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        return supported;
    }

    /* The kernel does not expose anything that determines whether or not glove
       mode is enabled, so we'll let Settings.apk keep track of the state
       (kernel boots with glove mode disabled) */

    /* Synchronized because the result needs to be checked (not sure if anything
     * else writes to that sysfs command path though...) */
    private static synchronized boolean setAndCheckResult(String command) {
        boolean status = false;
        Utils.writeValue(COMMAND_PATH, command);
        final String result = Utils.readOneLine(COMMAND_RESULT_PATH);
        if (result.equals(command + ":OK")) {
            status = true;
            logDebug("HTS: Successfully sent \"" + command + "\" to kernel");
        } else {
            logDebug("HTS: Sent \"" + command + "\" to kernel, but got back \"" + result + "\"");
        }
        return status;
    }

    /**
     * This method allows to setup high touch sensitivity status.
     *
     * @param status The new high touch sensitivity status
     * @return boolean Must be false if high touch sensitivity is not supported or the operation
     * failed; true in any other case.
     */
    public static boolean setEnabled(boolean status) {
        if (status) {
            return setAndCheckResult(GLOVE_MODE_ENABLE);
        } else {
            return setAndCheckResult(GLOVE_MODE_DISABLE);
        }
    }
}
