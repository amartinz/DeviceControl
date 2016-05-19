/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.Arrays;

import timber.log.Timber;

/**
 * Automatically handles reading to files to automatically set the value,
 * writing to files on preference change, even with multiple files,
 * handling bootup restoration.
 */
public class AwesomeTogglePreference extends CustomTogglePreference {

    private String mCategory;
    private String mValueChecked = "1";
    private String mValueNotChecked = "0";

    private boolean mStartUp;
    private boolean mMultiFile;

    private String mPath;
    private String[] mPaths;

    public AwesomeTogglePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AwesomeTogglePreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public AwesomeTogglePreference(final Context context, final String path, final String[] paths,
            final String category, final boolean multiFile, final boolean startUp) {
        super(context);
        mPath = path;
        mPaths = paths;
        mCategory = category;
        mMultiFile = multiFile;
        mStartUp = startUp;
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AwesomePreference);

        int filePath = -1, filePathList = -1, fileValue = -1;
        if (a != null) {
            filePath = a.getResourceId(R.styleable.AwesomePreference_filePath, -1);
            filePathList = a.getResourceId(R.styleable.AwesomePreference_filePathList, -1);
            fileValue = a.getResourceId(R.styleable.AwesomePreference_fileValue, -1);
            mCategory = a.getString(R.styleable.AwesomePreference_category);
            mStartUp = a.getBoolean(R.styleable.AwesomePreference_startup, true);
            mMultiFile = a.getBoolean(R.styleable.AwesomePreference_multifile, false);
            mValueChecked = a.getString(R.styleable.AwesomePreference_valueChecked);
            mValueNotChecked = a.getString(R.styleable.AwesomePreference_valueNotChecked);
            a.recycle();
        }

        final Resources res = context.getResources();
        if (filePath != -1) {
            mPath = Utils.checkPath(res.getString(filePath));
            mPaths = null;
        } else if (filePathList != -1) {
            mPaths = res.getStringArray(filePathList);
            mPath = Utils.checkPaths(mPaths);
            if (mPath.isEmpty() || !mMultiFile) {
                mPaths = null;
            }
        } else {
            mPath = "";
            mPaths = null;
        }

        if (!TextUtils.isEmpty(mPath) && filePathList != -1 && fileValue != -1) {
            final int index = Arrays.asList(res.getStringArray(filePathList)).indexOf(mPath);
            final String[] values = res.getStringArray(fileValue)[index].split(";");
            mValueChecked = values[0];
            mValueNotChecked = values[1];
            Timber.d("mValueChecked -> %s\nmValueNotChecked -> %s", mValueChecked, mValueNotChecked);
        }

        if (mCategory == null || mCategory.isEmpty()) {
            Timber.w("Category is not set! Defaulting to \"default\"");
            mCategory = "default";
        }
        if (TextUtils.isEmpty(mValueChecked)) { mValueChecked = "1"; }
        if (TextUtils.isEmpty(mValueNotChecked)) { mValueNotChecked = "0"; }
    }

    public void initValue() { initValue(false); }

    public void initValue(final boolean contains) {
        if (isSupported()) { setChecked(Utils.isEnabled(Utils.readOneLine(mPath), contains)); }
    }

    public void setupTitle() {
        if (!isSupported()) {
            Timber.v("setupTitle -> not supported");
            return;
        }
        final Integer title = PreferenceUtils.getTitle(Utils.getFileName(mPath));
        if (title != null) {
            setTitle(title);
        }
    }

    public void setupSummary() {
        if (!isSupported()) {
            Timber.v("setupSummary -> not supported");
            return;
        }
        final Integer summary = PreferenceUtils.getSummary(Utils.getFileName(mPath));
        if (summary != null) {
            setSummary(summary);
        }
    }

    public String getPath() { return mPath; }

    public boolean isSupported() {
        return ((!TextUtils.isEmpty(mPath)) || (mPaths != null && mPaths.length != 0));
    }

    public void writeValue(final boolean isChecked) {
        if (isSupported()) {
            if (mPaths != null && mMultiFile) {
                final int length = mPaths.length;
                for (int i = 0; i < length; i++) {
                    Utils.writeValue(mPaths[i], (isChecked ? mValueChecked : mValueNotChecked));
                    if (mStartUp) {
                        BootupConfig.setBootup(new BootupItem(mCategory,
                                getKey() + String.valueOf(i), mPaths[i],
                                (isChecked ? mValueChecked : mValueNotChecked), true));
                    }
                }
            } else {
                Utils.writeValue(mPath, (isChecked ? mValueChecked : mValueNotChecked));
                if (mStartUp) {
                    BootupConfig.setBootup(
                            new BootupItem(mCategory, getKey(), mPath,
                                    (isChecked ? mValueChecked : mValueNotChecked), true));
                }
            }
        }
    }

}
