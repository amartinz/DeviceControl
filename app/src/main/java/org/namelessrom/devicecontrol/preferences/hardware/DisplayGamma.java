/*
 * Copyright (C) 2013 The CyanogenMod Project
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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;

import at.amartinz.hardware.display.DisplayGammaCalibration;

/**
 * Special preference type that allows configuration of Gamma settings
 */
public class DisplayGamma extends DialogPreference {
    private static final int[] BAR_COLORS = new int[]{
            R.string.red,
            R.string.green,
            R.string.blue
    };

    private GammaSeekBar[][] mSeekBars;

    private String[][] mCurrentColors;
    private String[] mOriginalColors;
    private int mNumberOfControls;

    private final DisplayGammaCalibration displayGammaCalibration;

    public DisplayGamma(Context context, AttributeSet attrs) {
        super(context, attrs);
        displayGammaCalibration = new DisplayGammaCalibration(context);

        if (!isSupported()) {
            return;
        }

        mNumberOfControls = displayGammaCalibration.getNumberOfControls();
        mSeekBars = new GammaSeekBar[mNumberOfControls][BAR_COLORS.length];

        mOriginalColors = new String[mNumberOfControls];
        mCurrentColors = new String[mNumberOfControls][];

        setLayoutResource(R.layout.preference);
        setDialogLayoutResource(R.layout.preference_display_gamma_calibration);
    }

