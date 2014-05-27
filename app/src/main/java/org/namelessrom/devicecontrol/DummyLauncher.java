/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.stericson.roottools.RootTools;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Dummy Activity, used as Launcher.
 * The AddTaskActivity still stays fully functional even if we deactivate this class component.
 * <p/>
 * If we deactivate this activity, the launcher icon disappears.
 */
public class DummyLauncher extends Activity {

    @InjectView(R.id.launcher_status) TextView mStatus;
    @InjectView(R.id.btn_action)      Button   mAction;

    private final boolean hasRoot    = RootTools.isRootAvailable() && RootTools.isAccessGiven();
    private final boolean hasBusyBox = RootTools.isBusyboxAvailable();

    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_launcher);
        ButterKnife.inject(this);

        if (hasRoot && hasBusyBox) {
            startActivity(new Intent(DummyLauncher.this, MainActivity.class));
            finish();
        } else if (hasRoot) {
            mStatus.setText(getString(R.string.app_warning_busybox, getString(R.string.app_name)));
            mAction.setText(R.string.get_busybox);
        } else {
            mStatus.setText(getString(R.string.app_warning_root, getString(R.string.app_name)));
            mAction.setText(R.string.get_superuser);
        }
    }

    @OnClick(R.id.btn_action) void onAction() {
        if (!hasRoot) {
            RootTools.offerSuperUser(DummyLauncher.this);
        } else if (!hasBusyBox) {
            RootTools.offerBusyBox(DummyLauncher.this);
        }
    }

    @OnClick(R.id.btn_exit) void onExit() {
        finish();
    }

}
