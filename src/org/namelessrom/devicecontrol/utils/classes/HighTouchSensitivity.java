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

import android.util.Log;

import org.namelessrom.devicecontrol.threads.WriteAndForget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Glove mode / high touch sensitivity (on Samsung Galaxy S4)
 * TODO rewrite to use root
 */
public class HighTouchSensitivity {

    private static final String TAG = "HighTouchSensitivity";

    public final static String COMMAND_PATH = "/sys/class/sec/tsp/cmd";
    private final static String COMMAND_LIST_PATH = "/sys/class/sec/tsp/cmd_list";
    private final static String COMMAND_RESULT_PATH = "/sys/class/sec/tsp/cmd_result";
    private final static String GLOVE_MODE = "glove_mode";
    public final static String GLOVE_MODE_ENABLE = GLOVE_MODE + ",1";
    public final static String GLOVE_MODE_DISABLE = GLOVE_MODE + ",0";

    /**
     * Whether device supports high touch sensitivity.
     *
     * @return boolean Supported devices must return always true
     */
    public static boolean isSupported() {
        boolean supported = false;
        File f = new File(COMMAND_PATH);

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
            } catch (IOException e) {
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                }
            }
        }

        if (supported) {
            Log.v(TAG, "Glove mode / high touch sensitivity is supported");
        } else {
            Log.e(TAG, "Glove mode / high touch sensitivity is NOT supported");
        }

        return supported;
    }

    /* The kernel does not expose anything that determines whether or not glove
       mode is enabled, so we'll let Settings.apk keep track of the state
       (kernel boots with glove mode disabled) */

    /* Synchronized because the result needs to be checked (not sure if anything
     * else writes to that sysfs command path though...) */
    private static synchronized void setResult(String command) {
        new WriteAndForget(COMMAND_PATH, command).run();
    }

    /**
     * This method allows to setup high touch sensitivity status.
     *
     * @param status The new high touch sensitivity status
     * @return boolean Must be false if high touch sensitivity is not supported or the operation
     * failed; true in any other case.
     */
    public static void setEnabled(boolean status) {
        if (status) {
            setResult(GLOVE_MODE_ENABLE);
        } else {
            setResult(GLOVE_MODE_DISABLE);
        }
    }
}
