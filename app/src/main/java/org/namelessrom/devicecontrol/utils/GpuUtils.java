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
package org.namelessrom.devicecontrol.utils;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.GpuEvent;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class GpuUtils implements PerformanceConstants {

    public static String[] getFreqToMhz(final String file) throws IOException {
        final ArrayList<String> names = new ArrayList<String>();
        Utils.runRootCommand(String.format("chmod 644 %s", file));

        final File freqfile = new File(file);
        FileInputStream fin1 = null;
        String s1 = null;
        try {
            fin1 = new FileInputStream(freqfile);
            byte fileContent[] = new byte[(int) freqfile.length()];
            fin1.read(fileContent);
            s1 = new String(fileContent);
        } finally {
            if (fin1 != null) {
                fin1.close();
            }
        }
        final String[] frequencies = s1.trim().split(" ");
        for (final String s : frequencies) {
            names.add(toMhz(s));
        }
        return names.toArray(new String[names.size()]);
    }

    public static String toMhz(final String mhz) {
        return (String.valueOf(Integer.parseInt(mhz) / 1000000) + " MHz");
    }

    public static String fromMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            return String.valueOf(Integer.parseInt(mhzString.replace(" MHz", "")) * 1000000);
        } else { return "0"; }
    }

    public static String restore() {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = DatabaseHandler.getInstance().getAllItems(
                DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_GPU);
        for (final DataItem item : items) {
            sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
        }

        return sbCmd.toString();
    }

    public static String[] getAvailableFrequencies() {
        final String freqsRaw = Utils.readOneLine(GPU_FREQUENCIES_FILE);
        if (freqsRaw != null && !freqsRaw.isEmpty()) {
            return freqsRaw.split(" ");
        }
        return null;
    }

    public static String[] freqsToMhz(final String[] frequencies) {
        final int length = frequencies.length;
        final String[] names = new String[length];

        for (int i = 0; i < length; i++) {
            names[i] = toMhz(frequencies[i]);
        }

        return names;
    }

    public static boolean containsGov(final String gov) {
        for (final String s : GPU_GOVS) {
            if (gov.toLowerCase().equals(s.toLowerCase())) { return true; }
        }
        return false;
    }

    //==============================================================================================
    // Events
    //==============================================================================================
    public static void getOnGpuEvent() {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(GPU_FREQUENCIES_FILE).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(GPU_MAX_FREQ_FILE).append(" 2> /dev/null;");
            cmd.append("echo -n \"]\";");
            cmd.append("cat ").append(GPU_GOV_PATH).append(" 2> /dev/null;");
            cmd.append(");").append("echo $command | tr -d \"\\n\"");
            logDebug(cmd.toString());

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, false, cmd.toString()) {
                @Override
                public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    logDebug(line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    final List<String> result =
                            Arrays.asList(outputCollector.toString().split(" "));
                    final List<String> tmpList = new ArrayList<String>();
                    String tmpMax = "", tmpGov = "";

                    for (final String s : result) {
                        if (s.charAt(0) == '[') {
                            tmpMax = s.substring(1, s.length());
                        } else if (s.charAt(0) == ']') {
                            tmpGov = s.substring(1, s.length());
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String[] avail = tmpList.toArray(new String[tmpList.size()]);
                    final String max = tmpMax;
                    final String gov = tmpGov;
                    Application.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(new GpuEvent(avail, max, gov));
                        }
                    });

                }
            };

            if (mShell.isClosed()) { throw new Exception("Shell is closed"); }
            mShell.add(cmdCapture);
        } catch (Exception exc) {
            logDebug("Error: " + exc.getMessage());
        }
    }

}
