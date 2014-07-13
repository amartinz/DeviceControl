package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.negusoft.holoaccent.dialog.DividerPainter;

import org.namelessrom.devicecontrol.R;

import butterknife.ButterKnife;

public class CustomListPreference extends ListPreference {

    private String color = "#FFFFFF";

    public CustomListPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    public void setTitleColor(final String color) { this.color = color; }

    @Override protected void showDialog(Bundle state) {
        super.showDialog(state);
        new DividerPainter(getContext()).paint(getDialog().getWindow());
    }

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
