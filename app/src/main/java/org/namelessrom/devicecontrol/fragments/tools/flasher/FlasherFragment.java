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
package org.namelessrom.devicecontrol.fragments.tools.flasher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.FilePickerActivity;
import org.namelessrom.devicecontrol.events.RefreshEvent;
import org.namelessrom.devicecontrol.fragments.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.FlashUtils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachFragment;
import org.namelessrom.devicecontrol.adapters.FlashListAdapter;

import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Created by alex on 22.06.14.
 */
public class FlasherFragment extends AttachFragment implements DeviceConstants,
        View.OnClickListener {

    private static final int    REQUEST_CODE_FILE = 100;
    public static final  String EXTRA_FLASHITEM   = "extra_flashitem";

    private FlashListAdapter mAdapter;

    private LinearLayout mContainer;
    private ListView     mFlashList;

    private TextView mEmptyView;

    @Override
    public void onAttach(final Activity activity) { super.onAttach(activity, ID_TOOLS_FLASHER); }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View v = inflater.inflate(R.layout.fragment_flasher, container, false);

        mContainer = findById(v, R.id.container);
        mFlashList = findById(v, android.R.id.list);
        mEmptyView = findById(v, android.R.id.empty);

        final Button mCancel = findById(v, R.id.btn_cancel);
        mCancel.setOnClickListener(this);
        final Button mApply = findById(v, R.id.btn_apply);
        mApply.setOnClickListener(this);

        mAdapter = new FlashListAdapter();

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFlashList.setAdapter(mAdapter);

        checkAdapter();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_flasher, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            case R.id.action_task_add: {
                final Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerFragment.ARG_FILE_TYPE, "zip");
                startActivityForResult(i, REQUEST_CODE_FILE);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void checkAdapter() {
        if (mAdapter != null && mContainer != null && mEmptyView != null) {
            if (mAdapter.getCount() != 0) {
                mContainer.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
            } else {
                mContainer.setVisibility(View.INVISIBLE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe public void onRefreshEvent(final RefreshEvent event) {
        if (event == null) return;
        checkAdapter();
    }

    @Override public void onActivityResult(final int req, final int res, final Intent data) {
        logDebug("onActivityResult(%s, %s, %s)", req, res, data);
        if (req == REQUEST_CODE_FILE && res == Activity.RESULT_OK) {
            final String name = data.getStringExtra("name");
            final String path = data.getStringExtra("path");
            final FlashItem item = new FlashItem(name, path);

            logDebug(String.format("onActivityResult(%s)", item.getPath()));
            final List<FlashItem> flashItemList = new ArrayList<FlashItem>();
            flashItemList.addAll(((FlashListAdapter) mFlashList.getAdapter()).getFlashItemList());
            flashItemList.add(item);
            mAdapter = new FlashListAdapter(flashItemList);
            mFlashList.setAdapter(mAdapter);
            checkAdapter();
        } else {
            super.onActivityResult(req, res, data);
        }
    }

    @Override public void onClick(final View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.btn_apply: {
                final Activity activity = getActivity();
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.dialog_warning)
                        .setMessage(R.string.flash_warning)
                        .setPositiveButton(android.R.string.ok, new DialogInterface
                                .OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                // start the progress dialog
                                final ProgressDialog pd = new ProgressDialog(activity);
                                pd.setMessage(Application.getStr(R.string.applying_wait));
                                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                pd.setCancelable(false);
                                pd.setIndeterminate(true);
                                pd.show();

                                // finally flash it
                                final List<FlashItem> flashItemList = mAdapter.getFlashItemList();
                                final List<String> fileList =
                                        new ArrayList<String>(flashItemList.size());
                                for (final FlashItem item : flashItemList) {
                                    fileList.add(item.getPath());
                                }
                                FlashUtils.triggerFlash(fileList);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface
                                .OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                break;
            }
            case R.id.btn_cancel: {
                // TODO: allow to revert action?
                mAdapter = new FlashListAdapter();
                mFlashList.setAdapter(mAdapter);
                checkAdapter();
                break;
            }
        }
    }
}
