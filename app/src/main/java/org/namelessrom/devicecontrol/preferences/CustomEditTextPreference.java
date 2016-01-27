package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.R;

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}
