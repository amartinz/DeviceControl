/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.hardware;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Easy interaction with governors
 */
public class GovernorUtils {
    //----------------------------------------------------------------------------------------------
    public static final String[] GPU_GOVS = { "performance", "ondemand", "simple", "conservative", "interactive" };

    private static GovernorUtils sInstance;

    private GovernorUtils() { }

    public static GovernorUtils get() {
        if (sInstance == null) {
            sInstance = new GovernorUtils();
        }
        return sInstance;
    }

    @Nullable public String[] getAvailableGpuGovernors() {
        if (TextUtils.isEmpty(GpuUtils.get().getGpuGovsAvailablePath())) {
            return GPU_GOVS;
        }

        final String govs = Utils.readOneLine(GpuUtils.get().getGpuGovsAvailablePath());

        if (govs != null && !govs.isEmpty()) {
            return govs.split(" ");
        }
        return null;
    }
}
