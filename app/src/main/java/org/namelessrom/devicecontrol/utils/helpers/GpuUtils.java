package org.namelessrom.devicecontrol.utils.helpers;

import android.app.Activity;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.events.GpuEvent;
import org.namelessrom.devicecontrol.utils.BusProvider;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class GpuUtils implements PerformanceConstants {

    public static String[] getFreqToMhz(final String file) throws IOException {
        final ArrayList<String> names = new ArrayList<String>();
        Utils.setPermissions(file);

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
        final StringBuilder sb = new StringBuilder();

        final String gpuMax = PreferenceHelper.getString(PREF_MAX_GPU, "");
        if (gpuMax != null && !gpuMax.isEmpty()) {
            sb.append(Utils.getWriteCommand(GPU_MAX_FREQ_FILE, gpuMax));
        }

        final String gpuGov = PreferenceHelper.getString(PREF_GPU_GOV, "");
        if (gpuGov != null && !gpuGov.isEmpty()) {
            sb.append(Utils.getWriteCommand(GPU_GOV_PATH, gpuGov));
        }

        return sb.toString();
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
    public static void getOnGpuEvent(final Activity activity) {
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
                    final String[] result = outputCollector.toString().split(" ");
                    final int length = result.length;
                    String tmpMax = "", tmpGov = "";
                    String[] tmpArray = new String[length - 2];

                    String s;
                    for (int i = 0; i < length; i++) {
                        s = result[i];
                        if (s.charAt(0) == '[') {
                            tmpMax = s.substring(1, s.length());
                        } else if (s.charAt(0) == ']') {
                            tmpGov = s.substring(1, s.length());
                        } else {
                            tmpArray[i] = s;
                        }
                    }

                    final String[] avail = tmpArray;
                    final String max = tmpMax;
                    final String gov = tmpGov;
                    activity.runOnUiThread(new Runnable() {
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
