package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import butterknife.ButterKnife;

public class CustomListPreference extends ListPreference {

    public CustomListPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        final TextView title = ButterKnife.findById(view, android.R.id.title);
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView summary = ButterKnife.findById(view, android.R.id.summary);
        summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    @Override
    public boolean isPersistent() { return false; }

    @Override
    protected boolean shouldPersist() { return false; }

}
