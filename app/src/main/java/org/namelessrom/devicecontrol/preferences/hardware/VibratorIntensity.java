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
package org.namelessrom.devicecontrol.preferences.hardware;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class VibratorIntensity extends DialogPreference implements SeekBar.OnSeekBarChangeListener {


    private final Vibrator vib;

    private static String[] paths;

    private String path;
    private int max;
    private int min;
    private int defValue;
    private int threshold;

    private SeekBar mSeekBar;
    private TextView mValue;
    private String mOriginalValue;
    private Drawable mProgressDrawable;

    private Drawable mProgressThumb = null;

    private static LightingColorFilter mRedFilter = null;

    public VibratorIntensity(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        setupValues(context);

        setLayoutResource(R.layout.preference);
        setDialogLayoutResource(R.layout.preference_dialog_vibrator_tuning);
    }

    private void setupValues(final Context context) {
        // get all the values
        final Resources res = context.getResources();
        if (paths == null) {
            paths = res.getStringArray(R.array.hardware_vibrator_paths);
        }
        final String[] maxs = res.getStringArray(R.array.hardware_vibrator_maximum);
        final String[] mins = res.getStringArray(R.array.hardware_vibrator_minimum);
        final String[] maxPaths = res.getStringArray(R.array.hardware_vibrator_maximum_path);
        final String[] minPaths = res.getStringArray(R.array.hardware_vibrator_minimum_path);
        final String[] defs = res.getStringArray(R.array.hardware_vibrator_default);
        final String[] thresholds = res.getStringArray(R.array.hardware_vibrator_threshold);

        final int length = paths.length;
        for (int i = 0; i < length; i++) {
            // if the file exists, set up the values
            if (Utils.fileExists(paths[i])) {
                // our existing path
                path = paths[i];
                // if we have a path for maximum, use it. else use the maximum defined value
                if (TextUtils.equals("-", maxPaths[i])) {
                    max = Utils.parseInt(maxs[i]);
                } else {
                    max = (Utils.parseInt(Utils.readOneLine(maxPaths[i])));
                }
                // same for minimum
                if (TextUtils.equals("-", minPaths[i])) {
                    min = Utils.parseInt(mins[i]);
                } else {
                    min = (Utils.parseInt(Utils.readOneLine(minPaths[i])));
                }
                // we can also use max or min for the default value
                if (TextUtils.equals("max", defs[i])) {
                    defValue = max;
                } else if (TextUtils.equals("min", defs[i])) {
                    defValue = min;
                } else {
                    defValue = Utils.parseInt(defs[i]);
                }
                // if the threshold is -1, we should not show the warning dialog
                threshold = Utils.parseInt(thresholds[i]);
                // and get out of here
                break;
            }
        }
    }

    @Override protected void onBindDialogView(@NonNull final View view) {
        super.onBindDialogView(view);

        final String def = String.format(getContext().getString(R.string.string_default) + ": %s",
                String.valueOf(strengthToPercent(defValue)) + "%");
        ((TextView) view.findViewById(R.id.vibrator_value_def)).setText(def);

        mSeekBar = (SeekBar) view.findViewById(R.id.vibrator_seekbar);
        mValue = (TextView) view.findViewById(R.id.vibrator_value);
        final TextView mWarning = (TextView) view.findViewById(R.id.textWarn);

        if (threshold != -1) {
            final String strWarnMsg = getContext().getString(
                    R.string.vibrator_warning, strengthToPercent(threshold) - 1);
            mWarning.setText(strWarnMsg);
        } else {
            mWarning.setVisibility(View.GONE);
        }

        final Drawable progressDrawable = mSeekBar.getProgressDrawable();
        if (progressDrawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) progressDrawable;
            mProgressDrawable = ld.findDrawableByLayerId(android.R.id.progress);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mProgressThumb = mSeekBar.getThumb();
        }
        if (mRedFilter == null) {
            mRedFilter = new LightingColorFilter(Color.BLACK,
                    getContext().getResources().getColor(android.R.color.holo_red_light));
        }

        // Read the current value from sysfs in case user wants to dismiss his changes
        mOriginalValue = Utils.readOneLine(path);

        // Restore percent value from SharedPreferences object
        final BootupItem item = BootupConfig.get().getItemByName("vibrator_tuning");
        final String value = (item != null) ? item.value : null;
        final int percent = strengthToPercent(value != null ? Utils.parseInt(mOriginalValue) : defValue);
        Timber.v("value: %s, percent: %s", value, percent);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setProgress(percent);

        view.findViewById(R.id.vibrator_test).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (vib != null) {
                    vib.cancel();
                    vib.vibrate(250);
                }
            }
        });
    }

    @Override protected void onDialogClosed(final boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, "vibrator_tuning",
                    path, String.valueOf(percentToStrength(mSeekBar.getProgress())), true));
        } else {
            RootShell.fireAndForget(Utils.getWriteCommand(path, String.valueOf(mOriginalValue)));
        }
    }

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        final boolean shouldWarn = (threshold != -1 && progress >= strengthToPercent(threshold));
        if (mProgressDrawable != null) {
            mProgressDrawable.setColorFilter(shouldWarn ? mRedFilter : null);
        }
        if (mProgressThumb != null) {
            mProgressThumb.setColorFilter(shouldWarn ? mRedFilter : null);
        }
        mValue.setText(String.format("%d%%", progress));
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) { }

    @Override public void onStopTrackingTouch(SeekBar seekBar) {
        final String value = String.valueOf(percentToStrength(seekBar.getProgress()));
        RootShell.fireAndForget(Utils.getWriteCommand(path, value));
    }

    /**
     * Convert vibrator strength to percent
     */
    private int strengthToPercent(final int strength) {
        final double percent = (strength - min) * (100 / (max - min != 0 ? max - min : 1));

        if (percent > 100) { return 100; } else if (percent < 0) { return 0; }

        return (int) percent;
    }

    /**
     * Convert percent to vibrator strength
     */
    private int percentToStrength(final int percent) {
        final int strength = Math.round((((max - min) * percent) / 100) + min);

        if (strength > max) {
            return max;
        } else if (strength < min) {
            return min;
        }

        return strength;
    }

    public static boolean isSupported() {
        if (paths == null) {
            paths = App.get().getStringArray(R.array.hardware_vibrator_paths);
        }
        return (Utils.fileExists(paths));
    }

}
