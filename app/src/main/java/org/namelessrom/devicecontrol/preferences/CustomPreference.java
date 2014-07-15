package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import butterknife.ButterKnife;

public class CustomPreference extends Preference {

    private String color    = "#FFFFFF";
    private int    sumColor = -1;

    private boolean areMilliVolts;
    private String  category;

    private TextView title;
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
        final int newValue = Integer.parseInt(getKey()) + plus;
        setKey(newValue + "");
        if (areMilliVolts) {
            setSummary(newValue + " mV");
        } else {
            setSummary(newValue + "");
        }
    }

    public void setCustomSummaryKeyMinus(final int minus) {
        final int newValue = Integer.parseInt(getKey()) - minus;
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

    public void setTitleColor(final String color) {
        this.color = color;
        if (title != null) {
            title.setTextColor(Color.parseColor(this.color));
        }
    }

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

    @Override
    protected void onBindView(final View view) {
        super.onBindView(view);

        title = ButterKnife.findById(view, android.R.id.title);
        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        title.setTextColor(Color.parseColor(color));

        summary = ButterKnife.findById(view, android.R.id.summary);
        summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        if (sumColor != -1) {
            summary.setTextColor(sumColor);
        }
    }

}

