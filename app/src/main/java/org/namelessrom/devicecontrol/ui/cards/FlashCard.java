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
package org.namelessrom.devicecontrol.ui.cards;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.fragments.tools.FlasherFragment;
import org.namelessrom.devicecontrol.utils.RebootHelper;
import org.namelessrom.devicecontrol.utils.RecoveryHelper;

public class FlashCard extends LinearLayout {
    public Button install;

    public FlashCard(Context context, final FlasherFragment flasherFragment) {
        super(context, null);
        final int resId;
        if (Application.get().isDarkTheme()) {
            resId = R.layout.card_install_dark;
        } else {
            resId = R.layout.card_install_light;
        }
        LayoutInflater.from(context).inflate(resId, this, true);

        final RecoveryHelper recoveryHelper = new RecoveryHelper(flasherFragment.getActivity());
        final RebootHelper rebootHelper = new RebootHelper(recoveryHelper);

        install = (Button) findViewById(R.id.install);
        final CheckBox backup = (CheckBox) findViewById(R.id.backup);
        final CheckBox wipeCaches = (CheckBox) findViewById(R.id.wipedata);
        final CheckBox wipeData = (CheckBox) findViewById(R.id.wipecaches);

        install.setEnabled(false);
        install.setOnClickListener(new OnClickListener() {
            @Override public void onClick(final View view) {
                final String[] items = new String[flasherFragment.mFiles.size()];
                for (int i = 0; i < flasherFragment.mFiles.size(); i++) {
                    items[i] = flasherFragment.mFiles.get(i).getAbsolutePath();
                }
                rebootHelper.showRebootDialog(getContext(), items,
                        backup.isChecked(), wipeData.isChecked(), wipeCaches.isChecked());
            }

        });
    }


}
