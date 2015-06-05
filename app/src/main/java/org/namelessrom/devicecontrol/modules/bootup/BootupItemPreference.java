package org.namelessrom.devicecontrol.modules.bootup;

import android.content.Context;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.objects.BootupItem;

import alexander.martinz.libs.materialpreferences.MaterialSwitchPreference;

public class BootupItemPreference extends MaterialSwitchPreference {
    private BootupItem bootupItem;

    public BootupItemPreference(Context context) {
        super(context);
    }

    public BootupItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BootupItemPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BootupItemPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BootupItemPreference setBootupItem(Context context, BootupItem item) {
        this.bootupItem = item;

        setKey(item.name);
        setTitle(context.getString(item.titleResId));
        setSummary(String.format("%s\n%s", item.filename, item.value));
        setChecked(item.enabled);

        return this;
    }

    public BootupItem getBootupItem() {
        return bootupItem;
    }
}
