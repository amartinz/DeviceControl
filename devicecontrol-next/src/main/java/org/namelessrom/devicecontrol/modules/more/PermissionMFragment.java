package org.namelessrom.devicecontrol.modules.more;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tbruyelle.rxpermissions.RxPermissions;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseFragment;

@TargetApi(Build.VERSION_CODES.M)
public class PermissionMFragment extends BaseFragment {
    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_others_permission, container, false);
        final Context context = view.getContext();

        final Button buttonStorage = (Button) view.findViewById(R.id.btn_grant_storage);
        boolean grantedStorage = (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                                  == PackageManager.PERMISSION_GRANTED);
        grantedStorage = grantedStorage && (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                            == PackageManager.PERMISSION_GRANTED);
        toggleButton(buttonStorage, grantedStorage);
        buttonStorage.setOnClickListener((v) -> RxPermissions.getInstance(v.getContext())
                .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    toggleButton(buttonStorage, granted);
                }));

        return view;
    }

    private void toggleButton(final Button button, final boolean granted) {
        button.setEnabled(!granted);
        button.setText(granted ? R.string.granted : R.string.grant);

        final Context context = button.getContext();
        final int color = granted
                ? ContextCompat.getColor(context, R.color.grass)
                : ContextCompat.getColor(context, R.color.red_middle);
        button.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }
}
