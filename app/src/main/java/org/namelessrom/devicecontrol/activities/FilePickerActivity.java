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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.modules.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.modules.filepicker.FilePickerListener;
import org.namelessrom.devicecontrol.modules.filepicker.FlashItem;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

public class FilePickerActivity extends BaseActivity implements FilePickerListener {

    private Fragment mCurrentFragment;

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCurrentFragment = new FilePickerFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container, mCurrentFragment)
                .commit();
    }

    @Override public void onFlashItemPicked(final FlashItem flashItem) {
        final Bundle b = new Bundle(1);
        b.putString("name", flashItem.getName());
        b.putString("path", flashItem.getPath());
        final Intent i = new Intent();
        i.putExtras(b);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final Uri uri = Uri.fromFile(new File(flashItem.getPath()));
            Timber.v("Uri: %s", uri.toString());
            i.setDataAndNormalize(uri);
        } else {
            // we do not have the convenient normalizing method so we need to "normalize" ourselves
            String path;
            try {
                path = new File(flashItem.getPath()).toURI().normalize().getPath();
            } catch (Exception exc) {
                path = flashItem.getPath();
            }

            Uri uri = Uri.fromFile(new File(path));
            String scheme = uri.getScheme();
            if (scheme != null) {
                scheme = scheme.toLowerCase(Locale.ROOT);
            }

            // finally done, lets build that garbage back to an uri…
            uri = new Uri.Builder().scheme(scheme).path(path).build();

            Timber.v("Legacy | Uri: %s", uri.toString());
            i.setData(uri);
        }
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override public void onFilePicked(final File ignored) { }

    @Override public void onBackPressed() {
        if (mCurrentFragment instanceof OnBackPressedListener && ((OnBackPressedListener) mCurrentFragment).onBackPressed()) {
            Timber.v("onBackPressed()");
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

}
