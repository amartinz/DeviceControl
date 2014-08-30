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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.text.InputType;
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

import com.negusoft.holoaccent.dialog.DividerPainter;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;

import butterknife.ButterKnife;

public class VoltageFragment extends AttachPreferenceFragment
        implements DeviceConstants, PerformanceConstants {

    private static final String PREF_UV  = "pref_uv";
    private static final String PREF_VDD = "pref_vdd";

    private PreferenceCategory mCategory;
    private Context            mContext;
    private String[]           mNames;
    private String[]           mValues;
    private LinearLayout       mButtonLayout;
    private boolean isVdd = false;

    @Override protected int getFragmentId() { return ID_VOLTAGE; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.voltage_control);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mCategory = (PreferenceCategory) findPreference("uv_category");

        if (Utils.fileExists(UV_TABLE_FILE)) {
            if (mCategory.getPreferenceCount() != 0) {
                mCategory.removeAll();
            }
            addPreferences(true);
            isVdd = false;
        } else {
            if (Utils.fileExists(VDD_TABLE_FILE)) {
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

        isSupported(getPreferenceScreen(), mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View v = inflater.inflate(R.layout.fragment_voltage, container, false);

        final ListView list = ButterKnife.findById(v, android.R.id.list);

        mButtonLayout = ButterKnife.findById(v, R.id.btn_layout);
        final Button mButtonApply = ButterKnife.findById(v, R.id.btn_apply);
        final Button mButtonCancel = ButterKnife.findById(v, R.id.btn_cancel);

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
                        execute.append(Utils.getWriteCommand(VDD_TABLE_FILE, value));
                    }
                    Utils.runRootCommand(execute.toString());
                    PreferenceHelper.setString(PREF_VDD, sb.toString().trim());
                } else {
                    for (int i = 0; i < count; i++) {
                        pref = (CustomPreference) mCategory.getPreference(i);
                        mValues[i] = pref.getKey();
                    }
                    final String table = buildTable(mValues);
                    PreferenceHelper.setString(PREF_UV, table);
                    Utils.writeValue(UV_TABLE_FILE, table);
                }
                mButtonLayout.setVisibility(View.GONE);
                list.bringToFront();
            }

        });

        mButtonLayout.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (Utils.fileExists(UV_TABLE_FILE) || Utils.fileExists(VDD_TABLE_FILE)) {
            inflater.inflate(R.menu.menu_voltage, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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

            @Override
            protected String doInBackground(String... params) {
                try {
                    mNames = CpuUtils.getUvValues(true);
                    mValues = CpuUtils.getUvValues(false);
                } catch (Exception exc) {
                    Logger.e(this, "UV ERROR: " + exc.getMessage());
                    return "ERROR";
                }
                Logger.v(this, "UV TABLE: " + buildTable(mValues));

                String name;
                CustomPreference pref;
                final int length = mNames.length;
                for (int i = 0; i < length; i++) {
                    final int j = i;
                    name = mNames[i];
                    pref = new CustomPreference(mContext);
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
                            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                            final LinearLayout ll = new LinearLayout(mContext);
                            ll.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));

                            final EditText et = new EditText(mContext);

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
                                                    Utils.writeValue(UV_TABLE_FILE, value);
                                                    p.setSummary(et.getText().toString());
                                                    p.setKey(et.getText().toString());
                                                    mValues[j] = p.getKey();
                                                } else {
                                                    final String value = et.getText().toString();
                                                    p.setSummary(value + " mV");
                                                    p.setKey(value);
                                                    mValues[j] = value;
                                                    Utils.writeValue(UV_TABLE_FILE,
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
                            new DividerPainter(mContext).paint(window);

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

    public static String restore() {
        final StringBuilder restore = new StringBuilder();

        if (Utils.fileExists(VDD_TABLE_FILE)) {
            final String value = PreferenceHelper.getString(PREF_VDD, "");
            Logger.v(VoltageFragment.class, "VDD Table: " + value);

            if (!value.isEmpty()) {
                final String[] values = value.split("XXX");
                for (final String s : values) {
                    restore.append(Utils.getWriteCommand(VDD_TABLE_FILE, s));
                }
            }
        } else if (Utils.fileExists(UV_TABLE_FILE)) {
            final String value = PreferenceHelper.getString(PREF_UV, "");
            Logger.v(VoltageFragment.class, "UV Table: " + value);

            if (!value.isEmpty()) {
                restore.append(Utils.getWriteCommand(UV_TABLE_FILE, value));
            }
        }

        final String cmd = restore.toString();
        if (!cmd.isEmpty()) {
            return cmd;
        } else {
            return "echo 'No UV to restore';\n";
        }
    }

}
