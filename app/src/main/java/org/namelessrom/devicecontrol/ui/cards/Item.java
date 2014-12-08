/*
 * Copyright 2014 ParanoidAndroid Project
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.ui.cards;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class Item extends LinearLayout {

    public static interface OnItemClickListener {
        public void onClick(int id);
    }

    private ImageView           mIconView;
    private TextView            mTitleView;
    private OnItemClickListener mItemClickListener;
    private ColorStateList      mDefaultColors;
    private int                 mPressedColor;

    public Item(final Context context, AttributeSet attrs) {
        super(context, attrs);

        String title = null;
        Drawable icon = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Item);

        CharSequence s = a.getString(R.styleable.Item_itemTitle);
        if (s != null) {
            title = s.toString();
        }
        Drawable d = a.getDrawable(R.styleable.Item_itemIcon);
        if (d != null) {
            icon = d;
        }

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item, this, true);

        mTitleView = (TextView) view.findViewById(R.id.title);
        mTitleView.setText(title);
        mDefaultColors = mTitleView.getTextColors();
        mPressedColor = context.getResources().getColor(R.color.item_pressed);

        mIconView = (ImageView) view.findViewById(R.id.icon);
        mIconView.setImageDrawable(icon);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (!isEnabled()) {
                    return true;
                }

                Rect mViewRect = new Rect(view.getLeft(), view.getTop(), view.getRight(),
                        view.getBottom());
                boolean mTouchCancelled = !mViewRect.contains(view.getLeft() + (int) event.getX(),
                        view.getTop() + (int) event.getY());

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        setBackgroundColor(mPressedColor);
                        break;
                    case MotionEvent.ACTION_UP:
                        setBackgroundColor(context.getResources().getColor(
                                android.R.color.transparent));
                        mTitleView.setTextColor(mDefaultColors);
                        if (mItemClickListener != null && !mTouchCancelled) {
                            mItemClickListener.onClick(Item.this.getId());
                        }
                        break;
                }
                return true;
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public void setTitle(int resourceId) {
        mTitleView.setText(resourceId);
    }

    public void setTitle(String text) {
        mTitleView.setText(text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mTitleView != null) {
            if (enabled) {
                mTitleView.setTextColor(mDefaultColors);
            } else {
                mTitleView.setTextColor(getResources().getColor(R.color.card_text));
            }
        }
    }
}
