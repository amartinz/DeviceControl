package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;

import dreamers.graphics.RippleDrawable;

public class CustomCheckBoxPreference extends CheckBoxPreference {

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

    @Override protected void onBindView(@NonNull View view) {
        super.onBindView(view);
        if (isSelectable()) {
            RippleDrawable.createRipple(view, Application.sAccentColor);
        }
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }
}
