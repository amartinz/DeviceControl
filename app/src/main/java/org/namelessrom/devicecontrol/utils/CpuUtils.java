/*
 *  Copyright (C) 2014 Alexander "Evisceration" Martinz
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
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.CpuFreqEvent;
import org.namelessrom.devicecontrol.events.GovernorEvent;
import org.namelessrom.devicecontrol.events.IoSchedulerEvent;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Generic CPU Tasks.
 */
public class CpuUtils implements PerformanceConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String ACTION_FREQ_MAX = "action_freq_max";
    public static final String ACTION_FREQ_MIN = "action_freq_min";
    public static final String ACTION_GOV      = "action_gov";

    //==============================================================================================
    // Methods
    //==============================================================================================
    public static String[] getUvValues(final boolean getName) throws IOException {
        final ArrayList<String> valueList = new ArrayList<String>();
        DataInputStream in = null;
        BufferedReader br = null;
        FileInputStream fstream = null;
        try {
            File f = new File(VDD_TABLE_FILE);
            if (f.exists()) {
                fstream = new FileInputStream(VDD_TABLE_FILE);
            } else {
                f = new File(UV_TABLE_FILE);
                if (f.exists()) {
                    fstream = new FileInputStream(UV_TABLE_FILE);
                }
            }

            if (fstream != null) {
                in = new DataInputStream(fstream);
                br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                String[] values;
                while ((strLine = br.readLine()) != null) {
                    strLine = strLine.trim();
                    if ((strLine.length() != 0)) {
                        if (getName) {
                            values = strLine.replaceAll(":", "").split("\\s+");
                            valueList.add(values[0]);
                        } else {
                            values = strLine.split("\\s+");
                            valueList.add(values[1]);
                        }
                    }
                }
            }
        } finally {
            if (br != null) br.close();
            if (in != null) in.close();
            if (fstream != null) fstream.close();
        }

        return valueList.toArray(new String[valueList.size() - 1]);
    }

    public static boolean hasMsmDcvs() { return Utils.fileExists(MSM_DCVS_FILE); }

    public static boolean isMsmDcvs() {
        final String s = Utils.readOneLine(MSM_DCVS_FILE);
        if (s == null || s.isEmpty()) { return false; }
        return s.trim().equals("1");
    }

    public static void enableMsmDcvs(final boolean enable) {
        Utils.writeValue(MSM_DCVS_FILE, (enable ? "1" : "0"));
    }

    public static void enableIntelliPlug(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_PATH, (enable ? "1" : "0"));
    }

    public static void enableIntelliPlugEcoMode(final boolean enable) {
        Utils.writeValue(INTELLI_PLUG_ECO_MODE_PATH, (enable ? "1" : "0"));
    }

    public static String getCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return CPU0_FREQ_CURRENT_PATH;
            case 1:
                return CPU1_FREQ_CURRENT_PATH;
            case 2:
                return CPU2_FREQ_CURRENT_PATH;
            case 3:
                return CPU3_FREQ_CURRENT_PATH;
        }
    }

    public static String getMaxCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return FREQ0_MAX_PATH;
            case 1:
                return FREQ1_MAX_PATH;
            case 2:
                return FREQ2_MAX_PATH;
            case 3:
                return FREQ3_MAX_PATH;
        }
    }

    public static String getMinCpuFrequencyPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return FREQ0_MIN_PATH;
            case 1:
                return FREQ1_MIN_PATH;
            case 2:
                return FREQ2_MIN_PATH;
            case 3:
                return FREQ3_MIN_PATH;
        }
    }

    public static String getGovernorPath(final int cpu) {
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

    public static String getOnlinePath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return "";
            case 1:
                return CORE1_ONLINE;
            case 2:
                return CORE2_ONLINE;
            case 3:
                return CORE3_ONLINE;
        }
    }

    public static String getCpuFrequency(final int cpu) {
        final String path = getCpuFrequencyPath(cpu);

        if (Utils.fileExists(path)) {
            return Utils.readOneLine(path);
        } else {
            return "0";
        }
    }

    public static int getCpuTemperature() {
        String tmpString = Utils.readOneLine(CPU_TEMP_PATH);
        if (tmpString != null && !tmpString.trim().isEmpty()) {
            int temp = Integer.parseInt(tmpString);
            temp = (temp < 0 ? 0 : temp);
            temp = (temp > 100 ? 100 : temp);
            return temp;
        } else {
            return -1;
        }
    }

    public static boolean getIntelliPlugActive() {
        return Utils.readOneLine(INTELLI_PLUG_PATH).trim().equals("1");
    }

    public static boolean getIntelliPlugEcoMode() {
        return Utils.readOneLine(INTELLI_PLUG_ECO_MODE_PATH).trim().equals("1");
    }

    public static boolean hasIntelliPlug() {
        return Utils.fileExists(INTELLI_PLUG_PATH);
    }

    public static boolean hasIntelliPlugEcoMode() {
        return Utils.fileExists(INTELLI_PLUG_ECO_MODE_PATH);
    }

    /**
     * Get total number of cpus
     *
     * @return total number of cpus
     */
    public static int getNumOfCpus() {
        int numOfCpu = 1;
        final String numOfCpus = Utils.readOneLine(PRESENT_CPUS);
        if (numOfCpus != null && !numOfCpus.isEmpty()) {
            final String[] cpuCount = numOfCpus.split("-");
            if (cpuCount.length > 1) {
                try {
                    numOfCpu = Integer.parseInt(cpuCount[1]) - Integer.parseInt(cpuCount[0]) + 1;
                    if (numOfCpu < 0) {
                        numOfCpu = 1;
                    }
                } catch (NumberFormatException ex) {
                    numOfCpu = 1;
                }
            }
        }
        return numOfCpu;
    }

    public static String getValue(final int cpu, final String action) {
        return getOrSetValue(cpu, "", action, false);
    }

    public static void setValue(final int cpu, final String value, final String action) {
        getOrSetValue(cpu, value, action, true);
    }

    private static String getOrSetValue(final int cpu, final String value,
            final String action, final boolean set) {
        String path = "";
        final String pathOnline = getOnlinePath(cpu);

        if (action.equals(ACTION_FREQ_MAX)) {
            path = getMaxCpuFrequencyPath(cpu);
        } else if (action.equals(ACTION_FREQ_MIN)) {
            path = getMinCpuFrequencyPath(cpu);
        } else if (action.equals(ACTION_GOV)) {
            path = getGovernorPath(cpu);
        }

        if (set) {
            if (!path.isEmpty()) {
                // Bring them online to apply values on all cores
                final StringBuilder sb = new StringBuilder();
                if (!pathOnline.isEmpty()) {
                    sb.append(Utils.getWriteCommand(pathOnline, "0"));
                    sb.append(Utils.getWriteCommand(pathOnline, "1"));
                }
                sb.append(Utils.getWriteCommand(path, value));
                Utils.runRootCommand(sb.toString());
            }
        } else {
            if (!path.isEmpty() && Utils.fileExists(path)) {
                final String result = Utils.readOneLine(path);
                return (result != null ? result.trim() : "0");
            } else {
                return "0";
            }
        }

        return null;
    }

    /**
     * Gets available schedulers from file
     *
     * @return available schedulers
     */
    public static String[] getAvailableIOSchedulers() {
        String[] schedulers = null;
        final String[] aux = Utils.readStringArray(IO_SCHEDULER_PATH[0]);
        if (aux != null) {
            schedulers = new String[aux.length];
            for (int i = 0; i < aux.length; i++) {
                if (aux[i].charAt(0) == '[') {
                    schedulers[i] = aux[i].substring(1, aux[i].length() - 1);
                } else {
                    schedulers[i] = aux[i];
                }
            }
        }
        return schedulers;
    }

    /**
     * Get current IO Scheduler
     *
     * @return current io scheduler
     */
    public static String getIOScheduler(final int id) {
        String scheduler = null;
        final String[] schedulers = Utils.readStringArray(IO_SCHEDULER_PATH[id]);
        if (schedulers != null) {
            for (String s : schedulers) {
                if (s.charAt(0) == '[') {
                    scheduler = s.substring(1, s.length() - 1);
                    break;
                }
            }
        }
        return scheduler;
    }

    public static String getAvailableGovernors() {
        return Utils.readOneLine(GOV_AVAILALBLE_PATH);
    }

    /**
     * Convert to MHz and append a tag
     *
     * @param mhzString The string to convert to MHz
     * @return tagged and converted String
     */
    public static String toMHz(final String mhzString) {
        int value = -1;
        if (mhzString != null && !mhzString.isEmpty()) {
            try {
                value = Integer.parseInt(mhzString) / 1000;
            } catch (NumberFormatException exc) {
                value = -1;
            }
        }

        if (value != -1) {
            return String.valueOf(value) + " MHz";
        } else {
            if (Application.applicationContext != null) {
                return Application.applicationContext.getString(R.string.core_offline);
            } else {
                return "Offline";
            }
        }
    }

    /**
     * Convert from MHz to its original frequency
     *
     * @param mhzString The MHz string to convert to a frequency
     * @return the original frequency
     */
    public static String fromMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            return String.valueOf(Integer.parseInt(mhzString.replace(" MHz", "")) * 1000);
        } else { return "0"; }
    }

    public static String restore(final DatabaseHandler db) {
        final StringBuilder sbCmd = new StringBuilder();

        final List<DataItem> items = db.getAllItems(
                DatabaseHandler.TABLE_BOOTUP, DatabaseHandler.CATEGORY_CPU);
        String tmpString;
        int tmpInt = -1;
        for (final DataItem item : items) {
            tmpString = item.getName();
            if (tmpString != null && !tmpString.contains("io")) {
                try {
                    tmpInt = Integer.parseInt(
                            String.valueOf(tmpString.charAt(tmpString.length() - 1)));
                } catch (Exception exc) {
                    tmpInt = -1;
                }
            }
            if (tmpInt != -1) {
                final String path = CpuUtils.getOnlinePath(tmpInt);
                if (path != null && !path.isEmpty()) {
                    sbCmd.append(Utils.getWriteCommand(path, "0"));
                    sbCmd.append(Utils.getWriteCommand(path, "1"));
                }
            }
            sbCmd.append(Utils.getWriteCommand(item.getFileName(), item.getValue()));
        }

        return sbCmd.toString();
    }

    public static String[] getAvailableFrequencies() {
        final String freqsRaw = Utils.readOneLine(FREQ_AVAILABLE_PATH);
        if (freqsRaw != null && !freqsRaw.isEmpty()) {
            return freqsRaw.split(" ");
        }
        return null;
    }

    //==============================================================================================
    // Events
    //==============================================================================================
    public static void getCpuFreqEvent() {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(FREQ_AVAILABLE_PATH).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(FREQ0_MAX_PATH).append(" 2> /dev/null;");
            cmd.append("echo -n \"]\";");
            cmd.append("cat ").append(FREQ0_MIN_PATH).append(" 2> /dev/null;");
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
                    String tmpMax = "", tmpMin = "";

                    for (final String s : result) {
                        if (s.charAt(0) == '[') {
                            tmpMax = s.substring(1, s.length());
                        } else if (s.charAt(0) == ']') {
                            tmpMin = s.substring(1, s.length());
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String max = tmpMax;
                    final String min = tmpMin;
                    final String[] avail = tmpList.toArray(new String[tmpList.size()]);
                    Application.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(new CpuFreqEvent(avail, max, min));
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

    public static void getGovernorEvent() {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(GOV_AVAILALBLE_PATH).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(GOV0_CURRENT_PATH).append(" 2> /dev/null;");
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
                    String tmpString = "";

                    for (final String s : result) {
                        if (s.charAt(0) == '[') {
                            tmpString = s.substring(1, s.length());
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String gov = tmpString;
                    final String[] availGovs = tmpList.toArray(new String[tmpList.size()]);
                    Application.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(new GovernorEvent(availGovs, gov));
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

    public static void getIoSchedulerEvent() {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final String cmd = "cat " + IO_SCHEDULER_PATH[0] + " 2> /dev/null;";

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, false, cmd) {
                @Override
                public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    final List<String> result =
                            Arrays.asList(outputCollector.toString().split(" "));
                    final List<String> tmpList = new ArrayList<String>();
                    String tmpString = "";

                    for (final String s : result) {
                        if (s.charAt(0) == '[') {
                            tmpString = s.substring(1, s.length() - 1);
                            tmpList.add(tmpString);
                        } else {
                            tmpList.add(s);
                        }
                    }

                    final String scheduler = tmpString;
                    final String[] availableSchedulers =
                            tmpList.toArray(new String[tmpList.size()]);
                    Application.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(
                                    new IoSchedulerEvent(availableSchedulers, scheduler)
                            );
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

    public static String enableMpDecision(boolean start) {
        return (start ? "start mpdecision 2> /dev/null;\n" : "stop mpdecision 2> /dev/null;\n");
    }

}
