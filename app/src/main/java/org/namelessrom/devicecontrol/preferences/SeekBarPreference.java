package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;

import timber.log.Timber;

public class SeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private final String TAG = getClass().getName();

    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String DC = "http://schemas.android.com/apk/res-auto";

    protected static final int DEFAULT_VALUE = 50;

    protected SeekBar mSeekBar;

    private int mMaxValue = 100;
    private int mMinValue = 0;
    private int mInterval = 1;
    private int mCurrentValue;
    private String mUnitsLeft = "";
    private String mUnitsRight = "";
    private TextView mTitle;

    private TextView mStatusText;

    public SeekBarPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        initPreference(context, attrs);
    }

    public SeekBarPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(final Context context, final AttributeSet attrs) {
        setValuesFromXml(attrs);
        mSeekBar = new SeekBar(context, attrs);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    private void setValuesFromXml(final AttributeSet attrs) {
        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
        mMinValue = attrs.getAttributeIntValue(DC, "min", 0);
        mUnitsLeft = getAttributeStringValue(attrs, DC, "unitsLeft", "");
        final String units = getAttributeStringValue(attrs, DC, "units", "");
        mUnitsRight = getAttributeStringValue(attrs, DC, "unitsRight", units);
        try {
            final String newInterval = attrs.getAttributeValue(DC, "interval");
            if (newInterval != null) { mInterval = Utils.parseInt(newInterval); }
        } catch (Exception e) {
            Timber.e(e, "Invalid interval value");
        }
    }

    private String getAttributeStringValue(final AttributeSet attrs, final String namespace,
            final String name, final String defaultValue) {
        final String value = attrs.getAttributeValue(namespace, name);
        return (value != null ? value : defaultValue);
    }

    @Override public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        this.setShouldDisableView(true);
        if (mTitle != null) { mTitle.setEnabled(!disableDependent); }
        if (mSeekBar != null) { mSeekBar.setEnabled(!disableDependent); }
    }

    @Override protected View onCreateView(final ViewGroup parent) {
        super.onCreateView(parent);
        try {
            final LayoutInflater mInflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final RelativeLayout layout =
                    (RelativeLayout) mInflater.inflate(R.layout.preference_seekbar, parent, false);
            mTitle = (TextView) layout.findViewById(android.R.id.title);
            return layout;
        } catch (Exception e) {
            Timber.e(e, "Error creating seek bar preference");
            return null;
        }
    }

    @Override public void onBindView(@NonNull final View view) {
        super.onBindView(view);
        try {
            // move our seekbar to the new view we've been given
            final ViewParent oldContainer = mSeekBar.getParent();
            final ViewGroup newContainer =
                    (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);

            if (oldContainer != newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(mSeekBar);
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews();
                newContainer.addView(mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        } catch (Exception ex) {
            Timber.e(ex, "Error binding view!");
        }
        updateView(view);
    }

    /**
     * Update a SeekBarPreference view with our current state
     *
     * @param view View
     */
    protected void updateView(final View view) {
        try {
            final RelativeLayout layout = (RelativeLayout) view;
            mStatusText = (TextView) layout.findViewById(R.id.seekBarPrefValue);
            mStatusText.setText(String.valueOf(mCurrentValue));
            mStatusText.setMinimumWidth(30);
            mSeekBar.setProgress(mCurrentValue - mMinValue);

            final TextView unitsRight = (TextView) layout.findViewById(R.id.seekBarPrefUnitsRight);
            unitsRight.setText(mUnitsRight);
            final TextView unitsLeft = (TextView) layout.findViewById(R.id.seekBarPrefUnitsLeft);
            unitsLeft.setText(mUnitsLeft);
        } catch (Exception e) {
            Timber.e(e, "Error updating seek bar preference");
        }
    }

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + mMinValue;
        if (newValue > mMaxValue) {
            newValue = mMaxValue;
        } else if (newValue < mMinValue) {
            newValue = mMinValue;
        } else if (mInterval != 1 && newValue % mInterval != 0) {
            newValue = Math.round(((float) newValue) / mInterval) * mInterval;
        }

        // change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.setProgress(mCurrentValue - mMinValue);
            return;
        }

        if (mStatusText != null) {
            mStatusText.setText(String.valueOf(newValue));
        }
    }

    @Override public void onStartTrackingTouch(final SeekBar seekBar) { }

    @Override public void onStopTrackingTouch(final SeekBar seekBar) {
        mCurrentValue = seekBar.getProgress();
        notifyChanged();
    }

    @Override protected Object onGetDefaultValue(final TypedArray ta, final int index) {
        return ta.getInt(index, DEFAULT_VALUE);
    }

    @Override protected void onSetInitialValue(final boolean restoreValue, final Object defValue) {
        if (restoreValue) {
            mCurrentValue = getPersistedInt(mCurrentValue);
        } else {
            int temp = 0;
            try {
                temp = (Integer) defValue;
            } catch (Exception ex) {
                Timber.e(ex, "Invalid default value: %s", String.valueOf(defValue));
            }
            mCurrentValue = temp;
        }
    }

    public void setValue(final int value) { mCurrentValue = value; }

    @Override public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }

}
