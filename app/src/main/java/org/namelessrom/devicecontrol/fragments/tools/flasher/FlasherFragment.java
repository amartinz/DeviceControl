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
import android.widget.TextView;

import com.negusoft.holoaccent.dialog.AccentAlertDialog;
import com.negusoft.holoaccent.dialog.DividerPainter;
import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.FilePickerActivity;
import org.namelessrom.devicecontrol.adapters.FlashListAdapter;
import org.namelessrom.devicecontrol.cards.FlasherCard;
import org.namelessrom.devicecontrol.events.RefreshEvent;
import org.namelessrom.devicecontrol.fragments.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.FlashUtils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.views.AttachFragment;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.view.CardListView;

import static butterknife.ButterKnife.findById;

public class FlasherFragment extends AttachFragment implements DeviceConstants,
        View.OnClickListener {

    private static final int    REQUEST_CODE_FILE = 100;
    public static final  String EXTRA_FLASHITEM   = "extra_flashitem";

    private FlashListAdapter mAdapter;

    private LinearLayout mContainer;
    private CardListView mFlashList;

    private TextView mEmptyView;

    @Override protected int getFragmentId() { return ID_TOOLS_FLASHER; }

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

        mAdapter = new FlashListAdapter(getActivity());

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
            case R.id.action_task_add: {
                final Intent i = new Intent(getActivity(), FilePickerActivity.class);
                i.putExtra(FilePickerFragment.ARG_FILE_TYPE, "zip");
                startActivityForResult(i, REQUEST_CODE_FILE);
                return true;
            }
        }
        return false;
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
        Logger.v(this, String.format("onActivityResult(%s, %s, %s)", req, res, data));
        if (req == REQUEST_CODE_FILE && res == Activity.RESULT_OK) {
            final String name = data.getStringExtra("name");
            final String path = data.getStringExtra("path");
            final FlashItem item = new FlashItem(name, path);

            Logger.v(this, String.format("onActivityResult(%s)", item.getPath()));

            mAdapter.add(new FlasherCard(getActivity(), item));
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
                new AccentAlertDialog.Builder(activity)
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
                                new DividerPainter(getActivity()).paint(pd.getWindow());

                                // finally flash it
                                final int count = mAdapter.getCount();
                                final ArrayList<String> fileList = new ArrayList<String>(count);
                                Card card;
                                for (int i = 0; i < count; i++) {
                                    card = mAdapter.getItem(i);
                                    if (card != null && card instanceof FlasherCard) {
                                        fileList.add(((FlasherCard) card).getItem().getPath());
                                    }
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
                mAdapter.clear();
                checkAdapter();
                break;
            }
        }
    }
}
