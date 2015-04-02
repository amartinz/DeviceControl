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
package org.namelessrom.devicecontrol.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoSwipeViewPager extends ViewPager {
    private boolean allowSwipe = true;

    public NoSwipeViewPager(Context context) { super(context); }

    public NoSwipeViewPager(Context context, AttributeSet attrs) { super(context, attrs); }

    @Override public boolean onInterceptTouchEvent(MotionEvent event) {
        return allowSwipe && super.onInterceptTouchEvent(event);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        return allowSwipe && super.onTouchEvent(event);
    }

    public boolean isAllowSwipe() {
        return allowSwipe;
    }

    public NoSwipeViewPager setAllowSwipe(boolean allowSwipe) {
        this.allowSwipe = allowSwipe;
        return this;
    }

}
