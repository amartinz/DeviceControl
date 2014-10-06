/*
 * Copyright (C) 2013 The CyanogenMod Project
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

package org.namelessrom.devicecontrol.hardware;

import android.content.res.Resources;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;

public class DisplayColorCalibration {
    public static final String TAG = "display_color_calibration";

    private static DisplayColorCalibration sInstance;

    private String path;
    private String ctrl;
    private int    max;
    private int    min;

    private DisplayColorCalibration() {
        final Resources res = Application.get().getResources();
        final String[] paths = res.getStringArray(R.array.hardware_display_color_calibration_paths);
        final String[] ctrls = res.getStringArray(R.array.hardware_display_color_calibration_ctrls);
        final String[] maxs = res.getStringArray(R.array.hardware_display_color_calibration_max);
        final String[] mins = res.getStringArray(R.array.hardware_display_color_calibration_min);

        final int length = paths.length;
        for (int i = 0; i < length; i++) {
            // if the file exists, set up the values
            if (Utils.fileExists(paths[i])) {
                // our existing path
                path = paths[i];

                // our control path, optional
                ctrl = Utils.fileExists(ctrls[i]) ? ctrls[i] : null;

                // maximum and minimum
                max = Integer.parseInt(maxs[i]);
                min = Integer.parseInt(mins[i]);

                // and get out of here
                break;
            }
        }
    }

    public static DisplayColorCalibration get() {
        if (sInstance == null) {
            sInstance = new DisplayColorCalibration();
        }
        return sInstance;
    }

    public boolean isSupported() { return Utils.fileExists(path); }

    public int getMaxValue() { return max; }

    public int getMinValue() { return min; }

    public int getDefValue() { return getMaxValue(); }

    public String getCurColors() { return Utils.readOneLine(path); }

    public void setColors(final String colors) {
        final StringBuilder sb = new StringBuilder();
        sb.append(Utils.getWriteCommand(path, colors));
        if (!TextUtils.isEmpty(ctrl)) {
            sb.append(Utils.getWriteCommand(ctrl, "1"));
        }
        Utils.runRootCommand(sb.toString());
    }

    public String getPath() { return path; }
}
