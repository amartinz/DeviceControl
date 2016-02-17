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

import org.namelessrom.devicecontrol.modules.flasher.RebootHelper;

import javax.inject.Singleton;

import dagger.Component;

@Singleton @Component(modules = { AppModule.class })
public interface AppComponent {
    void inject(App app);

    void inject(RebootHelper rebootHelper);

    void inject(Logger logger);

    final class Initializer {
        static AppComponent init(final App app) {
            return DaggerAppComponent.builder()
                    .appModule(new AppModule(app))
                    .build();
        }

        private Initializer() { }
    }
}
