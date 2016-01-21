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
import android.widget.FrameLayout;

import org.namelessrom.devicecontrol.R;

public abstract class BaseCard extends FrameLayout {
    private FrameLayout mContainer;

    public BaseCard(final Context context) {
        super(context, null);
        final View v = LayoutInflater.from(context).inflate(R.layout.card_with_container, this, false);
        super.addView(v);

        mContainer = (FrameLayout) findViewById(R.id.layout_container);
    }

    public FrameLayout getContainer() {
        return mContainer;
    }

}
