/*
 * Copyright (C) 2013 - 2016 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import at.amartinz.hardware.device.Device;
import at.amartinz.hardware.device.ProcessorInfo;

public class DrawerHeaderTitle extends FrameLayout {
    public DrawerHeaderTitle(Context context) {
        super(context);
        init(context);
    }

    public DrawerHeaderTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawerHeaderTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DrawerHeaderTitle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(@NonNull Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.drawer_title_view, this, true);

        final TextView title = (TextView) findViewById(R.id.drawer_header_title);
        final TextView subTitle = (TextView) findViewById(R.id.drawer_header_subtitle);

        if (isInEditMode()) {
            title.setText(R.string.dummy_header_title);
            subTitle.setText(R.string.dummy_header_subtitle);
        } else {
            final Device device = Device.get(context);
            title.setText(device.getModelStringShort());

            final String bitString = (ProcessorInfo.is64BitStatic() ? "64-bit" : "32-bit");
            subTitle.setText(String.format("Android %s / %s", device.platformVersion, bitString));
        }
    }
}
