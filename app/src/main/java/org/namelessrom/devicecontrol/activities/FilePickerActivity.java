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

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.FlashItemEvent;
import org.namelessrom.devicecontrol.fragments.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.fragments.tools.flasher.FlasherFragment;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

/**
 * Created by alex on 25.06.14.
 */
public class FilePickerActivity extends Activity {

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        getFragmentManager().beginTransaction()
                .add(R.id.container, buildFragment(getIntent()))
                .commit();
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
        b.putSerializable(FlasherFragment.EXTRA_FLASHITEM, event.getItem());
        final Intent i = new Intent();
        i.putExtras(b);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

}
