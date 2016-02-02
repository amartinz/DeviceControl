package org.namelessrom.devicecontrol.theme;

import android.content.res.ColorStateList;

import org.namelessrom.devicecontrol.R;

public class NavigationDrawerResources {
    private static NavigationDrawerResources sInstance;

    private static final int[][] DRAWER_ICON_STATES = new int[][]{
            new int[]{ android.R.attr.state_checked }, // checked
            new int[]{ -android.R.attr.state_enabled }, // disabled
            new int[]{}
    };

    private static final int[] DRAWER_ICON_COLORS_DARK = new int[]{
            AppResources.getColor(R.color.drawer_icon_dark_checked),
            AppResources.getColor(R.color.drawer_icon_dark_disabled),
            AppResources.getColor(R.color.drawer_icon_dark)
    };

    private static final int[] DRAWER_ICON_COLORS_LIGHT = new int[]{
            AppResources.getColor(R.color.drawer_icon_light_checked),
            AppResources.getColor(R.color.drawer_icon_light_disabled),
            AppResources.getColor(R.color.drawer_icon_light)
    };

    private static final int[][] DRAWER_TEXT_STATES = new int[][]{
            new int[]{ android.R.attr.state_checked }, // checked
            new int[]{ -android.R.attr.state_enabled }, // disabled
            new int[]{}
    };

    private static final int[] DRAWER_TEXT_COLORS_DARK = new int[]{
            AppResources.getColor(R.color.drawer_text_dark_checked),
            AppResources.getColor(R.color.drawer_text_dark_disabled), // disabled
            AppResources.getColor(R.color.drawer_text_dark)
    };

    private static final int[] DRAWER_TEXT_COLORS_LIGHT = new int[]{
            AppResources.getColor(R.color.drawer_text_light_checked),
            AppResources.getColor(R.color.drawer_text_light_disabled), // disabled
            AppResources.getColor(R.color.drawer_text_light)
    };

    private ColorStateList itemIconColor;
    private ColorStateList itemTextColor;

    private NavigationDrawerResources() {
        final boolean isLight = AppResources.get().isLightTheme();

        this.itemIconColor = new ColorStateList(DRAWER_ICON_STATES, isLight ? DRAWER_ICON_COLORS_LIGHT : DRAWER_ICON_COLORS_DARK);

        this.itemTextColor = new ColorStateList(DRAWER_TEXT_STATES, isLight ? DRAWER_TEXT_COLORS_LIGHT : DRAWER_TEXT_COLORS_DARK);
    }

    public static NavigationDrawerResources get() {
        if (sInstance == null) {
            sInstance = new NavigationDrawerResources();
        }

        return sInstance;
    }

    public ColorStateList getItemIconColor() {
        return itemIconColor;
    }

    public ColorStateList getItemTextColor() {
        return itemTextColor;
    }

    public void cleanup() {
        sInstance = null;
    }

}
