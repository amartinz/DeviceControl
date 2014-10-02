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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.negusoft.holoaccent.activity.AccentActivity;
import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.bus.BusProvider;
import org.namelessrom.devicecontrol.bus.FlashItemEvent;
import org.namelessrom.devicecontrol.fragments.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

public class FilePickerActivity extends AccentActivity {

    private Fragment mCurrentFragment;

    @Override public int getOverrideAccentColor() {
        return Application.get().getAccentColor();
    }

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Application.get().isDarkTheme() ? R.style.BaseThemeDark : R.style.BaseThemeLight);

        setContentView(R.layout.activity_main);

        mCurrentFragment = buildFragment(getIntent());
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }

    @Override protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    private Fragment buildFragment(final Intent intent) {
        final String fileType = intent.getStringExtra(FilePickerFragment.ARG_FILE_TYPE);
        // Prepare bundle, containing the package name
        final Bundle bundle = new Bundle(1);
        bundle.putString(FilePickerFragment.ARG_FILE_TYPE, (fileType != null ? fileType : ""));

        // Bind bundle to fragment
        final Fragment f = new FilePickerFragment();
        f.setArguments(bundle);

        return f;
    }

    @Subscribe public void onFlashItemEvent(final FlashItemEvent event) {
        if (event == null) return;
        final Bundle b = new Bundle(1);
        final FlashItem item = event.getItem();
        b.putString("name", item.getName());
        b.putString("path", item.getPath());
        final Intent i = new Intent();
        i.putExtras(b);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override public void onBackPressed() {
        if (mCurrentFragment instanceof OnBackPressedListener
                && ((OnBackPressedListener) mCurrentFragment).onBackPressed()) {
            Logger.v(this, "onBackPressed()");
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
