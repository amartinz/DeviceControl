/*
 * Copyright (C) 2013 h0rn3t
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.devicecontrol.fragments.tools.sub.freezer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;
import com.stericson.roottools.execution.Shell;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.FreezeEvent;
import org.namelessrom.devicecontrol.events.ReplaceFragmentEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.widgets.AttachFragment;
import org.namelessrom.devicecontrol.widgets.adapters.PackAdapter;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class FreezerFragment extends AttachFragment
        implements DeviceConstants, AdapterView.OnItemClickListener {

    private static final String ARG_FREEZER = "arg_freezer";
    private static final String ARG_TYPE    = "arg_type";
    private LinearLayout linlaHeaderProgress;
    private LinearLayout linNopack, llist;
    private PackageManager packageManager;
    private ListView       packList;
    private PackAdapter    adapter;
    private int            curpos;
    private boolean        mFreeze;
    private String         pn;
    private String         titlu;
    private View           mShadowTop, mShadowBottom;

    public static FreezerFragment newInstance(final int freezer, final String type) {
        final Bundle b = new Bundle();
        b.putBoolean(FreezerFragment.ARG_FREEZER, (freezer == 0));
        b.putString(FreezerFragment.ARG_TYPE, type);
        FreezerFragment fragment = new FreezerFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getBus().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        final View v = layoutInflater.inflate(R.layout.tools_freezer_list, viewGroup, false);

        final Activity activity = getActivity();

        mFreeze = getArguments().getBoolean(FreezerFragment.ARG_FREEZER, false);
        final boolean sys = getArguments().getString(FreezerFragment.ARG_TYPE, "usr").equals("sys");

        packageManager = activity.getPackageManager();

        linlaHeaderProgress = (LinearLayout) v.findViewById(R.id.linlaHeaderProgress);
        linNopack = (LinearLayout) v.findViewById(R.id.noproc);

        llist = (LinearLayout) v.findViewById(R.id.llist);
        final Switch mSwitch = (Switch) v.findViewById(R.id.tools_freezer_toggle);
        mSwitch.setChecked(sys);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Fragment f = FreezerFragment.newInstance(mFreeze ? 0 : 1, b ? "sys" : "usr");
                BusProvider.getBus().post(new ReplaceFragmentEvent(f, true));
            }
        });

        mShadowTop = v.findViewById(R.id.tools_freezer_shadow_top);
        mShadowBottom = v.findViewById(R.id.tools_freezer_shadow_bottom);

        packList = (ListView) v.findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        if (mFreeze) {
            titlu = getString(R.string.freeze);
        } else {
            titlu = getString(R.string.defrost);
        }
        getFreezer(mFreeze, sys);

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long row) {
        pn = (String) parent.getItemAtPosition(position);
        curpos = position;
        if (mFreeze) {
            makedialog(titlu, getString(R.string.freeze_msg, pn));
        } else {
            makedialog(titlu, getString(R.string.defrost_msg, pn));
        }

    }

    @Subscribe
    public void onFreezer(final FreezeEvent event) {
        if (event == null) return;

        final Activity activity = getActivity();
        if (activity == null) return;

        final String result = event.getPackages();
        String[] pmList = null;
        if (result != null) {
            pmList = result.split(" ");
        }
        linlaHeaderProgress.setVisibility(View.GONE);
        if (pmList != null && pmList.length > 0) {
            adapter = new PackAdapter(activity, pmList, packageManager);
            packList.setAdapter(adapter);
            linNopack.setVisibility(View.GONE);
            llist.setVisibility(LinearLayout.VISIBLE);
            mShadowTop.setVisibility(View.VISIBLE);
            mShadowBottom.setVisibility(View.VISIBLE);
        } else {
            linNopack.setVisibility(View.VISIBLE);
        }
    }

    private void getFreezer(final boolean isFreeze, final boolean isSys) {
        final Activity activity = getActivity();
        if (activity == null) { return; }

        linlaHeaderProgress.setVisibility(View.VISIBLE);
        linNopack.setVisibility(View.GONE);
        llist.setVisibility(LinearLayout.GONE);

        final StringBuilder sb = new StringBuilder();
        sb.append("busybox echo `pm list packages ");
        if (isSys) {
            sb.append("-s ");
        } else {
            sb.append("-3 ");
        }
        if (isFreeze) {
            sb.append("-e ");
        } else {
            sb.append("-d ");
        }
        sb.append("| cut -d':' -f2`");

        final String cmd = sb.toString();
        logDebug(cmd);

        try {
            final Shell mShell = RootTools.getShell(true);

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture commandCapture = new CommandCapture(0, false, cmd) {
                @Override
                public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    logDebug(line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    final String result = outputCollector.toString();
                    logDebug(result);
                    Application.HANDLER.post(new Runnable() {
                        @Override
                        public void run() {
                            BusProvider.getBus().post(new FreezeEvent(result));
                        }
                    });
                }
            };

            if (mShell == null || mShell.isClosed()) {
                throw new Exception("Shell not available");
            }
            mShell.add(commandCapture);
        } catch (Exception exc) { /* TODO: throw error? */ }
    }

    private void makedialog(String titlu, String msg) {
        final Activity activity = getActivity();
        if (activity == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(titlu)
                .setMessage(msg)
                .setNegativeButton(getString(android.R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                )
                .setPositiveButton(getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        }
                );
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (theButton != null) {
            theButton.setOnClickListener(new FreezeListener(alertDialog));
        }
    }

    class FreezeListener implements View.OnClickListener {
        private final Dialog mDialog;

        public FreezeListener(final Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public void onClick(final View v) {
            mDialog.cancel();
            doFreeze();
        }
    }

    private void doFreeze() {
        final Activity activity = getActivity();

        String cmd;
        if (mFreeze) {
            cmd = "pm disable " + pn + " 2> /dev/null";
        } else {
            cmd = "pm enable " + pn + " 2> /dev/null";
        }

        try {
            final Shell mShell = RootTools.getShell(true);

            final StringBuilder outputCollector = new StringBuilder();
            final CommandCapture commandCapture = new CommandCapture(0, false, cmd) {
                @Override
                public void commandOutput(int id, String line) {
                    outputCollector.append(line);
                    logDebug(line);
                }

                @Override
                public void commandCompleted(int id, int exitcode) {
                    final String result = outputCollector.toString();
                    logDebug("Result: " + result);

                    // here we can just ignore if the activity is null,
                    // as the package is disabled and next time we launch the editor,
                    // it is already frozen
                    if (activity != null) {
                        Application.HANDLER.post(new Runnable() {
                            @Override
                            public void run() {
                                BusProvider.getBus().post(new ShellOutputEvent(-1, result, null));
                            }
                        });
                    }
                }
            };

            if (mShell == null || mShell.isClosed()) {
                throw new Exception("Shell not available");
            }
            mShell.add(commandCapture);
        } catch (Exception ignored) { }

    }

    @Subscribe
    public void onFreezeComplete(final ShellOutputEvent event) {
        if (event != null) {
            logDebug("onFreezeComplete");
        }
        adapter.delItem(curpos);
        adapter.notifyDataSetChanged();
        if (adapter.isEmpty()) {
            llist.setVisibility(LinearLayout.GONE);
            linNopack.setVisibility(View.VISIBLE);
        }
        final Activity activity = getActivity();
        if (activity != null) {
            final int msgId = (mFreeze ? R.string.package_frozen : R.string.package_defrost);
            Toast.makeText(activity, msgId, Toast.LENGTH_SHORT).show();
        }
    }
}
