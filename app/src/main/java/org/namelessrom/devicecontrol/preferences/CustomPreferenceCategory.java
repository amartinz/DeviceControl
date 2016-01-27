package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;

public class CustomPreferenceCategory extends PreferenceCategory {

    public CustomPreferenceCategory(Context context) {
        this(context, null);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_category);
    }

    public CustomPreferenceCategory(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_category);
    }

    @Override protected void onBindView(@NonNull final View view) {
        super.onBindView(view);
        final TextView title = (TextView) view.findViewById(android.R.id.title);
        title.setTextColor(AppResources.get().getAccentColor());

        final View separator = view.findViewById(R.id.category_separator);
        separator.setBackgroundColor(AppResources.get().getAccentColor());
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}

