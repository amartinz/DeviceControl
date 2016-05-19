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
package org.namelessrom.devicecontrol.modules.appmanager;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.namelessrom.devicecontrol.R;

import hugo.weaving.DebugLog;

public class AppIconImageView extends ImageView {
    public AppIconImageView(Context context) {
        this(context, null);
    }

    public AppIconImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppIconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @DebugLog public void loadImage(AppItem appItem) {
        Glide.with(getContext())
                .load(resourceToUri(appItem.getPackageName(), appItem.getApplicationInfo().icon))
                .placeholder(R.mipmap.ic_launcher_default)
                .into(this);
    }

    private Uri resourceToUri(String packageName, int resourceId) {
        return Uri.parse(String.format("android.resource://%s/%s", packageName, resourceId));
    }

}
