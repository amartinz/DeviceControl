/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.devicecontrol.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.namelessrom.devicecontrol.services.TaskerService;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

import java.util.Calendar;

import org.namelessrom.devicecontrol.Application;

/**
 * Helper class to schedule alarms
 */
public class AlarmHelper implements DeviceConstants {

    /**
     * Schedules the TaskerService with the FSTRIM flag.
     *
     * @param context  The context
     * @param interval The interval in minutes
     */
    public static void setAlarmFstrim(Context context, int interval) {
        Intent i = new Intent(context, TaskerService.class);
        i.setAction(ACTION_TASKER_FSTRIM);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Application.alarmManager == null) {
            Application.alarmManager = (AlarmManager)
                    context.getSystemService(Context.ALARM_SERVICE);
        }
        Application.alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(),
                60000 * interval, pi);
    }

    /**
     * Cancels the fstrim alarm
     *
     * @param context The context
     */
    public static void cancelAlarmFstrim(Context context) {
        Intent i = new Intent(context, TaskerService.class);
        i.setAction(ACTION_TASKER_FSTRIM);
        PendingIntent pi = PendingIntent.getService(
                context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        Application.alarmManager.cancel(pi);
    }

}
