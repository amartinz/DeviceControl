package org.namelessrom.devicecontrol.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
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

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 26.05.14.
 */
public class DialogHelper {

    public static void openSeekbarDialog(final Activity activity, final int currentProgress,
            final String title, final int min, final int max, final Preference pref,
            final String path, final String category) {
        if (activity == null) return;

        final Resources res = activity.getResources();
        final String cancel = res.getString(android.R.string.cancel);
        final String ok = res.getString(android.R.string.ok);
        final LayoutInflater factory = LayoutInflater.from(activity);
        final View alphaDialog = factory.inflate(R.layout.dialog_seekbar, null);

        final SeekBar seekbar = findById(alphaDialog, R.id.seek_bar);

        seekbar.setMax(max);
        seekbar.setProgress(currentProgress);

        final EditText settingText = findById(alphaDialog, R.id.setting_text);
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final Editable text = settingText.getText();
                    if (text != null) {
                        try {
                            final int val = Integer.parseInt(text.toString());
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
                    int val = Integer.parseInt(s.toString());
                    if (val > max) {
                        s.replace(0, s.length(), Integer.toString(max));
                        val = max;
                    }
                    seekbar.setProgress(val);
                } catch (NumberFormatException ignored) { }
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
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
                };
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

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
                        int val = Integer.parseInt(settingText.getText().toString());
                        if (val < min) {
                            val = min;
                        }
                        seekbar.setProgress(val);

                        final String newProgress = String.valueOf(seekbar.getProgress());
                        pref.setSummary(newProgress);
                        Utils.writeValue(path, newProgress);

                        PreferenceHelper.setBootup(
                                new DataItem(category, pref.getKey(), path, newProgress));
                    }
                }).create().show();
    }

}
