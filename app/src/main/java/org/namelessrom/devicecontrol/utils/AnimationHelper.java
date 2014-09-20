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

import android.animation.ObjectAnimator;
import android.view.View;

import org.namelessrom.devicecontrol.Application;

/**
 * Helps with animations, makes my life easier
 */
public class AnimationHelper {

    /**
     * Animates a single view on the x axis with a given duration, start and end
     *
     * @param v        The view to animate
     * @param duration The duration of the animation
     * @param start    The start point
     * @param end      The end point
     */
    public static void animateX(final View v, final int duration, final int start, final int end) {
        final ObjectAnimator outAnim = ObjectAnimator.ofFloat(v, "x", start, end);
        outAnim.setDuration(duration);
        outAnim.start();
    }

    /**
     * Gets a dimension from the resources and returns it in pixels
     *
     * @param resId The dimension to get
     * @return The dimension in pixels
     */
    public static int getDp(final int resId) {
        return Application.get().getResources().getDimensionPixelSize(resId);
    }

}
