package org.namelessrom.devicecontrol.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.services.BootupService;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
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
                        PreferenceHelper.setBoolean(getKey(entries.get(item)), isChecked);
                    }
                });
        builder.setCancelable(true);
        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) { }
        });

        return builder.create();
    }

    private boolean isChecked(final int entry) {
        return PreferenceHelper.getBoolean(getKey(entry), false);
    }

    private String getKey(final int entry) {
        switch (entry) {
            case R.string.device:
                return BootupService.SOB_DEVICE;
            case R.string.cpusettings:
                return BootupService.SOB_CPU;
            case R.string.gpusettings:
                return BootupService.SOB_GPU;
            case R.string.extras:
                return BootupService.SOB_EXTRAS;
            case R.string.sysctl_vm:
                return BootupService.SOB_SYSCTL;
            case R.string.voltage_control:
                return BootupService.SOB_VOLTAGE;
            default:
                return "-";
        }
    }
}
