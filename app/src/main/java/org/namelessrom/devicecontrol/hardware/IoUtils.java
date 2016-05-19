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

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.amartinz.execution.Command;
import at.amartinz.execution.Shell;
import at.amartinz.execution.ShellManager;
import timber.log.Timber;

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
        final Shell shell;
        if (new File(IO_SCHEDULER_PATH[0]).canRead()) {
            Timber.v("Using normal shell!");
            shell = ShellManager.get().getNormalShell();
        } else {
            Timber.v("Using root shell!");
            shell = ShellManager.get().getRootShell();
        }

        if (shell == null) {
            Timber.e("Could not open shell!");
            return;
        }

        final String cmd = String.format("cat \"%s\" 2> /dev/null;", IO_SCHEDULER_PATH[0]);
        final Command command = new IoCommand(cmd, listener);
        command.setOutputType(Command.OUTPUT_STRING);
        shell.add(command);
    }

    private static class IoCommand extends Command {
        private final IoSchedulerListener listener;

        public IoCommand(String cmd, IoSchedulerListener listener) {
            super(cmd);
            this.listener = listener;
        }

        @Override public void onCommandCompleted(int id, int exitCode) {
            super.onCommandCompleted(id, exitCode);

            final String output = getOutput();
            if (output == null) {
                return;
            }

            final List<String> result = Arrays.asList(output.split(" "));
            if (result.isEmpty()) {
                return;
            }

            final List<String> tmpList = new ArrayList<>();
            String tmpString = "";

            for (final String s : result) {
                if (TextUtils.isEmpty(s)) {
                    continue;
                }
                if (s.charAt(0) == '[') {
                    tmpString = s.substring(1, s.length() - 1);
                    tmpList.add(tmpString);
                } else {
                    tmpList.add(s);
                }
            }

            final String scheduler = tmpString;
            final String[] availableSchedulers = tmpList.toArray(new String[tmpList.size()]);
            App.HANDLER.post(new Runnable() {
                @Override public void run() {
                    listener.onIoScheduler(new IoScheduler(availableSchedulers, scheduler));
                }
            });
        }
    }
}
