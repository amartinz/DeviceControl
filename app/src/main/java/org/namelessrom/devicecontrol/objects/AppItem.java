package org.namelessrom.devicecontrol.objects;

import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by alex on 11.05.14.
 */
public class AppItem {

    private final ApplicationInfo info;
    private final Drawable        icon;
    private final String          label;

    private boolean enabled;

    public AppItem(final Drawable icon, final String label, final ApplicationInfo info) {
        this.info = info;
        this.icon = icon;
        this.label = label;

        this.enabled = info.enabled;
    }

    public Drawable getIcon() { return icon; }

    public String getLabel() { return label; }

    public String getPackageName() { return info.packageName; }

    public int getFlags() { return info.flags; }

    public boolean isSystemApp() {
        final int mask = (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
        return ((info.flags & mask) != 0);
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(final boolean enabled) { this.enabled = enabled; }

}
