/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.utils.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.transformers.DepthPageTransformer;
import org.namelessrom.devicecontrol.utils.transformers.ZoomOutPageTransformer;

/**
 * Created by alex on 18.12.13.
 */
public class JfViewPager extends ViewPager {

    public JfViewPager(Context context) {
        this(context, null);
    }

    public JfViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setClipChildren(false);

        this.setOffscreenPageLimit(3);

        if (PreferenceHelper.getCustomAnimations()) {
            switch (PreferenceHelper.getTransformerId()) {
                default:
                case 0:
                    this.setPageTransformer(true, new ZoomOutPageTransformer());
                    break;
                case 1:
                    this.setPageTransformer(true, new DepthPageTransformer());
                    break;
            }
        }

    }
}
