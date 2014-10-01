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
package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.listeners.OnShellOutputListener;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceProgressFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EntropyFragment extends AttachPreferenceProgressFragment
        implements DeviceConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener, OnShellOutputListener {

    //----------------------------------------------------------------------------------------------
    private CustomPreference         mEntropyAvail;
    private CustomCheckBoxPreference mRngActive;
    private CustomCheckBoxPreference mRngStartup;

    @Override protected int getFragmentId() { return ID_ENTROPY; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_entropy);
        setHasOptionsMenu(true);

        final PreferenceScreen mRoot = getPreferenceScreen();

        mEntropyAvail = (CustomPreference) findPreference("entropy_avail");
        if (mEntropyAvail != null) {
            if (!Utils.fileExists(ENTROPY_AVAIL)) {
                mRoot.removePreference(mEntropyAvail);
            }
        }

        mRngStartup = (CustomCheckBoxPreference) findPreference("rng_startup");
        if (mRngStartup != null) {
            mRngStartup.setChecked(PreferenceHelper.getBoolean("rng_startup", false));
            mRngStartup.setOnPreferenceChangeListener(this);
        }

        mRngActive = (CustomCheckBoxPreference) findPreference("rng_active");
        if (mRngActive != null) {
            AppHelper.getProcess(this, RNG_PATH);
            mRngActive.setOnPreferenceChangeListener(this);
        }

        new RefreshTask().execute();

        isSupported(mRoot, getActivity());
    }

    private void checkRngStartup() {
        if (mRngStartup == null) return;
        if (!Utils.fileExists(RNG_STARTUP_PATH)) {
            mRngStartup.setTitle(R.string.install_startup_script);
            mRngStartup.setSummary(R.string.startup_script_not_installed);
        } else {
            mRngStartup.setTitle(R.string.remove_startup_script);
            mRngStartup.setSummary(R.string.startup_script_installed);
        }
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mRngStartup == preference) {
            final boolean value = (Boolean) o;
            PreferenceHelper.setBoolean("rng_startup", value);
            return true;
        } else if (mRngActive == preference) {
            if (!Utils.fileExists(RNG_PATH)) {
                Logger.i(this, String.format("%s does not exist, downloading...", RNG_PATH));
                mRngActive.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);

                // check if file is already downloaded, and if, move it and return
                final File downloaded = new File(Application.get().getFilesDirectory() + "/rngd");
                if (downloaded.exists()) {
                    moveFile(downloaded);
                    return false;
                }

                // else download it
                Ion.with(this)
                        .load(URL_RNG)
                        .progressBar(mProgressBar)
                        .write(downloaded)
                        .setCallback(new FutureCallback<File>() {
                            @Override public void onCompleted(final Exception e, final File res) {
                                if (e != null) {
                                    Logger.e(this, "Error downloading rngd!");
                                    if (mRngActive != null) {
                                        mRngActive.setSummary(R.string.error_download);
                                    }
                                    return;
                                }
                                moveFile(res);
                            }
                        });
                return false;
            }
            if ((Boolean) o) {
                Logger.v(this, "Starting rngd");
                Utils.runRootCommand(String.format("%s -P", RNG_PATH));
            } else {
                Logger.v(this, "Stopping rngd");
                AppHelper.killProcess(RNG_PATH);
            }
            AppHelper.getProcess(this, RNG_PATH);
            return true;
        }

        return false;
    }

    private void moveFile(final File file) {
        if (mRngActive != null) {
            mRngActive.setEnabled(true);
        }
        Utils.remount("/system", "rw");
        Utils.getCommandResult(EntropyFragment.this, -1,
                String.format("cp -f %s %s;\nchmod 755 %s;\n",
                        file.getAbsolutePath(), RNG_PATH, RNG_PATH));
        mProgressBar.setVisibility(View.GONE);
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
                AppHelper.getProcess(this, RNG_PATH);
            default:
                break;
        }

        return false;
    }

    public void onShellOutput(final ShellOutputEvent event) {
        if (event == null) return;

        final int id = event.getId();
        if (id == -2) {
            Utils.remount("/system", "ro");
            checkRngStartup();
        } else if (id == -1) {
            Utils.remount("/system", "ro");
            AppHelper.getProcess(this, RNG_PATH);
        } else if (id == ID_PGREP) {
            if (mRngActive != null) {
                final boolean isActive = event.getOutput() != null && !event.getOutput().isEmpty();
                mRngActive.setChecked(isActive);
                if (!Utils.fileExists(RNG_PATH)) {
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
        if (PreferenceHelper.getBoolean("rng_startup", false)) {
            return "/system/bin/rngd -P;\n";
        }
        return "";
    }

    private class RefreshTask extends AsyncTask<Void, Void, List<String>> {

        @Override protected List<String> doInBackground(Void... params) {
            final ArrayList<String> list = new ArrayList<String>();

            list.add(Utils.readOneLine(ENTROPY_AVAIL));     // 0

            return list;
        }

        @Override protected void onPostExecute(final List<String> strings) {
            if (isAdded()) {
                String tmp;
                if (mEntropyAvail != null) {
                    tmp = strings.get(0);
                    Logger.v(this, "strings.get(0): " + tmp);
                    mEntropyAvail.setSummary(tmp);
                }
            }
        }
    }
}


