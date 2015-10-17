package org.namelessrom.devicecontrol.utils;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.R;

/**
 * Created by amartinz on 17.10.15.
 */
public class ThemeHelper {
    private static ThemeHelper sInstance;

    private final Context mContext;

    private int mAccentColor;
    private int mCardBackgroundColor;

    private ThemeHelper(@NonNull final Context context) {
        mContext = context;

        mAccentColor = ContextCompat.getColor(mContext, R.color.accent);

        mCardBackgroundColor = isDarkTheme()
                ? ContextCompat.getColor(mContext, R.color.cardview_dark_background)
                : ContextCompat.getColor(mContext, R.color.cardview_light_background);
    }

    public static ThemeHelper with(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new ThemeHelper(context);
        }
        return sInstance;
    }

    public boolean isDarkTheme() {
        // TODO: multi theme support?
        return true;
    }

    public int getAccentColor() {
        return mAccentColor;
    }

    public ThemeHelper setAccentColor(@ColorRes int accentColor) {
        mAccentColor = ContextCompat.getColor(mContext, accentColor);
        return this;
    }

    public int getCardBackgroundColor() {
        return mCardBackgroundColor;
    }
}
