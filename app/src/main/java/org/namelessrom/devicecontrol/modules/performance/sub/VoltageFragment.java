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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.models.ExtraConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import java.util.ArrayList;

import at.amartinz.execution.RootShell;

public class VoltageFragment extends AttachPreferenceFragment {
    private PreferenceCategory mCategory;
    private String[] mNames;
    private String[] mValues;
    private LinearLayout mButtonLayout;

    private boolean isVdd = false;

    @Override protected int getFragmentId() { return DeviceConstants.ID_VOLTAGE; }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.voltage_control);
        setHasOptionsMenu(true);

        mCategory = (PreferenceCategory) findPreference("uv_category");

        if (Utils.fileExists(VoltageUtils.UV_TABLE_FILE)) {
            if (mCategory.getPreferenceCount() != 0) {
                mCategory.removeAll();
            }
            addPreferences(true);
            isVdd = false;
        } else {
            if (Utils.fileExists(VoltageUtils.VDD_TABLE_FILE)) {
                if (mCategory.getPreferenceCount() != 0) {
                    mCategory.removeAll();
                }
                addPreferences(false);
                isVdd = true;
            } else {
                if (mCategory.getPreferenceCount() != 0) {
                    mCategory.removeAll();
                }
            }
        }

        isSupported(getPreferenceScreen(), getActivity());
    }

    @Override public View onCreateView(@NonNull final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstance) {
        final View v = inflater.inflate(R.layout.fragment_voltage, container, false);

        final ListView list = (ListView) v.findViewById(android.R.id.list);

        mButtonLayout = (LinearLayout) v.findViewById(R.id.btn_layout);
        final Button mButtonApply = (Button) v.findViewById(R.id.btn_apply);
        final Button mButtonCancel = (Button) v.findViewById(R.id.btn_cancel);

        mButtonCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int count = mCategory.getPreferenceCount();
                CustomPreference pref;
                for (int i = 0; i < count; i++) {
                    pref = (CustomPreference) mCategory.getPreference(i);
                    pref.restoreSummaryKey(mValues[i], mValues[i]);
                }
                mButtonLayout.setVisibility(View.GONE);
                list.bringToFront();
            }

        });

        mButtonApply.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final int count = mCategory.getPreferenceCount();
                CustomPreference pref;
                if (isVdd) {
                    String value;
                    final StringBuilder sb = new StringBuilder();
                    final StringBuilder execute = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        pref = (CustomPreference) mCategory.getPreference(i);
                        value = pref.getTitle() + " " + pref.getSummary();
                        mValues[i] = pref.getKey();
                        sb.append(value).append("XXX");
                        execute.append(Utils.getWriteCommand(VoltageUtils.VDD_TABLE_FILE, value));
                    }
                    RootShell.fireAndForget(execute.toString());
                    ExtraConfig.get().vdd = sb.toString().trim();
                    ExtraConfig.get().save();
                } else {
                    for (int i = 0; i < count; i++) {
                        pref = (CustomPreference) mCategory.getPreference(i);
                        mValues[i] = pref.getKey();
                    }
                    final String table = buildTable(mValues);
                    ExtraConfig.get().uv = table;
                    ExtraConfig.get().save();
                    Utils.writeValue(VoltageUtils.UV_TABLE_FILE, table);
                }

                BootupConfig.setBootup(new BootupItem(
                        BootupConfig.CATEGORY_VOLTAGE, BootupConfig.CATEGORY_VOLTAGE,
                        BootupConfig.CATEGORY_VOLTAGE, BootupConfig.CATEGORY_VOLTAGE,
                        false));

                mButtonLayout.setVisibility(View.GONE);
                list.bringToFront();
            }

        });

        mButtonLayout.setVisibility(View.GONE);

        return v;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (Utils.fileExists(VoltageUtils.UV_TABLE_FILE) || Utils.fileExists(
                VoltageUtils.VDD_TABLE_FILE)) {
            inflater.inflate(R.menu.menu_voltage, menu);
        }
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_plus:
                changeVoltage(true);
                return true;
            case R.id.action_minus:
                changeVoltage(false);
                return true;
        }

        return false;
    }

    private void changeVoltage(final boolean isPlus) {
        final int prefsIndex = mCategory.getPreferenceCount();
        CustomPreference pref;
        String value;
        boolean isCurrent = false;
        for (int i = 0; i < prefsIndex; i++) {
            pref = (CustomPreference) mCategory.getPreference(i);
            if (pref != null) {
                if (isVdd) {
                    if (isPlus) {
                        pref.setCustomSummaryKeyPlus(25000);
                    } else {
                        pref.setCustomSummaryKeyMinus(25000);
                    }
                } else {
                    if (isPlus) {
                        pref.setCustomSummaryKeyPlus(25);
                    } else {
                        pref.setCustomSummaryKeyMinus(25);
                    }
                }
                value = pref.getKey();
                if (value != null) {
                    isCurrent = value.equals(mValues[i]);
                }
            }
        }

        if (isCurrent) {
            mButtonLayout.setVisibility(View.GONE);
        } else {
            mButtonLayout.setVisibility(View.VISIBLE);
        }
    }

    public void addPreferences(final boolean millivolts) {

        class LongOperation extends AsyncTask<String, Void, String> {

            @Override protected String doInBackground(String... params) {
                try {
                    mNames = VoltageUtils.get().getUvValues(true);
                    mValues = VoltageUtils.get().getUvValues(false);
                } catch (Exception exc) {
                    return "ERROR";
                }

                String name;
                CustomPreference pref;
                final int length = mNames.length;
                for (int i = 0; i < length; i++) {
                    final int j = i;
                    name = mNames[i];
                    pref = new CustomPreference(getActivity());
                    pref.setTitle(name);
                    pref.areMilliVolts(millivolts);
                    if (isVdd) {
                        pref.setSummary(mValues[i]);
                    } else {
                        pref.setSummary(mValues[i] + " mV");
                    }
                    pref.setKey(mValues[i]);
                    mCategory.addPreference(pref);

                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(final Preference p) {
                            final AlertDialog.Builder builder =
                                    new AlertDialog.Builder(getActivity());

                            final LinearLayout ll = new LinearLayout(getActivity());
                            ll.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));

                            final EditText et = new EditText(getActivity());

                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(40, 40, 40, 40);
                            params.gravity = Gravity.CENTER;

                            final String val = p.getKey();

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
                                            if (et.getText() != null) {
                                                if (isVdd) {
                                                    final String value =
                                                            p.getTitle() + " " + et.getText()
                                                                    .toString();
                                                    Utils.writeValue(VoltageUtils.UV_TABLE_FILE,
                                                            value);
                                                    p.setSummary(et.getText().toString());
                                                    p.setKey(et.getText().toString());
                                                    mValues[j] = p.getKey();
                                                } else {
                                                    final String value = et.getText().toString();
                                                    p.setSummary(value + " mV");
                                                    p.setKey(value);
                                                    mValues[j] = value;
                                                    Utils.writeValue(VoltageUtils.UV_TABLE_FILE,
                                                            buildTable(mValues));
                                                }
                                            }
                                        }
                                    }
                            );
                            final AlertDialog dialog = builder.create();
                            dialog.show();

                            final Window window = dialog.getWindow();
                            window.setLayout(800, LayoutParams.WRAP_CONTENT);

                            return true;
                        }
                    });

                }
                return "Executed";
            }
        }
        new LongOperation().execute();
    }

    private String buildTable(final String[] vals) {
        final StringBuilder sb = new StringBuilder();
        final int length = vals.length;
        for (int j = 0; j < length; j++) {
            if (j != length - 1) {
                sb.append(vals[j]).append(' ');
            } else {
                sb.append(vals[j]);
            }
        }
        return sb.toString();
    }

    public static String restore(BootupConfig config) {
        final boolean hasVdd = Utils.fileExists(VoltageUtils.VDD_TABLE_FILE);
        final boolean hasUv = Utils.fileExists(VoltageUtils.UV_TABLE_FILE);
        if (!hasVdd && !hasUv) {
            return "";
        }

        final ArrayList<BootupItem> bootupItems = config
                .getItemsByCategory(BootupConfig.CATEGORY_VOLTAGE);
        if (bootupItems.size() == 0) {
            return "";
        }
        final BootupItem voltageBootupItem = bootupItems.get(0);
        if (voltageBootupItem == null || !voltageBootupItem.enabled) {
            return "";
        }

        final StringBuilder restore = new StringBuilder();
        if (hasVdd) {
            final String value = ExtraConfig.get().vdd;
            if (!TextUtils.isEmpty(value)) {
                final String[] values = value.split("XXX");
                for (final String s : values) {
                    restore.append(Utils.getWriteCommand(VoltageUtils.VDD_TABLE_FILE, s));
                }
            }
        } else {
            final String value = ExtraConfig.get().uv;
            if (!TextUtils.isEmpty(value)) {
                restore.append(Utils.getWriteCommand(VoltageUtils.UV_TABLE_FILE, value));
            }
        }

        return restore.toString();
    }

}
