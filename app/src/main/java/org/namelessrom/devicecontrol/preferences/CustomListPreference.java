package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.R;

public class CustomListPreference extends ListPreference {

    public CustomListPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}
