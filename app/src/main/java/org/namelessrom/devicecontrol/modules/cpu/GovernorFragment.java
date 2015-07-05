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
package org.namelessrom.devicecontrol.modules.cpu;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreferenceCategoryMaterial;
import org.namelessrom.devicecontrol.ui.views.AttachMaterialPreferenceFragment;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

import alexander.martinz.libs.materialpreferences.MaterialEditTextPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreference;

public class GovernorFragment extends AttachMaterialPreferenceFragment implements GovernorUtils.GovernorListener {
    private CustomPreferenceCategoryMaterial mCategory;

    @Override protected int getFragmentId() { return DeviceConstants.ID_GOVERNOR_TUNABLE; }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCategory = new CustomPreferenceCategoryMaterial(getActivity());
        mCategory.init(getActivity());
        mCategory.setKey("key_gov_category");
        addPreference(mCategory);

        mCategory.post(new Runnable() {
            @Override public void run() {
                GovernorUtils.get().getGovernor(GovernorFragment.this);
            }
        });
    }

    @Override public void onGovernor(@NonNull final GovernorUtils.Governor governor) {
        final File governorDir = new File("/sys/devices/system/cpu/cpufreq/" + governor.current);
        boolean hasTunables = governorDir.exists();
        if (!hasTunables) {
            mCategory.setTitle(getString(R.string.no_gov_tweaks_message));
            return;
        }

        mCategory.setTitle(getString(R.string.gov_tweaks, governor.current));
        new AddPreferences(getActivity(), governorDir).execute();
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

    private class AddPreferences extends AsyncTask<Void, Void, Void> implements MaterialPreference.MaterialPreferenceChangeListener {
        private Activity mActivity;
        private File mGovernorDir;

        public AddPreferences(Activity activity, File governorDir) {
            mActivity = activity;
            mGovernorDir = governorDir;
        }

        @Override protected Void doInBackground(Void... params) {
            if (!mGovernorDir.exists()) {
                return null;
            }
            final File[] files = mGovernorDir.listFiles();
            for (final File file : files) {
                final String fileName = file.getName();

                // Do not try to read boostpulse
                if ("boostpulse".equals(fileName)) {
                    continue;
                }

                final String filePath = file.getAbsolutePath();
                final String fileContent = Utils.readOneLine(filePath).trim().replaceAll("\n", "");
                mActivity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        final MaterialEditTextPreference pref =
                                new MaterialEditTextPreference(mActivity);
                        pref.setAsCard(false);
                        pref.init(mActivity);
                        pref.setKey(filePath);
                        pref.setTitle(fileName);
                        pref.setValue(fileContent);
                        mCategory.post(new Runnable() {
                            @Override public void run() {
                                mCategory.addPreference(pref);
                                pref.setOnPreferenceChangeListener(AddPreferences.this);
                            }
                        });
                    }
                });
            }

            return null;
        }

        @Override
        public boolean onPreferenceChanged(MaterialPreference materialPreference, Object o) {
            final String value = String.valueOf(o);
            Utils.writeValue(materialPreference.getKey(), value);
            updateBootupListDb(materialPreference, value);
            return false;
        }
    }

    private static void updateBootupListDb(final MaterialPreference p, final String value) {
        new BootupTask(p.getTitle(), p.getKey(), value).execute();
    }

    private static class BootupTask extends AsyncTask<String, Void, Void> {
        private final String name;
        private final String key;
        private final String value;

        public BootupTask(String name, String key, String value) {
            this.name = name;
            this.key = key;
            this.value = value;
        }

        @Override protected Void doInBackground(String... params) {
            BootupConfig.setBootup(new BootupItem(
                    BootupConfig.CATEGORY_CPU, name, key, value, true));
            return null;
        }
    }

}
