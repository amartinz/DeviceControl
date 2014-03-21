package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class CustomCheckBoxPreference extends CheckBoxPreference
        implements Preference.OnPreferenceChangeListener {

    private String color = "#FFFFFF";
    private String            value;
    private SharedPreferences mPrefs;
    private boolean mAutoHandle = false;

    public CustomCheckBoxPreference(Context context) {
        super(context);
        setLayoutResource(R.layout.preference);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference);
        init(attrs);
    }

    public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CustomPreferences, 0, 0
        );
        if (a != null) {
            mAutoHandle = a.getBoolean(R.styleable.CustomPreferences_auto_handle, false);
            a.recycle();
        }
    }

    public void setTitleColor(String color) {
        this.color = color;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        final TextView mTitle = (TextView) view.findViewById(android.R.id.title);
        mTitle.setTextColor(Color.parseColor(color));
        mTitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView mSummary = (TextView) view.findViewById(android.R.id.summary);
        mSummary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        final boolean checked = mPrefs.getBoolean(getKey(), false);
        final CheckBox mCheckBox = (CheckBox) view.findViewById(android.R.id.checkbox);
        mCheckBox.setChecked(checked);
        if (mAutoHandle) {
            setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        final boolean value = (Boolean) o;
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(getKey(), value);
        editor.commit();
        return true;
    }
}
