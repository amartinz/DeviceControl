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
package org.namelessrom.devicecontrol.fragments.tools.editor;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.objects.Prop;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachFragment;
import org.namelessrom.devicecontrol.adapters.PropAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.devicecontrol.Application.logDebug;

public class BuildPropEditorFragment extends AttachFragment
        implements DeviceConstants, FileConstants, AdapterView.OnItemClickListener {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final int REMOVE = 100;
    private static final int SAVE   = 200;

    private ListView       mListView;
    private LinearLayout   mLoadingView;
    private LinearLayout   mEmptyView;
    private RelativeLayout mTools;
    private View           mShadowTop, mShadowBottom;

    private       PropAdapter mAdapter = null;
    private       EditText    mFilter  = null;
    private final List<Prop>  mProps   = new ArrayList<Prop>();

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, ID_TOOLS_EDITORS_BUILD_PROP);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getBus().register(this);
        Utils.getCommandResult(-1, "cat /system/build.prop", null, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.tools_prop_list, container, false);

        mListView = findById(view, R.id.proplist);
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

        mLoadingView = findById(view, R.id.loading);
        mEmptyView = findById(view, R.id.nofiles);
        mTools = findById(view, R.id.tools);
        mFilter = findById(view, R.id.filter);
        mFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) { }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) { }

            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {
                if (mAdapter != null) {
                    final Editable filter = mFilter.getText();
                    mAdapter.getFilter().filter(filter != null ? filter.toString() : "");
                }
            }
        });

        mTools.setVisibility(View.GONE);

        mShadowTop = findById(view, R.id.tools_editor_shadow_top);
        mShadowBottom = findById(view, R.id.tools_editor_shadow_bottom);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);

        menu.removeItem(R.id.menu_action_apply);
        menu.removeItem(R.id.menu_action_toggle);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            case R.id.menu_action_add: {
                editBuildPropDialog(null);
                break;
            }
            default: {
                break;
            }
        }

        return false;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long row) {
        if (mAdapter == null) return;

        final Prop p = mAdapter.getItem(position);
        if (p != null) {
            if (!p.getName().contains("fingerprint")) {
                editBuildPropDialog(p);
            }
        }
    }

    @Subscribe
    public void onShellOutput(final ShellOutputEvent event) {
        final int id = event.getId();
        final String result = event.getOutput();
        switch (id) {
            case SAVE:
                Utils.remount("/system", "ro");
                break;
            case REMOVE:
                Utils.remount("/system", "ro");
                if (mAdapter != null) mAdapter.notifyDataSetChanged();
                break;
            default:
                logDebug("onReadPropsCompleted: " + result);
                if (isAdded()) {
                    loadBuildProp(result);
                } else {
                    logDebug("Not attached!");
                }
                break;
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    void loadBuildProp(final String s) {
        final Activity activity = getActivity();

        mProps.clear();
        final String p[] = s.split("\n");
        for (String aP : p) {
            if (!aP.contains("#") && aP.trim().length() > 0 && aP.contains("=")) {
                aP = aP.replace("[", "").replace("]", "");
                String pp[] = aP.split("=");
                if (pp.length >= 2) {
                    final StringBuilder sb = new StringBuilder();
                    for (int i = 2; i < pp.length; i++) {
                        sb.append('=').append(pp[i]);
                    }
                    mProps.add(new Prop(pp[0].trim(), pp[1].trim() + sb.toString()));
                } else {
                    mProps.add(new Prop(pp[0].trim(), ""));
                }
            }
        }
        Collections.sort(mProps);

        mLoadingView.setVisibility(View.GONE);
        if (mProps.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mTools.setVisibility(View.VISIBLE);
            mShadowTop.setVisibility(View.VISIBLE);
            mShadowBottom.setVisibility(View.VISIBLE);
            mAdapter = new PropAdapter(activity, mProps);
            mListView.setAdapter(mAdapter);
        }
    }

    //==============================================================================================
    // Dialogs
    //==============================================================================================

    private void editBuildPropDialog(final Prop p) {
        final Activity activity = getActivity();
        if (activity == null) return;

        String title;

        final View editDialog = Application.getLayoutInflater().inflate(
                R.layout.dialog_build_prop, null, false);
        final TextView tvName = findById(editDialog, R.id.prop_name_tv);
        final EditText etName = findById(editDialog, R.id.prop_name);
        final EditText etValue = findById(editDialog, R.id.prop_value);
        final Spinner sp = findById(editDialog, R.id.preset_spinner);
        final LinearLayout lpresets = findById(editDialog, R.id.prop_presets);
        final ArrayAdapter<CharSequence> vAdapter =
                new ArrayAdapter<CharSequence>(activity, android.R.layout.simple_spinner_item);
        vAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vAdapter.clear();

        if (p != null) {
            title = getString(R.string.edit_property);
            final String v = p.getVal();

            lpresets.setVisibility(View.GONE);
            if (v.equals("0")) {
                vAdapter.add("0");
                vAdapter.add("1");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            } else if (v.equals("1")) {
                vAdapter.add("1");
                vAdapter.add("0");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            } else if (v.equalsIgnoreCase("true")) {
                vAdapter.add("true");
                vAdapter.add("false");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            } else if (v.equalsIgnoreCase("false")) {
                vAdapter.add("false");
                vAdapter.add("true");
                lpresets.setVisibility(View.VISIBLE);
                sp.setAdapter(vAdapter);
            }
            tvName.setText(p.getName());
            etName.setText(p.getName());
            etName.setVisibility(EditText.GONE);
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
        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                    int position, long id) {
                if (sp.getSelectedItem() != null) {
                    etValue.setText(sp.getSelectedItem().toString().trim());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editDialog)
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(getString(R.string.save)
                        , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (p != null) {
                            if (etValue.getText() != null) {
                                final String name = p.getName();
                                final String value = etValue.getText().toString().trim();
                                p.setVal(value);
                                Utils.remount("/system", "rw");
                                Utils.getCommandResult(SAVE, Scripts.addOrUpdate(name, value));
                            }
                        } else {
                            if (etValue.getText() != null && etName.getText() != null) {
                                final String name = etName.getText().toString().trim();
                                if (name.length() > 0) {
                                    final String value = etValue.getText().toString().trim();
                                    mProps.add(new Prop(name, value));
                                    Utils.remount("/system", "rw");
                                    Utils.getCommandResult(SAVE, Scripts.addOrUpdate(name, value));
                                }
                            }
                        }

                        Collections.sort(mProps);
                        if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    }
                }).show();
    }

    private void makeDialog(final int title, final String msg, final Prop prop) {
        final Activity activity = getActivity();
        if (activity == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title)
                .setMessage(msg)
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(getString(android.R.string.yes), new DialogInterface
                        .OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.remount("/system", "rw");
                        Utils.getCommandResult(REMOVE, Scripts.removeProperty(prop.getName()));
                        if (mAdapter != null) mAdapter.remove(prop);
                    }
                });
        builder.show();
    }

}
