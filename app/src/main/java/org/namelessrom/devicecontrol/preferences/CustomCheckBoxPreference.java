package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class CustomCheckBoxPreference extends CheckBoxPreference {

    private String color = "#FFFFFF";
    private String value;

    public CustomCheckBoxPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference);
    }

    public void setTitleColor(String color) {
        this.color = color;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final TextView mTitle = (TextView) view.findViewById(android.R.id.title);
        mTitle.setTextColor(Color.parseColor(color));
        mTitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView mSummary = (TextView) view.findViewById(android.R.id.summary);
        mSummary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
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
