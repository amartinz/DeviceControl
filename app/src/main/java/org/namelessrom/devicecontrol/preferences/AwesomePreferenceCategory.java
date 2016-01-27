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
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Automatically handles checking, if the specified path(s) exist.
 */
public class AwesomePreferenceCategory extends CustomPreferenceCategory {
    private String mPath;
    private String[] mPaths;

    public AwesomePreferenceCategory(final Context context) { this(context, null); }

    public AwesomePreferenceCategory(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AwesomePreferenceCategory(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AwesomePreference);

        int filePath = -1, filePathList = -1;
        if (a != null) {
            filePath = a.getResourceId(R.styleable.AwesomePreference_filePath, -1);
            filePathList = a.getResourceId(R.styleable.AwesomePreference_filePathList, -1);
            a.recycle();
        }

        final Resources res = context.getResources();
        if (filePath != -1) {
            mPath = Utils.checkPath(res.getString(filePath));
            mPaths = null;
        } else if (filePathList != -1) {
            mPaths = res.getStringArray(filePathList);
            mPath = Utils.checkPaths(mPaths);
            if (mPath.isEmpty()) {
                mPaths = null;
            }
        } else {
            mPath = "";
            mPaths = null;
        }
    }

    public String getPath() { return mPath; }

    public boolean isSupported() {
        return ((mPath != null && !mPath.isEmpty()) || (mPaths != null && mPaths.length != 0));
    }

}
