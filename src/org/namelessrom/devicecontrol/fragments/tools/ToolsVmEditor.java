/*
 *  Copyright (C) 2013 h0rn3t
 *  Modifications Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.tools;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.adapters.PropAdapter;
import org.namelessrom.devicecontrol.utils.classes.Prop;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ToolsVmEditor extends Fragment
        implements DeviceConstants, FileConstants, AdapterView.OnItemClickListener {

    private ListView packList;
    private LinearLayout linlaHeaderProgress;
    private LinearLayout nofiles;
    private RelativeLayout tools;
    private PropAdapter adapter = null;
    private EditText filterText = null;
    private List<Prop> props = new ArrayList<Prop>();
    private final String dn = DC_BACKUP_DIR;

    private final String mod = "vm";
    private final String syspath = "/system/etc/";
    private final String sob = VM_SOB;
    private Boolean isdyn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.prop_view, container, false);

        packList = (ListView) view.findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        nofiles = (LinearLayout) view.findViewById(R.id.nofiles);
        tools = (RelativeLayout) view.findViewById(R.id.tools);
        filterText = (EditText) view.findViewById(R.id.filtru);
        filterText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.getFilter().filter(filterText.getText().toString());
            }
        });
        Button applyBtn = (Button) view.findViewById(R.id.applyBtn);
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Shell.SU.run("busybox mount -o remount,rw /system" + ";" +
                        "busybox cp " + dn + "/" + mod + ".conf" +
                        " " + syspath + mod + ".conf;" +
                        "busybox chmod 644 " + syspath + mod + ".conf" + ";" +
                        "busybox mount -o remount,ro /system" + ";" +
                        "busybox sysctl -p " + syspath + mod + ".conf" + ";");
            }
        });
        final Switch setOnBoot = (Switch) view.findViewById(R.id.applyAtBoot);
        setOnBoot.setChecked(PreferenceHelper.getBoolean(sob, false));
        setOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PreferenceHelper.setBoolean(sob, isChecked);
            }
        });
        tools.setVisibility(View.GONE);
        isdyn = (new File(DYNAMIC_DIRTY_WRITEBACK_PATH).exists());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new GetPropOperation().execute();
            }
        }, 250);

        return view;
    }

    private class GetPropOperation extends AsyncTask<String, Void, List<String>> {
        @Override
        protected List<String> doInBackground(String... params) {

            StringBuilder sb = new StringBuilder();
            sb.append("busybox mkdir -p ").append(dn).append(";\n");

            if (new File(syspath + mod + ".conf").exists()) {
                sb.append("busybox cp " + syspath + mod + ".conf" + " ")
                        .append(dn).append("/").append(mod).append(".conf;\n");
            } else {
                sb.append("busybox echo \"# created by DeviceControl\n\" > ")
                        .append(dn).append("/").append(mod).append(".conf;\n");
            }

            sb.append("busybox echo `busybox find /proc/sys/vm/* -type f -prune -perm -644`;\n");

            List<String> mResult = Shell.SU.run(sb.toString());
            if (mResult != null) {
                return mResult;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if ((result != null) && (result.size() > 0)) {
                load_prop(result);
                Collections.sort(props);
                linlaHeaderProgress.setVisibility(View.GONE);
                if (props.isEmpty()) {
                    nofiles.setVisibility(View.VISIBLE);
                } else {
                    nofiles.setVisibility(View.GONE);
                    tools.setVisibility(View.VISIBLE);
                    adapter = new PropAdapter(getActivity(), props);
                    packList.setAdapter(adapter);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
            nofiles.setVisibility(View.GONE);
            tools.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long row) {
        final Prop p = adapter.getItem(position);
        editPropDialog(p);
    }

    private void editPropDialog(Prop p) {
        final Prop pp = p;
        String title;

        LayoutInflater factory = LayoutInflater.from(getActivity());
        final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
        final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
        final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);

        if (pp != null) {
            tv.setText(pp.getVal());
            tn.setText(pp.getName());
            title = getString(R.string.etc_edit_prop_title);
        } else {
            title = getString(R.string.etc_add_prop_title);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(editDialog)
                .setNegativeButton(getString(R.string.etc_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                .setPositiveButton(
                        getString(R.string.etc_save), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (pp != null) {
                            if (tv.getText().toString() != null) {
                                pp.setVal(tv.getText().toString().trim());
                                String cmd = getActivity().getFilesDir() + "/utils -setprop \"" +
                                        pp.getName() + "=" + pp.getVal() + "\" " + dn + "/" +
                                        mod + ".conf";
                                Shell.SU.run(cmd);
                                Log.e("BUILD", "cmd: " + cmd);
                            }
                        } else {
                            if (tv.getText().toString() != null &&
                                    tn.getText().toString() != null &&
                                    tn.getText().toString().trim().length() > 0) {
                                props.add(new Prop(tn.getText().toString().trim(),
                                        tv.getText().toString().trim()));
                                Shell.SU.run(getActivity().getFilesDir() + "/utils -setprop \"" +
                                        tn.getText().toString().trim() + "=" +
                                        tv.getText().toString().trim() + "\" " + dn + "/" +
                                        mod + ".conf");
                            }
                        }
                        Collections.sort(props);
                        adapter.notifyDataSetChanged();
                    }
                }).create().show();
    }

    void load_prop(List<String> paramResult) {
        props.clear();
        for (String s : paramResult) {
            String p[] = s.split(" ");
            for (String aP : p) {
                if (aP.trim().length() > 0 && aP != null) {
                    final String pv = Utils.readOneLine(aP).trim();
                    final String pn = aP.trim().replace("/", ".").substring(10, aP.length());
                    if (testprop(pn)) {
                        props.add(new Prop(pn, pv));
                    }
                }
            }
        }
    }

    boolean testprop(String s) {
        return mod.equals("sysctl") ||
                !isdyn ||
                !(s.contains("dirty_writeback_active_centisecs") ||
                        s.contains("dynamic_dirty_writeback") ||
                        s.contains("dirty_writeback_suspend_centisecs"));
    }
}
