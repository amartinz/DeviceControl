/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;

public class DialogHelper {

    public static void openSeekbarDialog(final Activity activity, final int currentProgress,
            final String title, final int min, final int max, final Preference pref,
            final String path, final String category) {
        if (activity == null) return;

        final String cancel = App.get().getString(android.R.string.cancel);
        final String ok = App.get().getString(android.R.string.ok);
        final View alphaDialog = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_seekbar, null, false);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);

        seekbar.setMax(max);
        seekbar.setProgress(currentProgress);

        final EditText settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final Editable text = settingText.getText();
                    if (text != null) {
                        try {
                            final int val = Utils.parseInt(text.toString());
                            seekbar.setProgress(val);
                        } catch (Exception ignored) { /* ignored */ }
                    }
                    return true;
                }
                return false;
            }
        });
        settingText.setText(Integer.toString(currentProgress));
        settingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Utils.parseInt(s.toString());
                    if (val > max) {
                        s.replace(0, s.length(), Integer.toString(max));
                        val = max;
                    }
                    seekbar.setProgress(val);
                } catch (NumberFormatException ignored) { }
            }
        });

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                final int mSeekbarProgress = seekbar.getProgress();
                if (fromUser) {
                    settingText.setText(Integer.toString(mSeekbarProgress));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) { }
        });

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing
                            }
                        }
                )
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (settingText.getText() == null) return;
                        int val = Utils.parseInt(settingText.getText().toString());
                        if (val < min) {
                            val = min;
                        }
                        seekbar.setProgress(val);

                        final String newProgress = String.valueOf(seekbar.getProgress());
                        pref.setSummary(newProgress);
                        Utils.writeValue(path, newProgress);

                        BootupConfig.setBootup(
                                new BootupItem(category, pref.getKey(), path, newProgress, true));
                    }
                }).show();
    }

}
