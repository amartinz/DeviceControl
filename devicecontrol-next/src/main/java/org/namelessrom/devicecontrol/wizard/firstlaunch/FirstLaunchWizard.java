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

package org.namelessrom.devicecontrol.wizard.firstlaunch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.wizard.WizardCallbacks;
import org.namelessrom.devicecontrol.wizard.WizardManager;

public class FirstLaunchWizard extends WizardManager<FirstLaunchWizard> {
    public boolean isSetupActive;

    public static boolean isFirstLaunch(@NonNull Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(R.string.pref_first_launch), true);
    }

    @SuppressLint("CommitPrefEdits") public static void setFirstLaunchDone(@NonNull Context context, boolean firstLaunchDone) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(context.getString(R.string.pref_first_launch), firstLaunchDone).commit();
    }

    public static FirstLaunchWizard create(@NonNull WizardCallbacks callbacks) {
        return new FirstLaunchWizard()
                .setCallbacks(callbacks)
                .addPage(new FirstLaunchWelcomePage())
                .addPage(new FirstLaunchWelcomePage())
                .addPage(new FirstLaunchWelcomePage());
    }
}
