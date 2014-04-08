package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.widgets.AnimatedCheckbox;

public class CustomCheckBoxPreference extends CheckBoxPreference {

    private String  mColor = "#FFFFFF";
    private boolean mHide  = false;
    private String mValue;

    private View             mSeperator;
    private AnimatedCheckbox mCheckBox;

    public CustomCheckBoxPreference(Context context) {
        super(context);
        mHide = true;
        setLayoutResource(R.layout.preference_checkbox);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
        setLayoutResource(R.layout.preference_checkbox);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, context);
        setLayoutResource(R.layout.preference_checkbox);
    }

    private void init(final AttributeSet attrs, final Context context) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(
                    attrs, R.styleable.CustomCheckBoxPreference);
            if (a != null) {
                mHide = a.getBoolean(R.styleable.CustomCheckBoxPreference_hide_boot, mHide);
                a.recycle();
            }
        }
    }

    public void setTitleColor(String color) {
        mColor = color;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        final TextView mTitle = (TextView) view.findViewById(android.R.id.title);
        mTitle.setTextColor(Color.parseColor(mColor));
        mTitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView mSummary = (TextView) view.findViewById(android.R.id.summary);
        mSummary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        mSeperator = view.findViewById(R.id.separator);
        mCheckBox = (AnimatedCheckbox) view.findViewById(R.id.cb);
        hideBootup();
    }

    public void setOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener) {
        if (mCheckBox != null) {
            mCheckBox.setOnCheckedChangeListener(listener);
        }
    }

    public void hideBootup() {
        if (mHide) {
            if (mSeperator != null) {
                mSeperator.setVisibility(View.GONE);
            }
            if (mCheckBox != null) {
                mCheckBox.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean isPersistent() {
        return false;
    }

    @Override
    protected boolean shouldPersist() {
        return false;
    }

}
