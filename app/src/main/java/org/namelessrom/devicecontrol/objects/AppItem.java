package org.namelessrom.devicecontrol.objects;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

/**
 * Created by alex on 11.05.14.
 */
public class AppItem {

    private final PackageInfo     pkgInfo;
    private final ApplicationInfo appInfo;

    private final String   label;
    private final Drawable icon;

    private boolean enabled = false;

    public AppItem(final PackageInfo info, final String label, final Drawable icon) {
        this.pkgInfo = info;
        this.appInfo = info.applicationInfo;

        this.label = label;
        this.icon = icon;

        enabled = (appInfo != null && appInfo.enabled);
    }

    public String getLabel() { return label; }

    public Drawable getIcon() { return icon; }

    public PackageInfo getPackageInfo() { return pkgInfo; }

    public ApplicationInfo getApplicationInfo() { return appInfo; }

    public String getPackageName() { return pkgInfo.packageName; }

    public boolean isSystemApp() {
        final int mask = (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
        return ((appInfo.flags & mask) != 0);
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(final boolean enabled) { this.enabled = enabled; }

}
