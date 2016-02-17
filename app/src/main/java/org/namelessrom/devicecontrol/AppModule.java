/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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

import android.content.Context;
import android.os.PowerManager;
import android.os.Vibrator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    App app;

    public AppModule(App app) {
        this.app = app;
    }

    @Provides @Singleton App providesApplication() {
        return app;
    }

    @Provides @Singleton PowerManager providesPowerManager() {
        return (PowerManager) app.getSystemService(Context.POWER_SERVICE);
    }

    @Provides @Singleton Vibrator providesVibrator() {
        return (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
    }

}
