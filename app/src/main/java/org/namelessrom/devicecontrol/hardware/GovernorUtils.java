package org.namelessrom.devicecontrol.hardware;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Easy interaction with governors
 */
public class GovernorUtils {

    public static class Governor {
        public final String[] available;
        public final String   current;

        public Governor(final String[] availableGovernors, final String governor) {
            available = availableGovernors;
            current = governor;
        }
    }

    public interface GovernorListener {
        public void onGovernor(final Governor governor);
    }

    private static GovernorUtils sInstance;

    private GovernorUtils() { }

    public static GovernorUtils get() {
        if (sInstance == null) {
            sInstance = new GovernorUtils();
        }
        return sInstance;
    }

    public String getGovernorPath(final int cpu) {
        switch (cpu) {
            default:
            case 0:
                return Constants.GOV0_CURRENT_PATH;
            case 1:
                return Constants.GOV1_CURRENT_PATH;
            case 2:
                return Constants.GOV2_CURRENT_PATH;
            case 3:
                return Constants.GOV3_CURRENT_PATH;
        }
    }

    public String[] getAvailableGovernors() {
        String[] govArray = null;
        final String govs = Utils.readOneLine(Constants.GOV_AVAILALBLE_PATH);

        if (govs != null && !govs.isEmpty()) {
            govArray = govs.split(" ");
        }

        return govArray;
    }

    public void getGovernor(final GovernorListener listener) {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final StringBuilder cmd = new StringBuilder();
            cmd.append("command=$(");
            cmd.append("cat ").append(Constants.GOV_AVAILALBLE_PATH).append(" 2> /dev/null;");
            cmd.append("echo -n \"[\";");
            cmd.append("cat ").append(Constants.GOV0_CURRENT_PATH).append(" 2> /dev/null;");
            cmd.append(");").append("echo $command | tr -d \"\\n\"");
            Logger.v(CpuUtils.class, cmd.toString());

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, false, cmd.toString()) {
                @Override public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    Logger.v(CpuUtils.class, line);
                }

                @Override public void commandCompleted(int id, int exitcode) {
                    final List<String> result =
                            Arrays.asList(outputCollector.toString().split(" "));
                    final List<String> tmpList = new ArrayList<String>();
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
