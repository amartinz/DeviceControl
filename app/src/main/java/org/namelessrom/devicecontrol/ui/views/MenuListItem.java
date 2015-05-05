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
package org.namelessrom.devicecontrol.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public class MenuListItem extends LinearLayout {
    public MenuListItem(final Context context) {
        this(context, null);
    }

    public MenuListItem(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuListItem(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        createViews(context, attrs);
    }

    private void createViews(final Context context, final AttributeSet attrs) {
        final View view = LayoutInflater.from(context).inflate(R.layout.menu_main_list_item, this);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuListItem);
        final int titleRes = a.getResourceId(
                R.styleable.MenuListItem_menuItemTitle, R.string.empty);
        final int imageRes = a.getResourceId(
                R.styleable.MenuListItem_menuItemImage, R.drawable.transparent);
        a.recycle();

        final TextView title = (TextView) view.findViewById(android.R.id.text1);
        title.setText(titleRes);

        if (!isInEditMode()) {
            final ImageView image = (ImageView) view.findViewById(R.id.image);
            final Drawable icon = DrawableHelper.getDrawable(imageRes);
            DrawableHelper.applyAccentColorFilter(icon);
            image.setImageDrawable(icon);
        }
    }

}
