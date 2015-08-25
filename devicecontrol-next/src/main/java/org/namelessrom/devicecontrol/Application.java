/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences.RxSharedPreferences;

import alexander.martinz.libs.logger.Logger;
import rx.functions.Action1;

public class Application extends android.app.Application {
    private CustomTabsHelper mCustomTabsHelper;

    @Override public void onCreate() {
        super.onCreate();
        // enable until we hit the preference
        Logger.setEnabled(true);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final RxSharedPreferences rxPreferences = RxSharedPreferences.create(sharedPreferences);

        rxPreferences.getBoolean(getString(R.string.pref_enable_logger), BuildConfig.DEBUG).asObservable()
                .subscribe(new Action1<Boolean>() {
                    @Override public void call(Boolean enableLogger) {
                        Logger.setEnabled(enableLogger);
                        Logger.d(this, "Logger enabled -> %s", Logger.getEnabled());
                    }
                });

        mCustomTabsHelper = new CustomTabsHelper(getApplicationContext());
    }

    public CustomTabsHelper getCustomTabsHelper() {
        return mCustomTabsHelper;
    }
}
