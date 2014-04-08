package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.widgets.AnimatedCheckbox;

public class CustomListPreference extends ListPreference {

    private String  color = "#FFFFFF";
    private boolean mHide = false;

    private View             mSeperator;
    private AnimatedCheckbox mCheckBox;

    public CustomListPreference(Context context) {
        super(context);
        mHide = true;
        setLayoutResource(R.layout.preference_checkbox);
    }

    public CustomListPreference(final Context context, final boolean hideBootup) {
        super(context);
        mHide = hideBootup;
        setLayoutResource(R.layout.preference_checkbox);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
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
        this.color = color;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setTextColor(Color.parseColor(color));
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView summary = (TextView) view.findViewById(android.R.id.summary);
        summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

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
