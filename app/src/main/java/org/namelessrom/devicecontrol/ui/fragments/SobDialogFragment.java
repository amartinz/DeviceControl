package org.namelessrom.devicecontrol.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.DeviceConfiguration;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class SobDialogFragment extends DialogFragment {
    final ArrayList<Integer> entries = new ArrayList<>();

    public SobDialogFragment() {
        super();
        entries.add(R.string.device);
        entries.add(R.string.cpusettings);
        entries.add(R.string.gpusettings);
        entries.add(R.string.extras);
        entries.add(R.string.sysctl_vm);

        if (Utils.fileExists(VoltageUtils.VDD_TABLE_FILE)
                || Utils.fileExists(VoltageUtils.UV_TABLE_FILE)) {
            entries.add(R.string.voltage_control);
        }
    }

    @Override @NonNull public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int length = entries.size();
        final String[] items = new String[length];
        final boolean[] checked = new boolean[length];

        for (int i = 0; i < length; i++) {
            items[i] = getString(entries.get(i));
            checked[i] = isChecked(entries.get(i));
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.reapply_on_boot);
        builder.setMultiChoiceItems(items, checked,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override public void onClick(final DialogInterface dialogInterface,
                            final int item, final boolean isChecked) {
                        setChecked(item, isChecked);
                    }
                });
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) { }
        });

        return builder.create();
    }

    @Override public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        DeviceConfiguration.get(getActivity()).saveConfiguration(getActivity());
    }

    private boolean isChecked(final int entry) {
        switch (entry) {
            case R.string.device:
                return DeviceConfiguration.get(getActivity()).sobDevice;
            case R.string.cpusettings:
                return DeviceConfiguration.get(getActivity()).sobCpu;
            case R.string.gpusettings:
                return DeviceConfiguration.get(getActivity()).sobGpu;
            case R.string.extras:
                return DeviceConfiguration.get(getActivity()).sobExtras;
            case R.string.sysctl_vm:
                return DeviceConfiguration.get(getActivity()).sobSysctl;
            case R.string.voltage_control:
                return DeviceConfiguration.get(getActivity()).sobVoltage;
            default:
                return false;
        }
    }

    private void setChecked(final int entry, final boolean checked) {
        switch (entry) {
            case 0:
                DeviceConfiguration.get(getActivity()).sobDevice = checked;
                return;
            case 1:
                DeviceConfiguration.get(getActivity()).sobCpu = checked;
                return;
            case 2:
                DeviceConfiguration.get(getActivity()).sobGpu = checked;
                return;
            case 3:
                DeviceConfiguration.get(getActivity()).sobExtras = checked;
                return;
            case 4:
                DeviceConfiguration.get(getActivity()).sobSysctl = checked;
                return;
            case 5:
                DeviceConfiguration.get(getActivity()).sobVoltage = checked;
                return;
            default:
                break;
        }
    }
}
