/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.flasher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import org.namelessrom.devicecontrol.R;

public class FlashCard extends BaseCard {
    public Button install;

    public FlashCard(Context context, final FlasherFragment flasherFragment) {
        super(context);
        LayoutInflater.from(context)
                .inflate(R.layout.merge_card_flasher_install, getContainer(), true);

        final RecoveryHelper recoveryHelper = new RecoveryHelper(flasherFragment.getActivity());
        final RebootHelper rebootHelper = new RebootHelper(recoveryHelper);

        install = (Button) findViewById(R.id.install);
        final CheckBox backup = (CheckBox) findViewById(R.id.backup);
        final CheckBox wipeCaches = (CheckBox) findViewById(R.id.wipecaches);
        final CheckBox wipeData = (CheckBox) findViewById(R.id.wipedata);

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
