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
package org.namelessrom.devicecontrol.flasher;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;

public abstract class BaseCard extends LinearLayout {
    private FrameLayout mContainer;

    public BaseCard(final Context context) {
        super(context, null);
        final int resId;
        if (Application.get().isDarkTheme()) {
            resId = R.layout.card_install_dark;
        } else {
            resId = R.layout.card_install_light;
        }
        LayoutInflater.from(context).inflate(resId, this, true);
        mContainer = (FrameLayout) findViewById(R.id.layout_container);
    }

    public FrameLayout getContainer() {
        return mContainer;
    }

}
