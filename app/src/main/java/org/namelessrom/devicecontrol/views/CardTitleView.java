package org.namelessrom.devicecontrol.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CardTitleView extends LinearLayout {
    @BindView(android.R.id.icon) ImageView icon;
    @BindView(android.R.id.title) TextView title;
    @BindView(android.R.id.content) FrameLayout content;

    private int iconResId = Integer.MIN_VALUE;
    private int iconTint = Integer.MIN_VALUE;
    private int titleResId = Integer.MIN_VALUE;

    public CardTitleView(Context context) {
        super(context);
        init(null);
    }

    public CardTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CardTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public CardTitleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    protected void init(@Nullable AttributeSet attrs) {
        setOrientation(LinearLayout.VERTICAL);
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.card_view_title, this, true);

        ButterKnife.bind(this);

        parseAttrsIfPossible(attrs);

        if (iconResId != Integer.MIN_VALUE) {
            if (iconTint != Integer.MIN_VALUE) {
                final Drawable drawable = ContextCompat.getDrawable(getContext(), iconResId).mutate();
                icon.setImageDrawable(DrawableHelper.applyColorFilter(drawable, iconTint));
            } else {
                icon.setImageResource(iconResId);
            }
        } else {
            icon.setVisibility(View.GONE);
        }

        if (titleResId != Integer.MIN_VALUE) {
            title.setText(titleResId);
        }
    }

    protected void parseAttrsIfPossible(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CardTitleView);

        iconResId = a.getResourceId(R.styleable.CardTitleView_icon, Integer.MIN_VALUE);
        iconTint = a.getColor(R.styleable.CardTitleView_iconTint, Integer.MIN_VALUE);
        titleResId = a.getResourceId(R.styleable.CardTitleView_title, Integer.MIN_VALUE);

        a.recycle();
    }

    public FrameLayout getContentView() {
        return content;
    }

    @Override public void addView(View child) {
        if (content != null) {
            content.addView(child);
            return;
        }
        super.addView(child);
    }

    @Override public void addView(View child, int index) {
        if (content != null) {
            content.addView(child, index);
            return;
        }
        super.addView(child, index);
    }

    @Override public void addView(View child, int width, int height) {
        if (content != null) {
            content.addView(child, width, height);
            return;
        }
        super.addView(child, width, height);
    }

    @Override public void addView(View child, ViewGroup.LayoutParams params) {
        if (content != null) {
            content.addView(child, params);
            return;
        }
        super.addView(child, params);
    }

    @Override public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (content != null) {
            content.addView(child, index, params);
            return;
        }
        super.addView(child, index, params);
    }
}
