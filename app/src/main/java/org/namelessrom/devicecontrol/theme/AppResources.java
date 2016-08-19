package org.namelessrom.devicecontrol.theme;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.DeviceConfig;

import timber.log.Timber;

public class AppResources {
    private static AppResources sInstance;

    private Boolean isLowEndGfx = null;

    private int accentColor;
    private int primaryColor;

    private Drawable drawerHeaderDrawable;

    private AppResources(Context context) {
        this.accentColor = getColor(context, R.color.colorAccent);
        this.primaryColor = getColor(context, R.color.colorPrimaryDark);
    }

    @Deprecated public static AppResources get() {
        return get(App.get());
    }

    public static AppResources get(Context context) {
        if (sInstance == null) {
            sInstance = new AppResources(context);
        }
        return sInstance;
    }

    public static int getColor(@ColorRes int colorResId) {
        return getColor(App.get(), colorResId);
    }

    public static int getColor(Context context, @ColorRes int colorResId) {
        return ContextCompat.getColor(context, colorResId);
    }

    private static Drawable getDrawable(Context context, @DrawableRes int drawableResId) {
        return ContextCompat.getDrawable(context, drawableResId);
    }

    public boolean isLowEndGfx(Context context) {
        if (isLowEndGfx == null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            final boolean isLowEndGfx = ActivityManagerCompat.isLowRamDevice(am);
            final boolean setLowEndGfx = prefs.getBoolean(Constants.KEY_LOW_END_GFX, isLowEndGfx);
            Timber.d("isLowEndGfx: %s | setLowEndGfx: %s", isLowEndGfx, setLowEndGfx);

            this.isLowEndGfx = setLowEndGfx;
        }
        return this.isLowEndGfx;
    }

    public void setLowEndGfx(boolean isLowEndGfx) {
        this.isLowEndGfx = isLowEndGfx;
    }

    public AppResources setThemeMode(int themeMode) {
        DeviceConfig deviceConfig = DeviceConfig.get();
        deviceConfig.themeMode = themeMode;
        deviceConfig.save();

        return this;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public Drawable getDrawerHeader(Context context) {
        if (drawerHeaderDrawable == null) {
            final boolean isLowEndGfx = isLowEndGfx(context);
            if (isLowEndGfx) {
                drawerHeaderDrawable = new ColorDrawable(getPrimaryColor());
            } else {
                drawerHeaderDrawable = getDrawable(context, R.drawable.drawer_header_bg);
            }
        }
        return drawerHeaderDrawable;
    }

    public void cleanup() {
        drawerHeaderDrawable = null;
        isLowEndGfx = null;
        sInstance = null;
    }

}
