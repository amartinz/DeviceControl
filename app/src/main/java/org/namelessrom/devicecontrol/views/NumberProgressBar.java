package org.namelessrom.devicecontrol.views;

import android.content.Context;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.theme.AppResources;

public class NumberProgressBar extends com.daimajia.numberprogressbar.NumberProgressBar {
    public NumberProgressBar(Context context) {
        super(context);
    }

    public NumberProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override public int getDefaultColor() {
        if (isInEditMode()) {
            return super.getDefaultColor();
        }
        return AppResources.get().getAccentColor();
    }
}
