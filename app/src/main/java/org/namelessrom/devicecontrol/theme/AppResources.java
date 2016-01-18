package org.namelessrom.devicecontrol.theme;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;

public class AppResources {
    private static AppResources sInstance;

    private int isLightTheme = -1;

    private int accentColor;
    private int primaryColor;

    private int cardBackgroundColor;

    private AppResources() {
        final boolean isLight = isLightTheme();

        this.cardBackgroundColor = isLight
                ? getColor(R.color.cardview_light_background)
                : getColor(R.color.cardview_dark_background);

        this.accentColor = isLight
                ? getColor(R.color.accent_light)
                : getColor(R.color.accent);

        this.primaryColor = isLight
                ? getColor(R.color.light_primary_dark)
                : getColor(R.color.dark_primary_dark);
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

    public boolean isLightTheme() {
        if (isLightTheme == -1) {
            isLightTheme = DeviceConfig.get().lightTheme ? 1 : 0;
        }
        return (isLightTheme == 1);
    }

    public AppResources setLightTheme(boolean isLight) {
        isLightTheme = isLight ? 1 : 0;

        DeviceConfig deviceConfig = DeviceConfig.get();
        deviceConfig.lightTheme = isLight;
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

    @DrawableRes public int getDrawerHeaderResId() {
        return isLightTheme() ? R.drawable.drawer_header_bg_light : R.drawable.drawer_header_bg_dark;
    }

    public static ContextThemeWrapper getContextThemeWrapper(Context context) {
        final int themeId = AppResources.get().isLightTheme()
                ? R.style.AppTheme_Light
                : R.style.AppTheme_Dark;
        return new ContextThemeWrapper(context, themeId);
    }

    public static LayoutInflater getThemeLayoutInflater(Context context, LayoutInflater inflater) {
        return inflater.cloneInContext(getContextThemeWrapper(context));
    }

}
