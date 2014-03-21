package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

/**
 * Created by alex on 21.03.14.
 */
public class CustomPreference extends Preference {

    TextView title;
    TextView summary;
    String color    = "#FFFFFF";
    String sumColor = null;
    Preference        pref;
    boolean           excludeDialog;
    boolean           checkBoxState;
    boolean           areMilliVolts;
    String            category;
    SharedPreferences mPrefs;

    public CustomPreference(Context context, boolean excludeDialog, String category) {
        super(context);
        this.excludeDialog = excludeDialog;
        this.category = category;
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

    public void setCustomSummaryKeyPlus(int plus) {
        String currValue = this.getKey();
        int newValue = Integer.parseInt(currValue) + plus;
        this.setKey(newValue + "");
        if (areMilliVolts) {
            this.setSummary(newValue + " mV");
        } else {
            this.setSummary(newValue + "");
        }
    }

    public void setCustomSummaryKeyMinus(int minus) {
        String currValue = this.getKey();
        int newValue = Integer.parseInt(currValue) - minus;
        this.setKey(newValue + "");
        if (areMilliVolts) {
            this.setSummary(newValue + " mV");
        } else {
            this.setSummary(newValue + "");
        }
    }

    public void restoreSummaryKey(String summary, String key) {
        this.setKey(key);
        if (areMilliVolts) {
            this.setSummary(summary + " mV");
        } else {
            this.setSummary(summary + "");
        }
    }

    public void areMilliVolts(boolean areMillivolts) {
        this.areMilliVolts = areMillivolts;
    }

    public boolean getCheckBoxState() {
        return checkBoxState;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setTitleColor(String color) {
        this.color = color;
    }

    public void setSummaryColor(String color) {
        this.sumColor = color;
    }

    public void excludeFromDialog(boolean exclude) {
        this.excludeDialog = exclude;
    }

    public String getCategory() {
        return this.category;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        pref = this;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        title = (TextView) view.findViewById(android.R.id.title);
        title.setTextColor(Color.parseColor(color));
        summary = (TextView) view.findViewById(android.R.id.summary);
        if (sumColor != null) {
            summary.setTextColor(Color.parseColor(sumColor));
        }

        title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
        summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }
}

