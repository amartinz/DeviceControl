package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

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
    }

}
