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
package org.namelessrom.devicecontrol.modules.flasher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.RequestFileActivity;
import org.namelessrom.devicecontrol.theme.AppResources;
import org.namelessrom.devicecontrol.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.IOUtils;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

public class FlasherFragment extends AttachFragment implements RequestFileActivity.RequestFileCallback {
    private RecyclerView mRecyclerView;
    private FlashCard mFlashCard;

    public ArrayList<File> mFiles = new ArrayList<>();

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_FLASHER; }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        final View v = inflater.inflate(R.layout.fragment_flasher, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(android.R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        final FlasherAdapter adapter = new FlasherAdapter(this, mFiles);
        mRecyclerView.setAdapter(adapter);

        final FloatingActionButton fabAdd = (FloatingActionButton) v.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                final Activity activity = getActivity();
                if (activity != null) {
                    Intent intent = new Intent(activity, RequestFileActivity.class);
                    activity.startActivity(intent);
                }
            }
        });

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final LinearLayout container = (LinearLayout) view.findViewById(R.id.file_container);
        // add flasher card
        mFlashCard = new FlashCard(getActivity(), this);
        container.addView(mFlashCard, 0);
        // add option card
        container.addView(new FlashOptionCard(getActivity()), 0);

        RequestFileActivity.setRequestFileCallback(this);
    }

    private void addFile(final File file) {
        if (mFiles.indexOf(file) != -1) {
            // file already added, return
            return;
        }
        mFiles.add(file);
        mRecyclerView.setAdapter(new FlasherAdapter(this, mFiles));
        mFlashCard.install.setEnabled(true);
        mFlashCard.install.setTextColor(AppResources.get().getAccentColor());
    }

    @Override public void fileRequested(String filePath) {
        Timber.v("fileRequested --> %s", filePath);
        if (TextUtils.isEmpty(filePath)) {
            Snackbar.make(mRecyclerView, R.string.file_not_found, Snackbar.LENGTH_SHORT);
        } else {
            if (filePath.startsWith("null")) {
                filePath = filePath.replaceFirst("null", IOUtils.get().getPrimarySdCard());
                Timber.v("filePath started with null! modified -> %s", filePath);
            }
            addFile(new File(filePath));
        }
    }

    public void showRemoveDialog(final File file) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(R.string.remove_file_title);
        final String message = getString(R.string.remove_file_summary, file.getName());
        alert.setMessage(message);
        alert.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                mFiles.remove(file);
                mRecyclerView.setAdapter(new FlasherAdapter(FlasherFragment.this, mFiles));
                final boolean isEnabled = mFiles.size() > 0;
                mFlashCard.install.setEnabled(isEnabled);
                mFlashCard.install.setTextColor(isEnabled
                        ? AppResources.get().getAccentColor()
                        : getActivity().getResources().getColor(android.R.color.darker_gray));
            }
        });
        alert.show();
    }
}
