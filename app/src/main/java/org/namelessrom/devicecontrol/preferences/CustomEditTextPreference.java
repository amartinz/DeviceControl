package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import butterknife.ButterKnife;

public class CustomEditTextPreference extends EditTextPreference {

    private String color = "#FFFFFF";

    public CustomEditTextPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    public void setTitleColor(final String color) { this.color = color; }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        final TextView title = ButterKnife.findById(view, android.R.id.title);
        title.setTextColor(Color.parseColor(color));
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView summary = ButterKnife.findById(view, android.R.id.summary);
        summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    @Override
    public boolean isPersistent() { return false; }

    @Override
    protected boolean shouldPersist() { return false; }

}
