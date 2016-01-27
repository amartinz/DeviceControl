package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DrawableHelper;
import org.namelessrom.devicecontrol.utils.Utils;

public class CustomPreference extends Preference {
    private int sumColor = -1;

    private boolean areMilliVolts;
    private String category;

    private TextView summary;

    public CustomPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
    }

    public CustomPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference);
    }

    public void setCustomSummaryKeyPlus(final int plus) {
        final int newValue = Utils.parseInt(getKey()) + plus;
        setKey(newValue + "");
        if (areMilliVolts) {
            setSummary(newValue + " mV");
        } else {
            setSummary(newValue + "");
        }
    }

    public void setCustomSummaryKeyMinus(final int minus) {
        final int newValue = Utils.parseInt(getKey()) - minus;
        setKey(newValue + "");
        if (areMilliVolts) {
            setSummary(newValue + " mV");
        } else {
            setSummary(newValue + "");
        }
    }

    public void restoreSummaryKey(final String summary, final String key) {
        setKey(key);
        if (areMilliVolts) {
            setSummary(summary + " mV");
        } else {
            setSummary(summary + "");
        }
    }

    public void areMilliVolts(final boolean areMillivolts) { this.areMilliVolts = areMillivolts; }

    public void setCategory(final String category) { this.category = category; }

    public void setSummaryColor(final int color) {
        this.sumColor = color;
        if (summary != null) {
            summary.setTextColor(this.sumColor);
        }
    }

    public void setSummaryColor(final String color) {
        setSummaryColor(Color.parseColor(color));
    }

    public String getCategory() { return this.category; }

    @Override protected void onBindView(@NonNull final View view) {
        super.onBindView(view);

        summary = (TextView) view.findViewById(android.R.id.summary);
        if (sumColor != -1) {
            summary.setTextColor(sumColor);
        }

        final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
        if (icon != null) {
            final Drawable d = icon.getDrawable();
            if (d != null) {
                DrawableHelper.applyAccentColorFilter(d);
                icon.setImageDrawable(d);
            }
        }
    }

}

