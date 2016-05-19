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
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.Arrays;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;
import timber.log.Timber;

/**
 * Automatically handles reading to files to automatically set the value,
 * writing to files on preference change, even with multiple files,
 * handling bootup restoration.
 */
public class AutoSwitchPreference extends MaterialSwitchPreference {
    private String mValueChecked = "1";
    private String mValueNotChecked = "0";

    private boolean mStartup = true;
    private boolean mMultiFile = false;

    private String mPath;
    private String[] mPaths;

    private String mCategory = BootupConfig.CATEGORY_EXTRAS;

    public AutoSwitchPreference(final Context context) {
        super(context);
    }

    public AutoSwitchPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoSwitchPreference(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override public boolean init(final Context context, final AttributeSet attrs) {
        if (!super.init(context, attrs)) {
            return false;
        }

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AwesomePreference);

        int filePath = -1, filePathList = -1, fileValue = -1;
        if (a != null) {
            filePath = a.getResourceId(R.styleable.AwesomePreference_filePath, -1);
            filePathList = a.getResourceId(R.styleable.AwesomePreference_filePathList, -1);
            fileValue = a.getResourceId(R.styleable.AwesomePreference_fileValue, -1);
            mStartup = a.getBoolean(R.styleable.AwesomePreference_startup, mStartup);
            mMultiFile = a.getBoolean(R.styleable.AwesomePreference_multifile, mMultiFile);
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

        if (TextUtils.isEmpty(mValueChecked)) {
            mValueChecked = "1";
        }
        if (TextUtils.isEmpty(mValueNotChecked)) {
            mValueNotChecked = "0";
        }

        handleSelf(true);

        return true;
    }

    public void initValue() {
        initValue(false);
    }

    public void initValue(final boolean contains) {
        if (isSupported()) {
            setChecked(Utils.isEnabled(Utils.readOneLine(mPath), contains));
        }
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getPath() {
        return mPath;
    }

    public boolean isSupported() {
        return ((!TextUtils.isEmpty(mPath)) || (mPaths != null && mPaths.length != 0));
    }

    public void setPath(String path) {
        path = Utils.checkPath(path);
        if (!TextUtils.isEmpty(path)) {
            mPath = path;
            mPaths = null;
        }
    }

    public void setPaths(String[] paths) {
        String path = Utils.checkPaths(paths);
        if (!TextUtils.isEmpty(path)) {
            mPath = path;
            if (mPath.isEmpty() || !mMultiFile) {
                mPaths = null;
            } else {
                mPaths = paths;
            }
        }
    }

    public void setMultiFile(boolean isMultiFile) {
        mMultiFile = isMultiFile;
    }

    public void setStartup(boolean isStartup) {
        mStartup = isStartup;
    }

    public void setValueChecked(String valueChecked) {
        mValueChecked = valueChecked;
    }

    public void setValueNotChecked(String valueNotChecked) {
        mValueNotChecked = valueNotChecked;
    }

    public void writeValue(final boolean isChecked) {
        if (!isSupported()) {
            return;
        }

        if (mPaths != null && mMultiFile) {
            final int length = mPaths.length;
            for (int i = 0; i < length; i++) {
                Utils.writeValue(mPaths[i], (isChecked ? mValueChecked : mValueNotChecked));
                if (mStartup) {
                    BootupConfig.setBootup(new BootupItem(mCategory,
                            getKey() + String.valueOf(i), mPaths[i],
                            (isChecked ? mValueChecked : mValueNotChecked), true));
                }
            }
        } else {
            Utils.writeValue(mPath, (isChecked ? mValueChecked : mValueNotChecked));
            if (mStartup) {
                BootupConfig.setBootup(
                        new BootupItem(mCategory, getKey(), mPath,
                                (isChecked ? mValueChecked : mValueNotChecked), true));
            }
        }

        postDelayed(new Runnable() {
            @Override public void run() {
                initValue();
            }
        }, 200);
    }

    public void handleSelf(boolean handleSelf) {
        MaterialPreferenceChangeListener listener = null;
        if (handleSelf) {
            listener = new MaterialPreferenceChangeListener() {
                @Override public boolean onPreferenceChanged(MaterialPreference pref, Object o) {
                    writeValue((Boolean) o);
                    return true;
                }
            };
        }
        setOnPreferenceChangeListener(listener);
    }

}
