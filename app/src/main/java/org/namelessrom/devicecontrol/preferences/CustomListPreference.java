package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.negusoft.holoaccent.preference.ListPreference;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;

import dreamers.graphics.RippleDrawable;

public class CustomListPreference extends ListPreference {

    public CustomListPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    @Override protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        if (isSelectable()) {
            RippleDrawable.createRipple(view, Application.sAccentColor);
        }
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}
