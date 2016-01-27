package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.R;

public class CustomTogglePreference extends SwitchPreference {

    private String value;

    public CustomTogglePreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomTogglePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    public CustomTogglePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference);
    }

    public void setValue(final String value) { this.value = value; }

    public String getValue() { return this.value; }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}
