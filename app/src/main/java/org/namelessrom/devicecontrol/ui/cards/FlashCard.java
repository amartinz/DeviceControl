/*
 * Copyright 2014 ParanoidAndroid Project
 * Modifications Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.namelessrom.devicecontrol.ui.cards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.RequestFileActivity;
import org.namelessrom.devicecontrol.utils.IOUtils;
import org.namelessrom.devicecontrol.utils.RebootHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlashCard extends Card implements RequestFileActivity.RequestFileCallback {

    private static final String FILES = "FILES";

    private RebootHelper mRebootHelper;
    private List<File> mFiles = new ArrayList<>();
    private LinearLayout mLayout;
    private Item         mAdd;
    private Item         mInstall;
    private CheckBox     mBackup;
    private CheckBox     mWipeData;
    private CheckBox     mWipeCaches;
    private View         mAdditional;

    public FlashCard(Context context, AttributeSet attrs, RebootHelper rebootHelper,
            Bundle savedInstanceState) {
        super(context, attrs, savedInstanceState);

        setTitle(R.string.install_title);
        setLayoutId(R.layout.card_install);

        mRebootHelper = rebootHelper;

        mLayout = (LinearLayout) findLayoutViewById(R.id.layout);
        mAdd = (Item) findLayoutViewById(R.id.add);
        mInstall = (Item) findLayoutViewById(R.id.install);
        mBackup = (CheckBox) findLayoutViewById(R.id.backup);
        mWipeData = (CheckBox) findLayoutViewById(R.id.wipedata);
        mWipeCaches = (CheckBox) findLayoutViewById(R.id.wipecaches);
        mAdditional = findLayoutViewById(R.id.additional);

        findLayoutViewById(R.id.card_border_one)
                .setBackgroundColor(Application.get().getAccentColor());
        findLayoutViewById(R.id.card_border_two)
                .setBackgroundColor(Application.get().getAccentColor());
        findLayoutViewById(R.id.card_border_three)
                .setBackgroundColor(Application.get().getAccentColor());

        mInstall.setEnabled(false);

        mAdd.setOnItemClickListener(new Item.OnItemClickListener() {
            @Override public void onClick(int id) {
                Context context = getContext();
                Intent intent = new Intent(context, RequestFileActivity.class);
                context.startActivity(intent);
            }

        });

        mInstall.setOnItemClickListener(new Item.OnItemClickListener() {

            @Override
            public void onClick(int id) {
                String[] items = new String[mFiles.size()];
                for (int i = 0; i < mFiles.size(); i++) {
                    File file = mFiles.get(i);
                    items[i] = file.getAbsolutePath();
                }
                mRebootHelper.showRebootDialog(getContext(), items, mBackup.isChecked(),
                        mWipeData.isChecked(), mWipeCaches.isChecked());
            }

        });

        RequestFileActivity.setRequestFileCallback(this);

        if (savedInstanceState != null) {
            final List<File> files = (List<File>) savedInstanceState.getSerializable(FILES);
            if (files != null) {
                for (final File file : files) {
                    addFile(file);
                }
            }
        }

        if (isExpanded()) {
            mAdditional.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void expand() {
        super.expand();
        if (mAdditional != null) {
            mAdditional.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void collapse() {
        super.collapse();
        if (mAdditional != null) {
            mAdditional.setVisibility(View.GONE);
        }
    }

    @Override
    public void saveState(Bundle outState) {
        super.saveState(outState);
        outState.putSerializable(FILES, (Serializable) mFiles);
    }

    @Override
    public void fileRequested(String filePath) {
        Logger.v(this, "fileRequested --> %s", filePath);
        if (TextUtils.isEmpty(filePath)) {
            Utils.showToast(getContext(), R.string.file_not_found);
        } else {
            if (filePath.startsWith("null")) {
                filePath = filePath.replaceFirst("null", IOUtils.get().getPrimarySdCard());
                Logger.v(this, "filePath started with null! modified -> %s", filePath);
            }
            addFile(new File(filePath));
        }
    }

    private void addFile(final File file) {
        mFiles.add(file);

        final Item item = new Item(getContext(), null);
        item.setTitle(file.getName());
        item.setOnItemClickListener(new Item.OnItemClickListener() {
            @Override public void onClick(int id) {
                showRemoveDialog(item, file);
            }

        });
        mLayout.addView(item);
        mInstall.setEnabled(true);
    }

    private void showRemoveDialog(final Item item, final File file) {
        Context context = getContext();
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.remove_file_title);
        String message = context.getResources().getString(R.string.remove_file_summary,
                file.getName());
        alert.setMessage(message);
        alert.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();

                mFiles.remove(file);
                mLayout.removeView(item);
                mInstall.setEnabled(mFiles.size() > 0);
            }
        });
        alert.show();
    }

}
