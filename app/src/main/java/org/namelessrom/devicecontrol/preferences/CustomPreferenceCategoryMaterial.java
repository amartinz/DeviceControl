package org.namelessrom.devicecontrol.preferences;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.theme.AppResources;

import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;

// TODO: rename once all is migrated
public class CustomPreferenceCategoryMaterial extends MaterialPreferenceCategory {

    public CustomPreferenceCategoryMaterial(Context context) {
        super(context);
    }

    public CustomPreferenceCategoryMaterial(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomPreferenceCategoryMaterial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomPreferenceCategoryMaterial(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override public boolean init(Context context, AttributeSet attrs) {
        if (!super.init(context, attrs)) {
            return false;
        }
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cardview_background));
        return true;
    }

    @Override public LayoutInflater getCustomLayoutInflater() {
        return LayoutInflater.from(getContext());
    }
}

