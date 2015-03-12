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
package org.namelessrom.devicecontrol.cpu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.BootupConfiguration;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.ui.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

public class GovernorFragment extends AttachPreferenceFragment implements GovernorUtils.GovernorListener {

    private PreferenceCategory mCategory;
    private Context mContext;

    @Override protected int getFragmentId() { return DeviceConstants.ID_GOVERNOR_TUNABLE; }

    @Override public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.governor);

        mCategory = (PreferenceCategory) findPreference("key_gov_category");
        mContext = getActivity();

        GovernorUtils.get().getGovernor(this);
    }

    @Override public void onGovernor(@NonNull final GovernorUtils.Governor governor) {
        if (new File("/sys/devices/system/cpu/cpufreq/" + governor.current).exists()) {
            mCategory.setTitle(getString(R.string.gov_tweaks, governor.current));
            new addPreferences().execute(governor.current);
        } else {
            getPreferenceScreen().removeAll();
        }

        isSupported(getPreferenceScreen(), mContext, R.string.no_gov_tweaks_message);
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
            default:
                break;
        }

        return false;
    }

    private class addPreferences extends AsyncTask<String, Void, Void> {

        @Override protected Void doInBackground(String... params) {
            if (mCategory.getPreferenceCount() != 0) {
                mCategory.removeAll();
            }
            final String currentGovernor = params[0];
            final File f = new File("/sys/devices/system/cpu/cpufreq/" + currentGovernor);
            if (f.exists()) {
                final File[] files = f.listFiles();
                for (final File file : files) {
                    final String fileName = file.getName();

                    // Do not try to read boostpulse
                    if ("boostpulse".equals(fileName)) {
                        continue;
                    }

                    final String filePath = file.getAbsolutePath();
                    final String fileContent = Utils.readOneLine(filePath).trim()
                            .replaceAll("\n", "");
                    final CustomPreference pref = new CustomPreference(mContext);
                    pref.setTitle(fileName);
                    pref.setSummary(fileContent);
                    pref.setKey(filePath);
                    mCategory.addPreference(pref);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(final Preference p) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            LinearLayout ll = new LinearLayout(mContext);
                            ll.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            final EditText et = new EditText(mContext);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(40, 40, 40, 40);
                            params.gravity = Gravity.CENTER;
                            String val = p.getSummary().toString();
                            et.setLayoutParams(params);
                            et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                            et.setGravity(Gravity.CENTER_HORIZONTAL);
                            et.setText(val);
                            ll.addView(et);
                            builder.setView(ll);
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String value = et.getText().toString();
                                            p.setSummary(value);
                                            Utils.writeValue(p.getKey(), value);
                                            updateBootupListDb(p, value);
                                        }
                                    }
                            );
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                            final Window window = dialog.getWindow();
                            window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                            return true;
                        }

                    });
                }
            }

            return null;
        }

    }

    private static void updateBootupListDb(final Preference p, final String value) {

        class updateListDb extends AsyncTask<String, Void, Void> {

            @Override protected Void doInBackground(String... params) {
                final String name = p.getTitle().toString();
                final String key = p.getKey();
                BootupConfiguration.setBootup(Application.get(), new BootupItem(
                        DatabaseHandler.CATEGORY_CPU, name, key, value, true));

                return null;
            }

        }
        new updateListDb().execute();
    }

}
