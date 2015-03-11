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
package org.namelessrom.devicecontrol.utils;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;

/**
 * Helps with ddddddrawwabllessr5hhwr5hbwb
 */
public class DrawableHelper {

    @Nullable public static Drawable applyColorFilter(final Drawable drawable, final int color) {
        if (drawable == null) {
            Logger.w("DrawableHelper", "drawable is null!");
            return null;
        }
        final LightingColorFilter lightingColorFilter = new LightingColorFilter(Color.BLACK, color);
        drawable.setColorFilter(lightingColorFilter);
        return drawable;
    }

    public static Drawable applyAccentColorFilter(final int drawableRes) {
        return applyColorFilter(Application.get().getResources()
                .getDrawable(drawableRes), Application.get().getAccentColor());
    }

    public static Drawable applyAccentColorFilter(final Drawable drawable) {
        return applyColorFilter(drawable, Application.get().getAccentColor());
    }

}