    @SuppressLint("CommitPrefEdits") @Override protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);

        final ViewGroup container = (ViewGroup) view.findViewById(R.id.gamma_container);
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        final SharedPreferences prefs = getSharedPreferences();
        final Resources res = container.getResources();
        final String[] gammaDescriptors = displayGammaCalibration.getDescriptors();

        // Create multiple sets of seekbars, depending on the
        // number of controls the device has
        for (int index = 0; index < mNumberOfControls; index++) {
            mOriginalColors[index] = displayGammaCalibration.getCurGamma(index);
            mCurrentColors[index] = mOriginalColors[index].split(" ");

            final String defaultKey = "display_gamma_default_" + index;
            if (!prefs.contains(defaultKey)) {
                prefs.edit().putString(defaultKey, mOriginalColors[index]).commit();
            }

            if (mNumberOfControls != 1) {
                TextView header = (TextView) inflater.inflate(
                        R.layout.preference_display_gamma_calibration_header, container, false);

                if (index < gammaDescriptors.length) {
                    header.setText(gammaDescriptors[index]);
                } else {
                    header.setText(res.getString(
                            R.string.gamma_tuning_control_set_header, index + 1));
                }
                container.addView(header);
            }

            for (int color = 0; color < BAR_COLORS.length; color++) {
                ViewGroup item = (ViewGroup) inflater.inflate(
                        R.layout.preference_display_gamma_calibration_item, container, false);

                mSeekBars[index][color] = new GammaSeekBar(index, color, item);
                mSeekBars[index][color].setGamma(Integer.valueOf(mCurrentColors[index][color]));
                // make sure to add the seekbar group to the container _after_
                // creating GammaSeekBar, so that GammaSeekBar has a chance to
                // get the correct subviews without getting confused by duplicate IDs
                container.addView(item);
            }
        }
    }

    @Override protected void showDialog(Bundle state) {
        super.showDialog(state);

        // can't use onPrepareDialogBuilder for this as we want the dialog
        // to be kept open on click
        AlertDialog d = (AlertDialog) getDialog();
        Button defaultsButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
        defaultsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                for (int index = 0; index < mSeekBars.length; index++) {
                    final SharedPreferences prefs = getSharedPreferences();
                    final String defaultKey = "display_gamma_default_" + index;
                    // this key is guaranteed to be present, as we have
                    // created it in onBindDialogView()
                    final String pref = prefs.getString(defaultKey, null);
                    final String[] defaultColors = pref != null ? pref.split(" ") : mOriginalColors;

                    for (int color = 0; color < BAR_COLORS.length; color++) {
                        mSeekBars[index][color].setGamma(Integer.valueOf(defaultColors[color]));
                        mCurrentColors[index][color] = defaultColors[color];
                    }
                    displayGammaCalibration.setGamma(index,
                            TextUtils.join(" ", mCurrentColors[index]));
                }
            }
        });
    }

    @Override protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            final BootupConfig config = BootupConfig.get();
            for (int i = 0; i < mNumberOfControls; i++) {
                for (final String path : displayGammaCalibration.getPaths(i)) {
                    config.addItem(new BootupItem(
                            BootupConfig.CATEGORY_DEVICE, DisplayGammaCalibration.TAG + i,
                            path, displayGammaCalibration.getCurGamma(i), true));
                }
            }
            config.save();
        } else if (mOriginalColors != null) {
            for (int i = 0; i < mNumberOfControls; i++) {
                displayGammaCalibration.setGamma(i, mOriginalColors[i]);
            }
        }
    }

    @Override protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (getDialog() == null || !getDialog().isShowing()) {
            return superState;
        }

        // Save the dialog state
        final SavedState myState = new SavedState(superState);
        myState.controlCount = mNumberOfControls;
        myState.currentColors = mCurrentColors;
        myState.originalColors = mOriginalColors;

        // Restore the old state when the activity or dialog is being paused
        for (int i = 0; i < mNumberOfControls; i++) {
            displayGammaCalibration.setGamma(i, mOriginalColors[i]);
        }
        mOriginalColors = null;

        return myState;
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        mNumberOfControls = myState.controlCount;
        mOriginalColors = myState.originalColors;
        mCurrentColors = myState.currentColors;

        for (int index = 0; index < mNumberOfControls; index++) {
            for (int color = 0; color < BAR_COLORS.length; color++) {
                mSeekBars[index][color].setGamma(Integer.valueOf(mCurrentColors[index][color]));
            }
            displayGammaCalibration.setGamma(index, TextUtils.join(" ", mCurrentColors[index]));
        }
    }

    public static boolean isSupported() {
        return new DisplayGammaCalibration(App.get()).isSupported();
    }

    private static class SavedState extends BaseSavedState {
        int controlCount;
        String[] originalColors;
        String[][] currentColors;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            controlCount = source.readInt();
            originalColors = source.createStringArray();
            currentColors = new String[controlCount][];
            for (int i = 0; i < controlCount; i++) {
                currentColors[i] = source.createStringArray();
            }
        }

        @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(controlCount);
            dest.writeStringArray(originalColors);
            for (int i = 0; i < controlCount; i++) {
                dest.writeStringArray(currentColors[i]);
            }
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    private class GammaSeekBar implements SeekBar.OnSeekBarChangeListener {
        private int mControlIndex;
        private int mColorIndex;
        private int mMin;
        private SeekBar mSeekBar;
        private TextView mValue;

        public GammaSeekBar(int controlIndex, int colorIndex, ViewGroup container) {
            mControlIndex = controlIndex;
            mColorIndex = colorIndex;

            mMin = displayGammaCalibration.getMinValue(controlIndex);

            mValue = (TextView) container.findViewById(R.id.color_value);
            mSeekBar = (SeekBar) container.findViewById(R.id.color_seekbar);

            TextView label = (TextView) container.findViewById(R.id.color_text);
            label.setText(container.getContext().getString(BAR_COLORS[colorIndex]));

            mSeekBar.setMax(displayGammaCalibration.getMaxValue(controlIndex) - mMin);
            mSeekBar.setProgress(0);
            mValue.setText(String.valueOf(mSeekBar.getProgress() + mMin));

            // this must be done last, we don't want to apply our initial value to the hardware
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void setGamma(int gamma) {
            mSeekBar.setProgress(gamma - mMin);
        }

        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mCurrentColors[mControlIndex][mColorIndex] = String.valueOf(progress + mMin);
                displayGammaCalibration.setGamma(mControlIndex,
                        TextUtils.join(" ", mCurrentColors[mControlIndex]));
            }
            mValue.setText(String.valueOf(progress + mMin));
        }

        @Override public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }
    }
}
