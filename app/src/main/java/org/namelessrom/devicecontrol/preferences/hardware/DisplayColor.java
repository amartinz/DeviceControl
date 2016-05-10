/*
 * Copyright (C) 2013 The CyanogenMod Project
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import at.amartinz.hardware.display.DisplayColorCalibration;

/**
 * Special preference type that allows configuration of Color settings
 */
public class DisplayColor extends DialogPreference {
    // These arrays must all match in length and order
    private static final int[] SEEKBAR_ID = new int[]{
            R.id.color_red_seekbar,
            R.id.color_green_seekbar,
            R.id.color_blue_seekbar
    };

    private static final int[] SEEKBAR_VALUE_ID = new int[]{
            R.id.color_red_value,
            R.id.color_green_value,
            R.id.color_blue_value
    };

    private List<ColorSeekBar> mSeekBars = new ArrayList<>(SEEKBAR_ID.length);
    private String[] mCurrentColors;
    private String mOriginalColors;

    private final DisplayColorCalibration displayColorCalibration;

    public DisplayColor(Context context, AttributeSet attrs) {
        super(context, attrs);

        displayColorCalibration = new DisplayColorCalibration(context);

        setLayoutResource(R.layout.preference);
        setDialogLayoutResource(R.layout.preference_display_color_calibration);
    }

    @Override protected void onBindDialogView(@NonNull final View view) {
        super.onBindDialogView(view);

        mOriginalColors = displayColorCalibration.getCurColors();
        if (mOriginalColors != null) {
            mCurrentColors = mOriginalColors.split(" ");
        }

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            final SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            final TextView value = (TextView) view.findViewById(SEEKBAR_VALUE_ID[i]);
            final ColorSeekBar colorSeekBar = new ColorSeekBar(seekBar, value, i);
            mSeekBars.add(colorSeekBar);

            if (mCurrentColors != null) {
                colorSeekBar.setValueFromString(mCurrentColors[i]);
            }
        }
    }

    @Override protected void showDialog(final Bundle state) {
        super.showDialog(state);

        // Can't use onPrepareDialogBuilder for this as we want the dialog
        // to be kept open on click
        AlertDialog d = (AlertDialog) getDialog();
        Button defaultsButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
        defaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int defaultValue = displayColorCalibration.getDefValue();
                for (int i = 0; i < mSeekBars.size(); i++) {
                    mSeekBars.get(i).seekBar.setProgress(defaultValue);
                    mCurrentColors[i] = String.valueOf(defaultValue);
                }
                displayColorCalibration.setColors(TextUtils.join(" ", mCurrentColors));
            }
        });
    }

    @Override protected void onDialogClosed(final boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, DisplayColorCalibration.TAG,
                    displayColorCalibration.getPath(),
                    displayColorCalibration.getCurColors(), true));
        } else if (mOriginalColors != null) {
            displayColorCalibration.setColors(mOriginalColors);
        }
    }

    public static boolean isSupported() { return new DisplayColorCalibration(App.get()).isSupported(); }

    private class ColorSeekBar implements SeekBar.OnSeekBarChangeListener {
        private final int max;
        private final int min;

        private int index;
        private SeekBar seekBar;
        private TextView tvValue;

        public ColorSeekBar(SeekBar seekBar, TextView tvValue , int index) {
            this.max = displayColorCalibration.getMaxValue();
            this.min = displayColorCalibration.getMinValue();

            this.seekBar = seekBar;
            this.tvValue = tvValue ;
            this.index = index;

            this.seekBar.setMax(max - min);
            this.seekBar.setOnSeekBarChangeListener(this);
        }

        public void setValueFromString(String valueString) {
            seekBar.setProgress(Utils.parseInt(valueString));
        }

        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mCurrentColors == null) {
                return;
            }

            if (fromUser) {
                mCurrentColors[index] = String.valueOf(progress + min);
                displayColorCalibration.setColors(TextUtils.join(" ", mCurrentColors));
            }

            final int percent = Math.round(100F * progress / (max - min));
            tvValue.setText(String.format("%d%%", percent));
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }
}
