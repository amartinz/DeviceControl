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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

public abstract class Card extends LinearLayout {

    private Context      mContext;
    private View         mView;
    private LinearLayout mHeaderLayout;
    private LinearLayout mCardLayout;
    private TextView     mTitleView;
    private View         mLayoutView;
    private ImageView    mButton;
    private String       mExpandedProperty;

    private boolean mExpanded = false;

    public Card(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        this(context, attrs, savedInstanceState, true);
    }

    public Card(Context context, AttributeSet attrs, Bundle savedInstanceState, boolean useAccent) {
        super(context, attrs);

        mContext = context;

        mExpandedProperty = getClass().getName() + ".expanded";

        String title = null;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Card);

        CharSequence s = a.getString(R.styleable.Card_title);
        if (s != null) {
            title = s.toString();
        }

        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.card, this, true);

        mHeaderLayout = (LinearLayout) mView.findViewById(R.id.header_layout);

        mCardLayout = (LinearLayout) mView.findViewById(R.id.card_layout);
        if (!isInEditMode()) {
            final int backgroundColor;
            if (useAccent) {
                backgroundColor = Application.get()
                        .isDarkTheme() ? R.color.dark_background : R.color.light_background;
                mView.findViewById(R.id.card_border_main)
                        .setBackgroundColor(Application.get().getAccentColor());
                mCardLayout.setBackgroundColor(getResources().getColor(backgroundColor));
            } else {
                final Drawable drawable = getResources().getDrawable(R.drawable.card_background);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mCardLayout.setBackground(drawable);
                } else {
                    //noinspection deprecation
                    mCardLayout.setBackgroundDrawable(drawable);
                }
            }
        }

        mButton = (ImageView) mView.findViewById(R.id.headerbutton);

        if (canExpand()) {
            mButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mExpanded) {
                        collapse();
                    } else {
                        expand();
                    }
                }

            });
        } else {
            mButton.setVisibility(View.GONE);
        }

        mTitleView = (TextView) mView.findViewById(R.id.title);
        mTitleView.setText(title);
        mTitleView.setTextColor(Application.get().getAccentColor());

        if (savedInstanceState != null) {
            mExpanded = savedInstanceState.getBoolean(mExpandedProperty, false);
            if (mExpanded) {
                expand();
            } else {
                collapse();
            }
        } else {
            collapse();
        }
    }

    public void expand() {
        mExpanded = true;
        mButton.setImageDrawable(DrawableHelper.applyAccentColorFilter(
                mContext.getResources().getDrawable(R.drawable.ic_collapse)));
    }

    public void collapse() {
        mExpanded = false;
        mButton.setImageDrawable(DrawableHelper.applyAccentColorFilter(
                mContext.getResources().getDrawable(R.drawable.ic_expand)));
    }

    protected boolean canExpand() {
        return true;
    }

    protected boolean isExpanded() {
        return mExpanded;
    }

    public void saveState(Bundle outState) {
        outState.putBoolean(mExpandedProperty, mExpanded);
    }

    public void setTitle(int resourceId) {
        mTitleView.setText(resourceId);
    }

    public void setTitle(String text) {
        mTitleView.setText(text);
    }

    public void hideHeaderLayout() { mHeaderLayout.setVisibility(View.GONE); }

    protected void setLayoutId(int id) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mLayoutView = inflater.inflate(id, mCardLayout, true);
    }

    protected View findLayoutViewById(int id) {
        return mLayoutView.findViewById(id);
    }
}
