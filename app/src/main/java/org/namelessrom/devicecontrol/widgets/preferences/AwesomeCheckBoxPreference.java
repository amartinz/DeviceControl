package org.namelessrom.devicecontrol.widgets.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Automatically handles reading to files to automatically set the value,
 * writing to files on preference change, even with multiple files,
 * handling bootup restoration.
 */
public class AwesomeCheckBoxPreference extends CheckBoxPreference {

    protected static final String color = "#FFFFFF";

    private String  category;
    private boolean startUp;
    private boolean multiFile;
    private String  valueChecked;
    private String  valueNotChecked;

    private String   mPath;
    private String[] mPaths;

    public AwesomeCheckBoxPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AwesomeCheckBoxPreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AwesomePreference);

        int filePath = -1, filePathList = -1;
        try {
            assert (a != null);
            filePath = a.getResourceId(R.styleable.AwesomePreference_filePath, -1);
            filePathList = a.getResourceId(R.styleable.AwesomePreference_filePathList, -1);
            category = a.getString(R.styleable.AwesomePreference_category);
            startUp = a.getBoolean(R.styleable.AwesomePreference_startup, true);
            multiFile = a.getBoolean(R.styleable.AwesomePreference_multifile, false);
            valueChecked = a.getString(R.styleable.AwesomePreference_valueChecked);
            valueNotChecked = a.getString(R.styleable.AwesomePreference_valueNotChecked);
        } finally {
            if (a != null) a.recycle();
        }

        final Resources res = context.getResources();
        if (filePath != -1) {
            mPath = Utils.checkPath(res.getString(filePath));
            mPaths = null;
        } else if (filePathList != -1) {
            mPaths = res.getStringArray(filePathList);
            mPath = Utils.checkPaths(mPaths);
            if (mPath.isEmpty() || !multiFile) {
                mPaths = null;
            }
        } else {
            mPath = "";
            mPaths = null;
        }

        if (category == null || category.isEmpty()) {
            logDebug("AwesomeCheckBoxPreference", "Category is not set! Defaulting to \"default\"");
            category = "default";
        }
        if (valueChecked == null || valueChecked.isEmpty()) valueChecked = "1";
        if (valueNotChecked == null || valueNotChecked.isEmpty()) valueNotChecked = "0";

        setLayoutResource(R.layout.preference);
    }

    @Override protected void onBindView(final View view) {
        super.onBindView(view);

        final TextView mTitle = findById(view, android.R.id.title);
        mTitle.setTextColor(Color.parseColor(color));
        mTitle.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

        final TextView mSummary = findById(view, android.R.id.summary);
        mSummary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
    }

    public void initValue() { initValue(false); }

    public void initValue(final boolean contains) {
        if (isSupported()) setChecked(Utils.isEnabled(Utils.readOneLine(mPath), contains));
    }

    public String getPath() { return mPath; }

    public boolean isSupported() {
        return ((mPath != null && !mPath.isEmpty()) || (mPaths != null && mPaths.length != 0));
    }

    public void writeValue(final boolean isChecked) {
        if (isSupported()) {
            if (mPaths != null && multiFile) {
                final int length = mPaths.length;
                for (int i = 0; i < length; i++) {
                    Utils.writeValue(mPaths[i], (isChecked ? valueChecked : valueNotChecked));
                    if (startUp) {
                        PreferenceHelper.setBootup(new DataItem(
                                category, getKey() + String.valueOf(i), mPaths[i],
                                (isChecked ? valueChecked : valueNotChecked)));
                    }
                }
            } else {
                Utils.writeValue(mPath, (isChecked ? valueChecked : valueNotChecked));
                if (startUp) {
                    PreferenceHelper.setBootup(new DataItem(category, getKey(), mPath,
                            (isChecked ? valueChecked : valueNotChecked)));
                }
            }
        }
    }

    @Override public boolean isPersistent() { return false; }

    @Override protected boolean shouldPersist() { return false; }
}
