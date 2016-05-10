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
package org.namelessrom.devicecontrol.modules.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

import at.amartinz.execution.BusyBox;
import timber.log.Timber;

public class BuildPropEditorFragment extends BaseEditorFragment {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final int REMOVE = 100;
    private static final int SAVE = 200;

    private ListView mListView;
    private LinearLayout mLoadingView;

    private PropAdapter mAdapter = null;
    private final ArrayList<Prop> mProps = new ArrayList<>();

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_EDITORS_BUILD_PROP; }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Utils.getCommandResult(BuildPropEditorFragment.this, -1, "cat /system/build.prop", true);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.tools_prop_list, container, false);

        mListView = (ListView) view.findViewById(R.id.proplist);
        mListView.setOnItemClickListener(this);
        mListView.setFastScrollEnabled(true);
        mListView.setFastScrollAlwaysVisible(true);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, final View view
                    , final int i, final long l) {
                final Prop p = (Prop) adapterView.getItemAtPosition(i);
                if (p != null && !p.getName().contains("fingerprint")) {
                    makeDialog(R.string.delete_property,
                            getString(R.string.delete_property_message, p.getName()), p);
                }
                return true;
            }
        });

        mLoadingView = (LinearLayout) view.findViewById(R.id.loading);

        final LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.nofiles);
        mListView.setEmptyView(emptyView);

        return view;
    }

    @Override protected PropAdapter getAdapter() {
        return mAdapter;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // remove unused items
        menu.removeItem(R.id.menu_action_apply);
        menu.removeItem(R.id.menu_action_toggle);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_add: {
                editBuildPropDialog(null);
                return true;
            }
        }

        return false;
    }

    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long row) {
        if (mAdapter == null) {
            return;
        }

        final Prop p = mAdapter.getItem(position);
        if (p != null) {
            if (!p.getName().contains("fingerprint")) {
                editBuildPropDialog(p);
            }
        }
    }

    @Override
    public void onShellOutput(final ShellOutput shellOutput) {
        switch (shellOutput.id) {
            case SAVE:
            case REMOVE:
                Utils.remount("/system", "ro");
                Collections.sort(mProps);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            default:
                Timber.v("onReadPropsCompleted: %s", shellOutput.output);
                if (isAdded()) {
                    loadBuildProp(shellOutput.output);
                } else {
                    Timber.w("Not attached!");
                }
                break;
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadBuildProp(final String s) {
        mProps.clear();
        final String p[] = s.split("\n");
        for (String aP : p) {
            if (TextUtils.isEmpty(aP) || aP.contains("#") || !aP.contains("=")) {
                continue;
            }

            aP = aP.trim();
            if (aP.length() <= 0) {
                continue;
            }

            aP = aP.replace("[", "").replace("]", "");
            final String pp[] = aP.split("=");

            final Prop prop;
            if (pp.length >= 2) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 2; i < pp.length; i++) {
                    sb.append('=').append(pp[i]);
                }
                prop = new Prop(pp[0].trim(), pp[1].trim() + sb.toString());
            } else {
                prop = new Prop(pp[0].trim(), "");
            }
            if (!mProps.contains(prop)) {
                mProps.add(prop);
            }
        }
        Collections.sort(mProps);

        mLoadingView.setVisibility(View.GONE);

        mAdapter = new PropAdapter(getActivity(), mProps);
        mListView.setAdapter(mAdapter);
    }

    //==============================================================================================
    // Dialogs
    //==============================================================================================
    private boolean mSpinnerHelper;

    private void editBuildPropDialog(final Prop p) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        final View editDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_build_prop, null, false);
        final TextView tvName = (TextView) editDialog.findViewById(R.id.prop_name_tv);
        final EditText etName = (EditText) editDialog.findViewById(R.id.prop_name);
        final EditText etValue = (EditText) editDialog.findViewById(R.id.prop_value);
        final Spinner sp = (Spinner) editDialog.findViewById(R.id.preset_spinner);
        final LinearLayout lpresets = (LinearLayout) editDialog.findViewById(R.id.prop_presets);
        final ArrayAdapter<CharSequence> vAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vAdapter.clear();

        final String title;
        if (p != null) {
            title = getString(R.string.edit_property);
            final String v = p.getVal();

            lpresets.setVisibility(View.GONE);
            if ("0".equals(v) || "1".equals(v)) {
                vAdapter.add("0");
                vAdapter.add("1");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            } else if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                vAdapter.add("false");
                vAdapter.add("true");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            tvName.setText(p.getName());
            etName.setText(p.getName());
            etName.setVisibility(View.GONE);
            etValue.setText(p.getVal());
        } else {
            title = getString(R.string.add_property);
            vAdapter.add("");
            vAdapter.add("0");
            vAdapter.add("1");
            vAdapter.add("true");
            vAdapter.add("false");
            sp.setAdapter(vAdapter);
            lpresets.setVisibility(View.VISIBLE);
            etName.setVisibility(View.VISIBLE);
        }

        // fuuuu, stupid spinner bugs
        mSpinnerHelper = false;
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!mSpinnerHelper) {
                    mSpinnerHelper = true;
                    return;
                }

                final Object item = sp.getSelectedItem();
                if (item != null) {
                    etValue.setText(item.toString().trim());
                }
            }

            @Override public void onNothingSelected(AdapterView<?> parentView) { }
        });

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editDialog)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        if (etValue.getText() == null) {
                            return;
                        }

                        final String name;
                        final String value;

                        if (p != null) {
                            name = p.getName();
                            value = etValue.getText().toString().trim();
                            p.setVal(value);
                        } else {
                            if (etName.getText() == null) {
                                return;
                            }
                            name = etName.getText().toString().trim();
                            if (name.length() <= 0) {
                                return;
                            }
                            value = etValue.getText().toString().trim();
                            mProps.add(new Prop(name, value));
                        }

                        final String addCmd = Scripts.addOrUpdate(name, value);
                        final String cmd = BusyBox.callBusyBoxApplet("mount", "-o rw,remount /system;") + addCmd;
                        Utils.getCommandResult(BuildPropEditorFragment.this, SAVE, cmd);
                    }
                }).show();
    }

    private void makeDialog(final int title, final String msg, final Prop prop) {
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(msg)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.remount("/system", "rw");
                        final String cmd = Scripts.removeProperty(prop.getName());
                        Utils.getCommandResult(BuildPropEditorFragment.this, REMOVE, cmd);
                        if (mAdapter != null) {
                            mAdapter.remove(prop);
                        }
                    }
                }).show();
    }

}
