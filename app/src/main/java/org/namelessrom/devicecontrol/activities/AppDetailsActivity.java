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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.fragments.tools.AppListFragment;

public class AppDetailsActivity extends BaseActivity {

    public static final String ARG_PACKAGE_NAME = "package";

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override protected void onResume() {
        super.onResume();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, buildFragment(getIntent()))
                .commit();
    }

    private Fragment buildFragment(Intent intent) {
        String packageName = null;
        final Bundle args;
        if (intent != null) {
            args = intent.getExtras();
            packageName = (args != null) ? args.getString(ARG_PACKAGE_NAME) : null;
        } else {
            args = null;
        }

        if (packageName == null) {
            intent = (args == null) ? getIntent() : (Intent) args.getParcelable("intent");
            if (intent != null && intent.getData() != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        Logger.i(this, "packageName:" + String.valueOf(packageName));

        // Prepare bundle, containing the package name
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_PACKAGE_NAME, packageName);

        // Bind bundle to fragment
        final Fragment f = new AppListFragment();
        f.setArguments(bundle);

        return f;
    }

}
