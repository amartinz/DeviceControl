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
        return Application.applicationContext.getResources().getDimensionPixelSize(resId);
    }

}
