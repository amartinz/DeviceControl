/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol;

import android.app.AlarmManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;

import com.stericson.RootTools.RootTools;

import org.namelessrom.devicecontrol.utils.PreferenceHelper;

import static org.namelessrom.devicecontrol.utils.constants.DeviceConstants.DC_FIRST_START;

public class Application extends android.app.Application {

    // Switch to your needs
    public static final boolean IS_LOG_DEBUG = false;

    public static boolean IS_SYSTEM_APP = false;
    public static boolean HAS_ROOT = false;

    public static AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Application.IS_LOG_DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

            RootTools.debugMode = true;
        }

        PreferenceHelper.getInstance(this);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        IS_SYSTEM_APP = getResources().getBoolean(R.bool.is_system_app);

        // we need to detect SU for some features :)
        new DetectSu().execute();

        // Set flag to enable BootUp receiver
        PreferenceHelper.setBoolean(DC_FIRST_START, false);

    }

    private class DetectSu extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HAS_ROOT = RootTools.isRootAvailable() && RootTools.isAccessGiven();
            return null;
        }
    }
}
