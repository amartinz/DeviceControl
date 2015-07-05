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

package org.namelessrom.devicecontrol.ui.preferences.hardware;

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

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.hardware.DisplayColorCalibration;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.List;

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

    public DisplayColor(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference);
        setDialogLayoutResource(R.layout.preference_display_color_calibration);
    }

    @Override protected void onBindDialogView(@NonNull final View view) {
        super.onBindDialogView(view);

        mOriginalColors = DisplayColorCalibration.get().getCurColors();
        Logger.v(this, "mOriginalColors -> %s", mOriginalColors);
        mCurrentColors = mOriginalColors.split(" ");

        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView value = (TextView) view.findViewById(SEEKBAR_VALUE_ID[i]);
            ColorSeekBar colorSeekBar = new ColorSeekBar(seekBar, value, i);
            mSeekBars.add(colorSeekBar);
            colorSeekBar.setValueFromString(mCurrentColors[i]);
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
                final int defaultValue = DisplayColorCalibration.get().getDefValue();
                for (int i = 0; i < mSeekBars.size(); i++) {
                    mSeekBars.get(i).mSeekBar.setProgress(defaultValue);
                    mCurrentColors[i] = String.valueOf(defaultValue);
                }
                DisplayColorCalibration.get().setColors(TextUtils.join(" ", mCurrentColors));
            }
        });
    }

    @Override protected void onDialogClosed(final boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_DEVICE, DisplayColorCalibration.TAG,
                    DisplayColorCalibration.get().getPath(),
                    DisplayColorCalibration.get().getCurColors(), true));
        } else if (mOriginalColors != null) {
            DisplayColorCalibration.get().setColors(mOriginalColors);
        }
    }

    public static boolean isSupported() { return DisplayColorCalibration.get().isSupported(); }

    private class ColorSeekBar implements SeekBar.OnSeekBarChangeListener {
        private int mIndex;
        private SeekBar mSeekBar;
        private TextView mValue;

        public ColorSeekBar(SeekBar seekBar, TextView value, int index) {
            mSeekBar = seekBar;
            mValue = value;
            mIndex = index;

            mSeekBar.setMax(DisplayColorCalibration.get().getMaxValue() -
                    DisplayColorCalibration.get().getMinValue());
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void setValueFromString(String valueString) {
            mSeekBar.setProgress(Utils.parseInt(valueString));
        }

        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            final int min = DisplayColorCalibration.get().getMinValue();
            final int max = DisplayColorCalibration.get().getMaxValue();

            if (fromUser) {
                mCurrentColors[mIndex] = String.valueOf(progress + min);
                DisplayColorCalibration.get().setColors(TextUtils.join(" ", mCurrentColors));
            }

            final int percent = Math.round(100F * progress / (max - min));
            mValue.setText(String.format("%d%%", percent));
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }
}
