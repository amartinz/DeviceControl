/*
 * <!--
 *    Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * -->
 */
package org.namelessrom.devicecontrol;

import android.content.Context;
import android.os.Vibrator;

import javax.inject.Inject;

public class Logger extends alexander.martinz.libs.logger.Logger {
    public static Logger logger;

    @Inject Vibrator vibrator;

    private Logger(Context context) {
        ((App) context.getApplicationContext()).getAppComponent().inject(this);
    }

    public static Logger get(Context context) {
        if (logger == null) {
            logger = new Logger(context);
        }
        return logger;
    }

    public void debugVibrate() {
        if (BuildConfig.DEBUG) {
            vibrator.cancel();
            vibrator.vibrate(100);
        }
    }
}
