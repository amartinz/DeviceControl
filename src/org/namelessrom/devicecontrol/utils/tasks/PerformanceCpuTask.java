/*
 * Copyright (C) 2013 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.namelessrom.devicecontrol.utils.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.performance.PerformanceCpuFragment;
import org.namelessrom.devicecontrol.utils.DeviceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 22.12.13.
 */
public class PerformanceCpuTask extends AsyncTask<Void, Integer, List<Boolean>>
        implements DeviceConstants {

    private final Context mContext;
    private final PerformanceCpuFragment mFragment;
    private ProgressDialog mDialog;

    public PerformanceCpuTask(PerformanceCpuFragment paramFragment) {
        mFragment = paramFragment;
        mContext = mFragment.getActivity();
    }

    @Override
    protected void onPreExecute() {
        mDialog = new ProgressDialog(mContext);
        mDialog.setTitle("");
        mDialog.setMessage(mContext.getString(R.string.dialog_getting_information, ""));
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected List<Boolean> doInBackground(Void... voids) {
        List<Boolean> tmpList = new ArrayList<Boolean>();

        publishProgress(0);

        return tmpList;
    }

    @Override
    protected void onProgressUpdate(Integer... integers) {
        switch (integers[0]) {
            default:
            case 0:
                mDialog.setMessage(mContext.getString(
                        R.string.dialog_getting_information, "Hotplugger"));
                break;
        }
    }

    @Override
    protected void onPostExecute(List<Boolean> booleans) {
        mDialog.dismiss();
        mFragment.setResult(booleans);
    }
}
