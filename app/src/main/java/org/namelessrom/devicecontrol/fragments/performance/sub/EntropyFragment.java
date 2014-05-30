package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.app.Activity;
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
import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceProgressFragment;
import org.namelessrom.devicecontrol.widgets.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.widgets.preferences.CustomPreference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class EntropyFragment extends AttachPreferenceProgressFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private CustomPreference         mEntropyAvail;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mRngActive;

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_ENTROPY);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

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

        mRngActive = (CustomCheckBoxPreference) findPreference("rng_active");
        if (mRngActive != null) {
            AppHelper.getProcess(RNG_PATH);
            mRngActive.setOnPreferenceChangeListener(this);
        }

        new RefreshTask().execute();

        isSupported(mRoot, getActivity());
    }

    @Override public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (mRngActive == preference) {
            if (!Utils.fileExists(RNG_PATH)) {
                logDebug(String.format("%s does not exist, downloading...", RNG_PATH));
                mRngActive.setEnabled(false);
                mProgressBar.setVisibility(View.VISIBLE);
                Ion.with(this)
                        .load(URL_RNG)
                        .progressBar(mProgressBar)
                        .write(new File(Application.getFilesDirectory() + "/rngd"))
                        .setCallback(new FutureCallback<File>() {
                            @Override public void onCompleted(Exception e, File result) {
                                if (e != null) {
                                    logDebug("Error downloading rngd!");
                                    if (mRngActive != null) {
                                        mRngActive.setSummary(R.string.error_download);
                                    }
                                    return;
                                }
                                if (mRngActive != null) {
                                    mRngActive.setEnabled(true);
                                }
                                Utils.remount("/system", "rw");
                                Utils.getCommandResult(-1, String.format("cp -f %s %s;\n" +
                                                "chmod 755 %s;\n",
                                        result.getAbsolutePath(), RNG_PATH, RNG_PATH
                                ));
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                return false;
            }
            if ((Boolean) o) {
                logDebug("Starting rngd");
                Utils.runRootCommand(String.format("%s -P", RNG_PATH));
            } else {
                logDebug("Stopping rngd");
                AppHelper.killProcess(RNG_PATH);
            }
            AppHelper.getProcess(RNG_PATH);
            return true;
        }

        return false;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            case R.id.menu_action_refresh:
                new RefreshTask().execute();
                AppHelper.getProcess(RNG_PATH);
            default:
                break;
        }

        return false;
    }

    @Subscribe public void onShellOutputEvent(final ShellOutputEvent event) {
        if (event == null) return;

        if (event.getId() == -1) {
            Utils.remount("/system", "ro");
            AppHelper.getProcess(RNG_PATH);
        } else if (event.getId() == ID_PGREP) {
            if (mRngActive != null) {
                final boolean isActive = event.getOutput() != null && !event.getOutput().isEmpty();
                mRngActive.setChecked(isActive);
                if (!Utils.fileExists(RNG_PATH)) {
                    mRngActive.setSummary(R.string.install_rng);
                } else {
                    mRngActive.setSummary("");
                }
            }
        }
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
                    logDebug("strings.get(0): " + tmp);
                    mEntropyAvail.setSummary(tmp);
                }
            }
        }
    }
}


