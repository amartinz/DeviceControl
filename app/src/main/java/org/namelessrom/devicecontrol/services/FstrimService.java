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
package org.namelessrom.devicecontrol.services;

import android.app.IntentService;
import android.content.Intent;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.FileOutputStream;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.Command;
import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class FstrimService extends IntentService {
    public static final String ACTION_TASKER_FSTRIM = "action_tasker_fstrim";

    private FileOutputStream fileOutputStream;

    public FstrimService() {
        super("FstrimService");
    }

    @Override protected void onHandleIntent(final Intent intent) {
        if (intent == null || !ACTION_TASKER_FSTRIM.equals(intent.getAction())) {
            return;
        }
        Timber.v("FSTRIM RUNNING");

        final String path = getFilesDir().getAbsolutePath() + DeviceConstants.DC_LOG_FILE_FSTRIM;
        final String cmd = "date;"
                           + BusyBox.callBusyBoxApplet("fstrim", "-v /cache;")
                           + BusyBox.callBusyBoxApplet("fstrim", "-v /data;")
                           + BusyBox.callBusyBoxApplet("fstrim", "-v /system;");

        try {
            fileOutputStream = new FileOutputStream(path);
        } catch (Exception ignored) { }

        final Command command = new Command(cmd) {
            @Override public void onCommandOutput(int id, String line) {
                super.onCommandOutput(id, line);
                Timber.v("Result: %s", line);
                writeLog(line);
            }

            @Override public void onCommandCompleted(int id, int exitcode) {
                super.onCommandCompleted(id, exitcode);
                try {
                    writeLog("\n\n");
                } catch (Exception ignored) { } finally {
                    Utils.closeQuietly(fileOutputStream);
                }
            }
        };

        RootShell.fireAndForget(command);
    }

    private void writeLog(String line) {
        if (fileOutputStream != null) {
            try {
                fileOutputStream.write((line + '\n').getBytes());
            } catch (Exception ignored) { }
        }
    }

}
