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
package org.namelessrom.devicecontrol.log;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.views.AttachFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static butterknife.ButterKnife.findById;

public class LogCollectorFragment extends AttachFragment {

    private static final File TMP = new File(Application.get().getCacheDir(), "logger");

    private Spinner mLogKernel;
    private Spinner mLogCompression;
    private Button  mLogCollect;

    private int mUid = -1;

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_LOG_COLLECTOR; }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tools_log_collector, container, false);

        mLogKernel = findById(v, R.id.log_kernel);
        mLogCompression = findById(v, R.id.log_compression);

        mLogCollect = findById(v, R.id.log_collect);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // setup kernel adapter
        final ArrayAdapter<String> kernelAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item);
        kernelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        String[] entries = Application.get().getStringArray(R.array.log_kernel_entries);
        kernelAdapter.addAll(entries);

        mLogKernel.setAdapter(kernelAdapter);
        mLogKernel.setSelection(0);

        // setup compression adapter
        final ArrayAdapter<String> compressionAdapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item);
        compressionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        entries = Application.get().getStringArray(R.array.log_compression_entries);
        compressionAdapter.addAll(entries);

        mLogCompression.setAdapter(compressionAdapter);
        mLogCompression.setSelection(0);

        // setup start button
        mLogCollect.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(final View view) {
                final LogConfiguration configuration = new LogConfiguration(
                        mLogKernel.getSelectedItemPosition(),
                        mLogCompression.getSelectedItemPosition());
                setupCollecting(configuration);
                mLogCollect.setEnabled(false);
            }
        });
    }

    private void setupCollecting(final LogConfiguration configuration) {
        // save our uid
        mUid = AppHelper.getUid();
        Logger.v(this, String.format("UID: %s", mUid));

        // create a list, where we will store our commands
        final ArrayList<String> commandList = new ArrayList<String>();
        // and another list, where we will store our status messages
        final ArrayList<String> statusList = new ArrayList<String>();

        // first, lets create our tmp directory if it does not exist
        commandList.add(String.format("mkdir -p %s;", TMP.getAbsolutePath()));
        statusList.add(getString(R.string.log_creating_working_folder));

        // lets dump the logcat
        commandList
                .add(String.format("logcat -d > %s/logcat.txt;", TMP.getAbsolutePath()));
        statusList.add(String.format("%s: %s", getString(R.string.saving),
                getString(R.string.logcat)));

        // check if we should take kernel logs too
        shouldTakeKernelLog(configuration.getKernelLogType(), commandList, statusList);

        // fix permissions
        if (mUid != -1) {
            commandList.add(String.format("busybox chown -R %s.%s %s;", mUid, mUid,
                    TMP.getAbsolutePath()));
            statusList.add(getString(R.string.fixing_permissions));
        }

        // we are done with collecting logs, lets compress that stuff
        // well, actually we do not compress yet, but we are adding the status entry
        shouldCompressLog(configuration.getLogCompression(), commandList, statusList);

        // and let's GO!
        new LogCollectorTask(getActivity(), commandList, statusList).execute();
    }

    private void shouldTakeKernelLog(final int kernelLogType, final ArrayList<String> commandList,
            final ArrayList<String> statusList) {
        final String type;
        switch (kernelLogType) {
            default:
            case LogConfiguration.TYPE_KERNEL_LOG_NONE:
                commandList.add("");
                statusList.add(getString(R.string.log_ignoring_kernel));
                return;
            case LogConfiguration.TYPE_KERNEL_LOG_BOTH:
                commandList.add(String
                        .format("busybox dmesg > %s/kmsg.txt;", TMP.getAbsolutePath()));
                statusList.add(String
                        .format("%s: %s", getString(R.string.saving), getString(R.string.kmsg)));

                commandList.add(String.format("busybox cat /proc/last_kmsg > %s/last_kmsg.txt;",
                        TMP.getAbsolutePath()));
                statusList.add(String.format("%s: %s", getString(R.string.saving),
                        getString(R.string.last_kmsg)));
                return;
            case LogConfiguration.TYPE_KERNEL_LOG_KMSG:
                commandList.add(String
                        .format("busybox dmesg > %s/kmsg.txt;", TMP.getAbsolutePath()));
                type = getString(R.string.kmsg);
                break;
            case LogConfiguration.TYPE_KERNEL_LOG_LAST_KMSG:
                commandList.add(String.format("busybox cat /proc/last_kmsg > %s/last_kmsg.txt;",
                        TMP.getAbsolutePath()));
                type = getString(R.string.last_kmsg);
                break;
        }

        statusList.add(String.format("%s: %s", getString(R.string.saving), type));
    }

    private void shouldCompressLog(final int logCompression, final ArrayList<String> commandList,
            final ArrayList<String> statusList) {
        switch (logCompression) {
            default:
            case LogConfiguration.TYPE_COMPRESSION_TAR:
                commandList.add("TAR");
                statusList.add(String.format("%s: %s", getString(R.string.compressing),
                        getString(R.string.tar)));
                break;
            case LogConfiguration.TYPE_COMPRESSION_TAR_GZ:
                commandList.add("TARGZ");
                statusList.add(String.format("%s: %s", getString(R.string.compressing),
                        getString(R.string.tar_gz)));
                break;
        }
    }

    private File compressLog(final int logCompression) {
        final String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final File finalFile = new File(getActivity().getCacheDir(), String.format("log_%s", date));

        switch (logCompression) {
            default:
            case LogConfiguration.TYPE_COMPRESSION_TAR: {
                final File newFile = new File(finalFile.getAbsolutePath() + ".tar");
                final String cmd = String.format("busybox tar -cf %s -C %s . && ",
                        newFile.getAbsolutePath(), TMP.getAbsolutePath());
                final String cmdPerms = (mUid == -1 ? "" : String.format("busybox chown %s.%s %s;",
                        mUid, mUid, newFile.getAbsolutePath()));
                Utils.runRootCommand(cmd + cmdPerms, true);
                return newFile;
            }
            case LogConfiguration.TYPE_COMPRESSION_TAR_GZ: {
                final File newFile = new File(finalFile.getAbsolutePath() + ".tar.gz");
                final String cmd = String.format("busybox tar -zcf %s -C %s . && ",
                        newFile.getAbsolutePath(), TMP.getAbsolutePath());
                final String cmdPerms = (mUid == -1 ? "" : String.format("busybox chown %s.%s %s;",
                        mUid, mUid, newFile.getAbsolutePath()));
                Utils.runRootCommand(cmd + cmdPerms, true);
                return newFile;
            }
        }
    }

    private void moveAndCleanUp(final File src, final File dst) {
        final StringBuilder sb = new StringBuilder();
        // copy the file over
        sb.append(String.format("cp %s %s && ", src.getAbsolutePath(), dst.getAbsolutePath()));
        // delete the source
        sb.append(String.format("rm -rf %s && ", src.getAbsolutePath()));
        // delete the working folder
        sb.append(String.format("rm -rf %s;", TMP.getAbsolutePath()));
        Utils.runRootCommand(sb.toString(), true);
    }

    private class LogCollectorTask extends AsyncTask<Void, String, File> {
        private final Context context;

        private final ArrayList<String> cmdList;
        private final ArrayList<String> statusList;

        private ProgressDialog dialog;

        public LogCollectorTask(final Context context, final ArrayList<String> cmdList,
                final ArrayList<String> statusList) {
            this.context = context;
            this.cmdList = cmdList;
            this.statusList = statusList;
        }

        @Override protected void onPreExecute() {
            dialog = new ProgressDialog(context);
            dialog.setTitle(R.string.log_collecting_wait);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage("...");
            dialog.show();
        }

        @Override protected File doInBackground(final Void... voids) {
            String cmd, msg;
            File output = null;
            final int length = cmdList.size();
            for (int i = 0; i < length; i++) {
                // store the command and status message
                cmd = cmdList.get(i);
                msg = statusList.get(i);

                // update the dialog
                publishProgress(msg);

                // check if we are compressing
                if (TextUtils.equals(cmd, "TAR")) {
                    output = compressLog(LogConfiguration.TYPE_COMPRESSION_TAR);
                } else if (TextUtils.equals(cmd, "TARGZ")) {
                    output = compressLog(LogConfiguration.TYPE_COMPRESSION_TAR_GZ);
                } else {
                    // wait for it ...
                    Utils.runRootCommand(cmd, true);
                }
            }

            if (output != null) {
                final File finalFile = new File(Environment.getExternalStorageDirectory()
                        + File.separator + "DeviceControl" + File.separator + output.getName());

                Logger.v(LogCollectorFragment.this, String.format("moving from %s to %s",
                        output.getAbsolutePath(), finalFile.getAbsolutePath()));

                // create directories
                finalFile.mkdirs();

                // if the file already exists, delete it
                if (finalFile.exists()) {
                    finalFile.delete();
                }

                // move the file
                moveAndCleanUp(output, finalFile);

                return finalFile;
            }

            return null;
        }

        @Override protected void onPostExecute(final File file) {
            dialog.dismiss();
            if (mLogCollect != null) {
                mLogCollect.setEnabled(true);
            }

            if (file == null) {
                return;
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.log_collected);
            builder.setMessage(getString(R.string.log_collected_msg, file.getAbsolutePath()));
            builder.setNegativeButton(android.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            builder.setPositiveButton(R.string.log_send, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialogInterface, int i) {
                    final Intent intent = new Intent(Intent.ACTION_SEND);
                    // set mime type
                    intent.setType(getMimeType(file.getName()));
                    // add attachment
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    // set a subject, just for fun
                    intent.putExtra(Intent.EXTRA_SUBJECT,
                            String.format("[Device Control] Collected logs - %s", file.getName()));

                    // create a chooser
                    try {
                        getActivity().startActivity(
                                Intent.createChooser(intent, getString(R.string.log_send)));
                    } catch (Exception e) {
                        Logger.e(LogCollectorFragment.this, "Error sending log. %s",
                                e.getMessage());
                    }
                }
            });

            builder.show();
        }

        @Override protected void onProgressUpdate(final String... values) {
            if (values[0] != null) {
                Logger.v(LogCollectorFragment.this, values[0]);
                dialog.setMessage(values[0]);
            }
        }
    }

    private String getMimeType(final String filename) {
        if (filename.endsWith(".tar.gz")) {
            return "application/gzip";
        } else if (filename.endsWith(".tar")) {
            return "application/x-tar";
        }
        return "text/plain";
    }
}
