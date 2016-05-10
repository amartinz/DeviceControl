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
package org.namelessrom.devicecontrol.modules.performance.sub;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.ExtraUtils;
import org.namelessrom.devicecontrol.models.ExtraConfig;
import org.namelessrom.devicecontrol.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.preferences.CustomTogglePreference;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachPreferenceProgressFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class EntropyFragment extends AttachPreferenceProgressFragment implements Preference.OnPreferenceChangeListener, ShellOutput.OnShellOutputListener {
    private static final String URL_RNG =
            "http://sourceforge.net/projects/namelessrom/files/romextras/binaries/rngd/download";
    private static final File RNGD = new File(App.get().getFilesDirectory(), "rngd");

    private CustomPreference mEntropyAvail;
    private AwesomeEditTextPreference mReadWakeupThreshold;
    private AwesomeEditTextPreference mWriteWakeupThreshold;

    private CustomTogglePreference mRngActive;
    private CustomTogglePreference mRngStartup;

    @Override protected int getFragmentId() { return DeviceConstants.ID_ENTROPY; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_entropy);
        setHasOptionsMenu(true);

        PreferenceCategory category = (PreferenceCategory) findPreference("entropy");
        mEntropyAvail = (CustomPreference) findPreference("entropy_avail");
        if (!Utils.fileExists(ExtraUtils.ENTROPY_AVAIL)) {
            category.removePreference(mEntropyAvail);
        }
        mReadWakeupThreshold =
                (AwesomeEditTextPreference) findPreference("entropy_read_wakeup_threshold");
        if (mReadWakeupThreshold.isSupported()) {
            mReadWakeupThreshold.initValue();
            mReadWakeupThreshold.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mReadWakeupThreshold);
        }
        mWriteWakeupThreshold =
                (AwesomeEditTextPreference) findPreference("entropy_write_wakeup_threshold");
        if (mWriteWakeupThreshold.isSupported()) {
            mWriteWakeupThreshold.initValue();
            mWriteWakeupThreshold.setOnPreferenceChangeListener(this);
        } else {
            category.removePreference(mWriteWakeupThreshold);
        }

        // category = (PreferenceCategory) findPreference("rng");
        mRngStartup = (CustomTogglePreference) findPreference(ExtraConfig.RNG_STARTUP);
        mRngStartup.setChecked(ExtraConfig.get().rngStartup);
        mRngStartup.setOnPreferenceChangeListener(this);


        mRngActive = (CustomTogglePreference) findPreference("rng_active");
        AppHelper.getProcess(this, RNGD.getAbsolutePath());
        mRngActive.setOnPreferenceChangeListener(this);

        new RefreshTask().execute();

        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mReadWakeupThreshold == preference) {
            final String value = String.valueOf(o);
            mReadWakeupThreshold.writeValue(value);
            return true;
        } else if (mWriteWakeupThreshold == preference) {
            final String value = String.valueOf(o);
            mWriteWakeupThreshold.writeValue(value);
            return true;
        } else if (mRngStartup == preference) {
            ExtraConfig.get().rngStartup = (Boolean) o;
            ExtraConfig.get().save();
            return true;
        } else if (mRngActive == preference) {
            if (!RNGD.exists()) {
                Timber.i("%s does not exist, downloading...", RNGD.getAbsolutePath());
                mRngActive.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);

                // else download it
                Ion.with(this)
                        .load(URL_RNG)
                        .progress(mProgressBar)
                        .write(RNGD)
                        .setCallback(new FutureCallback<File>() {
                            @Override public void onCompleted(final Exception e, final File res) {
                                if (e != null) {
                                    Timber.e("Error downloading rngd!");
                                    if (mRngActive != null) {
                                        mRngActive.setSummary(R.string.error_download);
                                    }
                                    return;
                                }

                                if (mRngActive != null) {
                                    mRngActive.setEnabled(true);
                                }

                                setRngdPermissions();
                                AppHelper.getProcess(EntropyFragment.this, RNGD.getAbsolutePath());

                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                return false;
            }

            setRngdPermissions();

            if ((Boolean) o) {
                Timber.v("Starting rngd");
                RootShell.fireAndForget(String.format("%s -P;\n", RNGD.getAbsolutePath()));
            } else {
                Timber.v("Stopping rngd");
                AppHelper.killProcess(getActivity(), RNGD.getAbsolutePath());
            }
            AppHelper.getProcess(this, RNGD.getAbsolutePath());
            return true;
        }

        return false;
    }

    public void setRngdPermissions() {
        Timber.v("RNGD --> setReadable: %s, setExectuable: %s",
                RNGD.setReadable(true, false),
                RNGD.setExecutable(true, false));
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_refresh:
                new RefreshTask().execute();
                AppHelper.getProcess(this, RNGD.getAbsolutePath());
            default:
                break;
        }

        return false;
    }

    public void onShellOutput(final ShellOutput shellOutput) {
        if (shellOutput == null) { return; }

        if (shellOutput.id == DeviceConstants.ID_PGREP) {
            if (mRngActive != null) {
                final boolean isActive =
                        shellOutput.output != null && !shellOutput.output.isEmpty();
                mRngActive.setChecked(isActive);
                if (!RNGD.exists()) {
                    mRngActive.setSummary(R.string.install_rng);
                    mRngStartup.setEnabled(false);
                } else {
                    mRngActive.setSummary("");
                    mRngStartup.setEnabled(true);
                }
            }
        }
    }

    public static String restore() {
        if (ExtraConfig.get().rngStartup) {
            return String.format("%s -P;\n", RNGD.getAbsolutePath());
        }
        return "";
    }

    private class RefreshTask extends AsyncTask<Void, Void, List<String>> {

        @Override protected List<String> doInBackground(Void... params) {
            final ArrayList<String> list = new ArrayList<>();

            list.add(Utils.readOneLine(ExtraUtils.ENTROPY_AVAIL));     // 0

            return list;
        }

        @Override protected void onPostExecute(final List<String> strings) {
            if (isAdded()) {
                String tmp;
                if (mEntropyAvail != null) {
                    tmp = strings.get(0);
                    Timber.v("strings.get(0): %s", tmp);
                    mEntropyAvail.setSummary(tmp);
                }
            }
        }
    }
}


