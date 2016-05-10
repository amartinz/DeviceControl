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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Scripts;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import at.amartinz.execution.BusyBox;
import at.amartinz.execution.RootShell;
import timber.log.Timber;

public class SysctlEditorFragment extends BaseEditorFragment {

    //==============================================================================================
    // Fields
    //==============================================================================================
    private static final int APPLY = 200;
    private static final int SAVE = 201;

    private ListView mListView;
    private LinearLayout mLoadingView;

    private PropAdapter mAdapter = null;
    private final ArrayList<Prop> mProps = new ArrayList<>();

    private boolean mLoadFull = false;

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_EDITORS_VM; }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAllTheStuff();
    }

    @Override protected PropAdapter getAdapter() {
        return mAdapter;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.tools_prop_list, container, false);

        mListView = (ListView) view.findViewById(R.id.proplist);
        mListView.setOnItemClickListener(this);
        mListView.setFastScrollEnabled(true);
        mListView.setFastScrollAlwaysVisible(true);

        mLoadingView = (LinearLayout) view.findViewById(R.id.loading);

        final LinearLayout emptyView = (LinearLayout) view.findViewById(R.id.nofiles);
        mListView.setEmptyView(emptyView);

        return view;
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // remove unused items
        menu.removeItem(R.id.menu_action_add);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.menu_action_apply: {
                makeApplyDialog();
                return true;
            }
            case R.id.menu_action_toggle: {
                mLoadFull = !mLoadFull;
                loadAllTheStuff();
                return true;
            }
        }

        return false;
    }

    @Override public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long row) {
        final Prop p = mAdapter.getItem(position);
        if (p != null) {
            editPropDialog(p);
        }
    }

    // TODO: animate?
    private void loadAllTheStuff() {
        mLoadingView.setVisibility(View.VISIBLE);

        final StringBuilder sb = new StringBuilder();
        final String dn = App.get().getFilesDirectory();

        if (new File("/system/etc/sysctl.conf").exists()) {
            sb.append(Scripts.copyFile("/system/etc/sysctl.conf", dn + "/sysctl.conf"));
        } else {
            sb.append("echo \"# created by Device Control\n\" > ").append(dn).append("/sysctl.conf;\n");
        }

        if (mLoadFull) {
            String findCmd = BusyBox.callBusyBoxApplet("find", "/proc/sys/* -type f -perm -644");
            findCmd = TextUtils.isEmpty(findCmd) ? "" : findCmd;
            String grepCmd = BusyBox.callBusyBoxApplet("grep", "-v \"vm.\"");
            grepCmd = TextUtils.isEmpty(grepCmd) ? "" : grepCmd;
            final String cmd = String.format("echo `%s | %s`;\n", findCmd, grepCmd);
            sb.append(cmd);
        } else {
            String findCmd = BusyBox.callBusyBoxApplet("find", "/proc/sys/vm/* -type f -prune -perm -644");
            findCmd = TextUtils.isEmpty(findCmd) ? "" : findCmd;
            final String cmd = String.format("echo `%s`;\n", findCmd);
            sb.append(cmd);
        }

        Utils.getCommandResult(SysctlEditorFragment.this, -1, sb.toString());
    }

    private void makeApplyDialog() {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        new AlertDialog.Builder(activity)
                .setTitle(getString(R.string.dialog_warning))
                .setMessage(getString(R.string.dialog_warning_apply))
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }
                )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialogInterface, int i) {
                                Utils.remount("/system", "rw");
                                Utils.getCommandResult(SysctlEditorFragment.this, APPLY,
                                        Scripts.copyFile(App.get().getFilesDirectory() + "/sysctl.conf", Scripts.SYSCTL));
                                dialogInterface.dismiss();
                                Toast.makeText(activity, getString(R.string.toast_settings_applied), Toast.LENGTH_SHORT).show();
                            }
                        }
                ).show();
    }

    @Override public void onShellOutput(final ShellOutput shellOutput) {
        switch (shellOutput.id) {
            case SAVE:
                Utils.remount("/system", "ro"); // slip through to APPLY
            case APPLY:
                RootShell.fireAndForget("chmod 644 /system/etc/sysctl.conf;sysctl -p /system/etc/sysctl.conf;");
                break;
            default:
                Timber.v("onReadPropsCompleted: %s", shellOutput.output);
                if (isAdded()) {
                    loadProp(shellOutput.output);
                } else {
                    Timber.w("Not attached!");
                }
                break;
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    private void loadProp(final String result) {
        final Activity activity = getActivity();
        if ((activity != null) && (result != null) && (!result.isEmpty())) {
            mProps.clear();
            final String[] p = result.split(" ");
            for (String aP : p) {
                if (aP != null && !aP.isEmpty()) {
                    aP = aP.trim();
                    final int length = aP.length();
                    if (length > 0) {
                        String pv = Utils.readOneLine(aP);
                        if (pv != null && !pv.isEmpty()) {
                            pv = pv.trim();
                        }
                        String pn = aP.replace("/", ".");
                        if (pn.length() > 10) {
                            pn = pn.substring(10, length);
                        }
                        mProps.add(new Prop(pn, pv));
                    }
                }
            }
            Collections.sort(mProps);
            mLoadingView.setVisibility(View.GONE);

            mAdapter = new PropAdapter(activity, mProps);
            mListView.setAdapter(mAdapter);
        }
    }

    //==============================================================================================
    // Dialogs
    //==============================================================================================

    private void editPropDialog(final Prop p) {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        final String dn = App.get().getFilesDirectory();
        String title;

        final View editDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_prop, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.prop_value);
        final TextView tn = (TextView) editDialog.findViewById(R.id.prop_name_tv);

        if (p != null) {
            tv.setText(p.getVal());
            tn.setText(p.getName());
            title = getString(R.string.edit_property);
        } else {
            title = getString(R.string.add_property);
        }

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editDialog)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override public void onClick(final DialogInterface dialog, final int which) { }
                        }
                )
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override public void onClick(final DialogInterface dialog, final int which) {
                        if (p != null) {
                            if (tv.getText() != null) {
                                final String name = p.getName();
                                final String value = tv.getText().toString().trim();
                                p.setVal(value);
                                Utils.getCommandResult(SysctlEditorFragment.this, SAVE,
                                        Scripts.addOrUpdate(name, value, dn + "/sysctl.conf"));
                            }
                        } else {
                            if (tv.getText() != null && tn.getText() != null) {
                                final String name = tn.getText().toString().trim();
                                final String value = tv.getText().toString().trim();
                                if (name.length() > 0) {
                                    mProps.add(new Prop(name, value));
                                    Utils.getCommandResult(SysctlEditorFragment.this, SAVE,
                                            Scripts.addOrUpdate(name, value, dn + "/sysctl.conf"));
                                }
                            }
                        }
                        Collections.sort(mProps);
                        mAdapter.notifyDataSetChanged();
                    }
                }).show();
    }

}
