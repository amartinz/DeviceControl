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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Easy interaction with governors
 */
public class GovernorUtils {

    //----------------------------------------------------------------------------------------------
    public static final String GOV0_CURRENT_PATH =
            CpuUtils.CPU_BASE + "cpu0/cpufreq/scaling_governor";
    public static final String GOV1_CURRENT_PATH =
            CpuUtils.CPU_BASE + "cpu1/cpufreq/scaling_governor";
    public static final String GOV2_CURRENT_PATH =
            CpuUtils.CPU_BASE + "cpu2/cpufreq/scaling_governor";
    public static final String GOV3_CURRENT_PATH =
            CpuUtils.CPU_BASE + "cpu3/cpufreq/scaling_governor";

    //----------------------------------------------------------------------------------------------
    public static final String GOV_AVAILALBLE_PATH =
            CpuUtils.CPU_BASE + "cpu0/cpufreq/scaling_available_governors";

    //----------------------------------------------------------------------------------------------
    public static final String[] GPU_GOVS =
            { "performance", "ondemand", "simple", "conservative", "interactive" };

    public static class Governor {
        public final String[] available;
        public final String current;

        public Governor(final String[] availableGovernors, final String governor) {
            available = availableGovernors;
            current = governor;
        }
    }

    public interface GovernorListener {
        void onGovernor(@NonNull final Governor governor);
    }

    private static GovernorUtils sInstance;

    private GovernorUtils() { }

    public static GovernorUtils get() {
        if (sInstance == null) {
            sInstance = new GovernorUtils();
        }
        return sInstance;
    }

    @NonNull public String getGovernorPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return GOV0_CURRENT_PATH;
            case 1:
                return GOV1_CURRENT_PATH;
            case 2:
                return GOV2_CURRENT_PATH;
            case 3:
                return GOV3_CURRENT_PATH;
        }
    }

    @Nullable public String[] getAvailableGovernors(final boolean isGpu) {
        String[] govArray = null;
        final String govs = Utils.readOneLine(isGpu
                ? GpuUtils.get().getGpuGovsAvailablePath() : GOV_AVAILALBLE_PATH);

        if (govs != null && !govs.isEmpty()) {
            govArray = govs.split(" ");
        }

        return govArray;
    }

    @Nullable public String[] getAvailableCpuGovernors() {
        return getAvailableGovernors(false);
    }

    @Nullable public String[] getAvailableGpuGovernors() {
        if (TextUtils.isEmpty(GpuUtils.get().getGpuGovsAvailablePath())) return GPU_GOVS;
        return getAvailableGovernors(true);
    }

    public void getGovernor(final GovernorListener listener) {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(GOV_AVAILALBLE_PATH).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(GOV0_CURRENT_PATH).append(" 2> /dev/null;");
            cmd.append(");").append("echo $command | busybox tr -d \"\\n\"");
            Logger.v(CpuUtils.class, cmd.toString());

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, cmd.toString()) {
                @Override public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    Logger.v(CpuUtils.class, line);
                }

                @Override public void commandCompleted(int id, int exitcode) {
                    final List<String> result =
                            Arrays.asList(outputCollector.toString().split(" "));
                    final List<String> tmpList = new ArrayList<>();
                    String tmpString = "";

                    if (result.size() <= 0) return;

                    for (final String s : result) {
                        if (s.isEmpty()) continue;
                        if (s.charAt(0) == '[') {
                            tmpString = s.substring(1, s.length());
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String gov = tmpString;
                    final String[] availGovs = tmpList.toArray(new String[tmpList.size()]);
                    Application.HANDLER.post(new Runnable() {
                        @Override public void run() {
                            listener.onGovernor(new Governor(availGovs, gov));
                        }
                    });

                }
            };

            if (mShell.isClosed()) { throw new Exception("Shell is closed"); }
            mShell.add(cmdCapture);
        } catch (Exception exc) {
            Logger.v(CpuUtils.class, "Error: " + exc.getMessage());
        }
    }
}
