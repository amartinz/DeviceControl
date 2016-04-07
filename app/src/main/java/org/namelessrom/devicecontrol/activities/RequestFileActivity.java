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

package org.namelessrom.devicecontrol.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.IOUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.Serializable;

import timber.log.Timber;

public class RequestFileActivity extends Activity {

    private static final String ROOT_ID_PRIMARY_EMULATED = "primary";
    private static final int REQUEST_PICK_FILE = 203;
    private static final int REQUEST_PICK_FILE_TWO = 204;

    public interface RequestFileCallback extends Serializable {
        void fileRequested(String filePath);
    }

    private static RequestFileCallback sCallback;

    public static void setRequestFileCallback(RequestFileCallback callback) {
        sCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final PackageManager pm = getPackageManager();
        if (pm == null) {
            launchInternalPicker();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/zip");

        try {
            if (pm.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES).size() > 0) {
                intent = new Intent();
                intent.setType("application/zip");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_PICK_FILE);
            } else {
                throw new ActivityNotFoundException();
            }
        } catch (ActivityNotFoundException e) {
            Timber.e(e, "No activity found to handle file picking! Falling back to default!");
            launchInternalPicker();
        }
    }

    private void launchInternalPicker() {
        final Intent intent = new Intent();
        final Intent i = new Intent(this, FilePickerActivity.class);
        i.setType("application/zip");
        try {
            startActivityForResult(intent, REQUEST_PICK_FILE_TWO);
        } catch (ActivityNotFoundException e) {
            Timber.wtf(e, "Could not start default activity to pick files");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && (requestCode == REQUEST_PICK_FILE || requestCode == REQUEST_PICK_FILE_TWO)) {

            if (data == null) {
                Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
            } else {
                try {
                    // some file pickers like AndroZip allow to pick the file but we error out when
                    // trying to read the provider as it is not exported
                    handleActivityResult(data, requestCode);
                } catch (SecurityException se) {
                    Timber.e(se, "could not handle activity result");
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_LONG).show();
                }
            }
        }

        finish();
    }

    private void handleActivityResult(@NonNull Intent data, int reqCode) throws SecurityException {
        final Uri uri;
        String filePath = null;
        if (reqCode == REQUEST_PICK_FILE) {
            uri = data.getData();
            if (uri != null) {
                filePath = uri.getPath();
            }
        } else {
            uri = null;
            filePath = data.getStringExtra("path");
        }

        Timber.i("uri: %s, filepath: %s", uri, filePath);

        if (!Utils.fileExists(filePath) && uri != null) {
            final ContentResolver cr = getContentResolver();
            Cursor cursor = null;
            try {
                cursor = cr.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    if (index >= 0) {
                        filePath = cursor.getString(index);
                    } else if (Build.VERSION.SDK_INT >= 19
                            && uri.toString().startsWith(ContentResolver.SCHEME_CONTENT)) {
                        String newUri = new Uri.Builder()
                                .scheme(ContentResolver.SCHEME_CONTENT)
                                .authority(uri.getAuthority())
                                .appendPath("document")
                                .build().toString();
                        String path = uri.toString();
                        index = filePath.indexOf(":");
                        if (path.startsWith(newUri) && index >= 0) {
                            String firstPath = filePath.substring(0, index);
                            filePath = filePath.substring(index + 1);
                            String storage = IOUtils.get().getPrimarySdCard();
                            if (!firstPath.contains(ROOT_ID_PRIMARY_EMULATED)) {
                                storage = IOUtils.get().getSecondarySdCard();
                            }
                            filePath = storage + "/" + filePath;
                        } else {
                            filePath = null;
                        }

                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

        if (sCallback != null) {
            sCallback.fileRequested(filePath);
        }
    }
}
