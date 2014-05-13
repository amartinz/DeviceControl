package org.namelessrom.devicecontrol.fragments.tools.sub.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
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
import org.namelessrom.devicecontrol.events.SubFragmentEvent;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

public class VmFragment extends AttachPreferenceFragment implements DeviceConstants {

    private CustomPreference mFullEditor;
    private CustomPreference mDirtyRatio;
    private CustomPreference mDirtyBackground;
    private CustomPreference mDirtyExpireCentisecs;
    private CustomPreference mDirtyWriteback;
    private CustomPreference mMinFreeK;
    private CustomPreference mOvercommit;
    private CustomPreference mSwappiness;
    private CustomPreference mVfs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.vm);

        mFullEditor = (CustomPreference) findPreference(PREF_FULL_EDITOR);
        mDirtyRatio = (CustomPreference) findPreference(PREF_DIRTY_RATIO);
        mDirtyBackground = (CustomPreference) findPreference(PREF_DIRTY_BACKGROUND);
        mDirtyExpireCentisecs = (CustomPreference) findPreference(PREF_DIRTY_EXPIRE);
        mDirtyWriteback = (CustomPreference) findPreference(PREF_DIRTY_WRITEBACK);
        mMinFreeK = (CustomPreference) findPreference(PREF_MIN_FREE_KB);
        mOvercommit = (CustomPreference) findPreference(PREF_OVERCOMMIT);
        mSwappiness = (CustomPreference) findPreference(PREF_SWAPPINESS);
        mVfs = (CustomPreference) findPreference(PREF_VFS);

        mDirtyRatio.setSummary(Utils.readOneLine(DIRTY_RATIO_PATH));
        mDirtyBackground.setSummary(Utils.readOneLine(DIRTY_BACKGROUND_PATH));
        mDirtyExpireCentisecs.setSummary(Utils.readOneLine(DIRTY_EXPIRE_PATH));
        mDirtyWriteback.setSummary(Utils.readOneLine(DIRTY_WRITEBACK_PATH));
        mMinFreeK.setSummary(Utils.readOneLine(MIN_FREE_PATH));
        mOvercommit.setSummary(Utils.readOneLine(OVERCOMMIT_PATH));
        mSwappiness.setSummary(Utils.readOneLine(SWAPPINESS_PATH));
        mVfs.setSummary(Utils.readOneLine(VFS_CACHE_PRESSURE_PATH));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mFullEditor) {
            BusProvider.getBus().post(new SubFragmentEvent(ID_TOOLS_EDITORS_VM));
            return true;
        } else if (preference == mDirtyRatio) {
            final String title = getString(R.string.dirty_ratio_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(DIRTY_RATIO_PATH));
            openDialog(currentProgress, title, 0, 100, preference,
                    DIRTY_RATIO_PATH, PREF_DIRTY_RATIO);
            return true;
        } else if (preference == mDirtyBackground) {
            final String title = getString(R.string.dirty_background_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(DIRTY_BACKGROUND_PATH));
            openDialog(currentProgress, title, 0, 100, preference,
                    DIRTY_BACKGROUND_PATH, PREF_DIRTY_BACKGROUND);
            return true;
        } else if (preference == mDirtyExpireCentisecs) {
            final String title = getString(R.string.dirty_expire_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(DIRTY_EXPIRE_PATH));
            openDialog(currentProgress, title, 0, 5000, preference,
                    DIRTY_EXPIRE_PATH, PREF_DIRTY_EXPIRE);
            return true;
        } else if (preference == mDirtyWriteback) {
            final String title = getString(R.string.dirty_writeback_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(DIRTY_WRITEBACK_PATH));
            openDialog(currentProgress, title, 0, 5000, preference,
                    DIRTY_WRITEBACK_PATH, PREF_DIRTY_WRITEBACK);
            return true;
        } else if (preference == mMinFreeK) {
            final String title = getString(R.string.min_free_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(MIN_FREE_PATH));
            openDialog(currentProgress, title, 0, 8192, preference,
                    MIN_FREE_PATH, PREF_MIN_FREE_KB);
            return true;
        } else if (preference == mOvercommit) {
            final String title = getString(R.string.overcommit_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(OVERCOMMIT_PATH));
            openDialog(currentProgress, title, 0, 100, preference,
                    OVERCOMMIT_PATH, PREF_OVERCOMMIT);
            return true;
        } else if (preference == mSwappiness) {
            final String title = getString(R.string.swappiness_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(SWAPPINESS_PATH));
            openDialog(currentProgress, title, 0, 100, preference,
                    SWAPPINESS_PATH, PREF_SWAPPINESS);
            return true;
        } else if (preference == mVfs) {
            final String title = getString(R.string.vfs_title);
            final int currentProgress = Integer.parseInt(
                    Utils.readOneLine(VFS_CACHE_PRESSURE_PATH));
            openDialog(currentProgress, title, 0, 200, preference,
                    VFS_CACHE_PRESSURE_PATH, PREF_VFS);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void openDialog(int currentProgress, String title, final int min, final int max,
            final Preference pref, final String path, final String key) {
        final Activity activity = getActivity();

        if (activity == null) return;

        final Resources res = activity.getResources();
        final String cancel = res.getString(android.R.string.cancel);
        final String ok = res.getString(android.R.string.ok);
        final LayoutInflater factory = LayoutInflater.from(activity);
        final View alphaDialog = factory.inflate(R.layout.dialog_seekbar, null);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);

        seekbar.setMax(max);
        seekbar.setProgress(currentProgress);

        final EditText settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int val = Integer.parseInt(settingText.getText().toString());
                    seekbar.setProgress(val);
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
                        int val = Integer.parseInt(settingText.getText().toString());
                        if (val < min) {
                            val = min;
                        }
                        seekbar.setProgress(val);
                        int newProgress = seekbar.getProgress();
                        pref.setSummary(Integer.toString(newProgress));
                        Utils.writeValue(path, Integer.toString(newProgress));
                        final SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(activity).edit();
                        editor.putInt(key, newProgress);
                        editor.commit();
                    }
                }).create().show();
    }
}
