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

public class IoUtils {

    public static final String[] IO_SCHEDULER_PATH = {
            "/sys/block/mmcblk0/queue/scheduler",
            "/sys/block/mmcblk1/queue/scheduler"
    };
    public static final String[] READ_AHEAD_PATH = {
            "/sys/block/mmcblk0/queue/read_ahead_kb",
            "/sys/block/mmcblk1/queue/read_ahead_kb"
    };

    public static class IoScheduler {
        public final String[] available;
        public final String current;

        public IoScheduler(final String[] availableIoSchedulers, final String ioScheduler) {
            available = availableIoSchedulers;
            current = ioScheduler;
        }
    }

    public interface IoSchedulerListener {
        void onIoScheduler(final IoScheduler ioScheduler);
    }

    private static IoUtils sInstance;

    private IoUtils() { }

    public static IoUtils get() {
        if (sInstance == null) {
            sInstance = new IoUtils();
        }
        return sInstance;
    }

    /**
     * Gets available schedulers from file
     *
     * @return available schedulers
     */
    @Nullable public String[] getAvailableIoSchedulers() {
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

    public void getIoScheduler(final IoSchedulerListener listener) {
        try {
            final Shell mShell = RootTools.getShell(true);
            if (mShell == null) { throw new Exception("Shell is null"); }

            final String cmd = "cat " + IO_SCHEDULER_PATH[0] + " 2> /dev/null;";

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture cmdCapture = new CommandCapture(0, cmd) {
                @Override public void commandOutput(int id, String line) {
                    outputCollector.append(line);
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
                        @Override public void run() {
                            listener.onIoScheduler(new IoScheduler(availableSchedulers, scheduler));
                        }
                    });

                }
            };

            if (mShell.isClosed()) { throw new Exception("Shell is closed"); }
            mShell.add(cmdCapture);
        } catch (Exception exc) {
            Logger.e(CpuUtils.class, "Error: " + exc.getMessage());
        }
    }
}
