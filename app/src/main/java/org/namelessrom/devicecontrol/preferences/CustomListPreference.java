package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.negusoft.holoaccent.preference.ListPreference;

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
    public boolean isPersistent() { return false; }

    @Override
    protected boolean shouldPersist() { return false; }

}
