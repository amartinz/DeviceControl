package org.namelessrom.devicecontrol.theme;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;

public class AppResources {
    private static AppResources sInstance;

    private int isDarkTheme = -1;

    private int accentColor;
    private int primaryColor;

    private int cardBackgroundColor;

    private AppResources() {
        final boolean isDark = isDarkTheme();

        this.cardBackgroundColor = isDark
                ? getColor(R.color.cardview_dark_background)
                : getColor(R.color.cardview_light_background);

        this.accentColor = isDark
                ? getColor(R.color.accent)
                : getColor(R.color.accent_light);

        this.primaryColor = isDark
                ? getColor(R.color.dark_primary_dark)
                : getColor(R.color.light_primary_dark);
    }

    public static AppResources get() {
        if (sInstance == null) {
            sInstance = new AppResources();
        }

        return sInstance;
    }

    private int getColor(int colorResId) {
        return Application.get().getColorApplication(colorResId);
    }

    public boolean isDarkTheme() {
        if (isDarkTheme == -1) {
            isDarkTheme = DeviceConfig.get().darkTheme ? 1 : 0;
        }
        return (isDarkTheme == 1);
    }

    public AppResources setDarkTheme(boolean isDark) {
        isDarkTheme = isDark ? 1 : 0;

        DeviceConfig deviceConfig = DeviceConfig.get();
        deviceConfig.darkTheme = isDark;
        deviceConfig.save();

        sInstance = new AppResources();

        return this;
    }

    public AppResources setAccentColor(int accentColor) {
        this.accentColor = accentColor;
        return this;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public AppResources setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
        return this;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public AppResources setCardBackgroundColor(int cardBackgroundColor) {
        this.cardBackgroundColor = cardBackgroundColor;
        return this;
    }

    public int getCardBackgroundColor() {
        return cardBackgroundColor;
    }

    public static ContextThemeWrapper getContextThemeWrapper(Context context) {
        final int themeId = AppResources.get().isDarkTheme()
                ? R.style.AppTheme_Dark
                : R.style.AppTheme_Light;
        return new ContextThemeWrapper(context, themeId);
    }

    public static LayoutInflater getThemeLayoutInflater(Context context, LayoutInflater inflater) {
        return inflater.cloneInContext(getContextThemeWrapper(context));
    }

}
