/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Modifications Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.namelessrom.devicecontrol.ui.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.negusoft.holoaccent.preference.DialogPreference;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

/**
 * Special preference type that allows configuration of
 * vibrator intensity settings on Samsung devices
 */
public class VibratorTuningPreference extends DialogPreference
        implements SeekBar.OnSeekBarChangeListener, DeviceConstants {

    private static final String[] FILES_VIBRATOR = {
            "/sys/class/timed_output/vibrator/pwm_value",
            "/sys/devices/platform/tspdrv/nforce_timed",
            "/sys/vibrator/pwm_val",
            "/sys/vibrator/pwmvalue"
    };

    private static final int VIBRATOR_INTENSITY_MAX               = 100;
    private static final int VIBRATOR_INTENSITY_MIN               = 0;
    private static final int VIBRATOR_INTENSITY_DEFAULT_VALUE     = 50;
    private static final int VIBRATOR_INTENSITY_WARNING_THRESHOLD = 76;

    //----------------------------------------------------------------------------------------------
    private static final String FILE_VIBRATOR = Utils.checkPaths(FILES_VIBRATOR);
    private final Vibrator vib;
    private       SeekBar  mSeekBar;
    private       TextView mValue;
    private       String   mOriginalValue;
    private       Drawable mProgressDrawable;

    private Drawable            mProgressThumb = null;
    private LightingColorFilter mRedFilter     = null;

    public VibratorTuningPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        setDialogLayoutResource(R.layout.preference_dialog_vibrator_tuning);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.defaults_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) { }
        });
    }

    @Override
    protected void onBindDialogView(@NonNull final View view) {
        super.onBindDialogView(view);

        mSeekBar = (SeekBar) view.findViewById(R.id.vibrator_seekbar);
        mValue = (TextView) view.findViewById(R.id.vibrator_value);
        final TextView mWarning = (TextView) view.findViewById(R.id.textWarn);

        final String strWarnMsg = getContext().getResources().getString(
                R.string.vibrator_warning
                , strengthToPercent(VIBRATOR_INTENSITY_WARNING_THRESHOLD) - 1);
        mWarning.setText(strWarnMsg);

        final Drawable progressDrawable = mSeekBar.getProgressDrawable();
        if (progressDrawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) progressDrawable;
            mProgressDrawable = ld.findDrawableByLayerId(android.R.id.progress);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mProgressThumb = mSeekBar.getThumb();
        }
        mRedFilter = new LightingColorFilter(Color.BLACK,
                getContext().getResources().getColor(android.R.color.holo_red_light));

        // Read the current value from sysfs in case user wants to dismiss his changes
        mOriginalValue = Utils.readOneLine(FILE_VIBRATOR);

        // Restore percent value from SharedPreferences object
        final String value = PreferenceHelper.getBootupValue("vibrator_tuning");
        final int percent = strengthToPercent(value != null
                ? Integer.parseInt(value)
                : VIBRATOR_INTENSITY_DEFAULT_VALUE);

        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(percent);

        view.findViewById(R.id.vibrator_test).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (vib != null) {
                            vib.cancel();
                            vib.vibrate(250);
                        }
                    }
                }
        );
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);

        // can't use onPrepareDialogBuilder for this as we want the dialog
        // to be kept open on click
        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            final Button defaultsButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
            if (defaultsButton != null) {
                defaultsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int progress = strengthToPercent(VIBRATOR_INTENSITY_DEFAULT_VALUE);
                        mSeekBar.setProgress(progress);

                        final String value = String.valueOf(percentToStrength(progress));
                        Utils.runRootCommand(Utils.getWriteCommand(FILE_VIBRATOR, value));
                    }
                });
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_DEVICE, "vibrator_tuning",
                    FILE_VIBRATOR, String.valueOf(mSeekBar.getProgress())));
        } else {
            Utils.runRootCommand(
                    Utils.getWriteCommand(FILE_VIBRATOR, String.valueOf(mOriginalValue)));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final boolean shouldWarn =
                progress >= strengthToPercent(VIBRATOR_INTENSITY_WARNING_THRESHOLD);
        if (mProgressDrawable != null) {
            mProgressDrawable.setColorFilter(shouldWarn ? mRedFilter : null);
        }
        if (mProgressThumb != null) {
            mProgressThumb.setColorFilter(shouldWarn ? mRedFilter : null);
        }
        mValue.setText(String.format("%d%%", progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final String value = String.valueOf(percentToStrength(seekBar.getProgress()));
        Utils.runRootCommand(Utils.getWriteCommand(FILE_VIBRATOR, value));
    }

    /**
     * Convert vibrator strength to percent
     */
    public static int strengthToPercent(int strength) {
        final double maxValue = VIBRATOR_INTENSITY_MAX;
        final double minValue = VIBRATOR_INTENSITY_MIN;

        double percent = (strength - minValue) * (100 / (maxValue - minValue));

        if (percent > 100) { percent = 100; } else if (percent < 0) { percent = 0; }

        return (int) percent;
    }

    /**
     * Convert percent to vibrator strength
     */
    public static int percentToStrength(int percent) {
        int strength = Math.round(
                (((VIBRATOR_INTENSITY_MAX - VIBRATOR_INTENSITY_MIN) * percent) / 100)
                        + VIBRATOR_INTENSITY_MIN
        );

        if (strength > VIBRATOR_INTENSITY_MAX) {
            strength = VIBRATOR_INTENSITY_MAX;
        } else if (strength < VIBRATOR_INTENSITY_MIN) { strength = VIBRATOR_INTENSITY_MIN; }

        return strength;
    }

    public static boolean isSupported() { return (!FILE_VIBRATOR.isEmpty()); }

}
