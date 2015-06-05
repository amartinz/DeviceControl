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
package org.namelessrom.devicecontrol.activities;

import android.app.ActivityManager.TaskDescription;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;

public abstract class BaseActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        final boolean isDarkTheme = AppResources.get().isDarkTheme();
        setTheme(isDarkTheme ? R.style.AppTheme_Dark : R.style.AppTheme_Light);

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // WTF! IRIS506Q android version "unknown"
            try {
                getWindow().setStatusBarColor(AppResources.get().getPrimaryColor());
            } catch (Exception e) {
                Log.e("BaseActivity", "get a stone and throw it at your device vendor", e);
            }

            // color recents tab
            final Bitmap appIcon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher_devicecontrol);

            final TaskDescription taskDescription = new TaskDescription(String.valueOf(getTitle()),
                    appIcon, AppResources.get().getAccentColor());
            setTaskDescription(taskDescription);
        }
    }

}
