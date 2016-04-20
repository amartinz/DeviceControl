package org.namelessrom.devicecontrol.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.utils.DrawableHelper;

/**
 * Created by amartinz on 20.04.16.
 */
public class QuickActionActivity extends Activity implements DialogInterface.OnClickListener {
    private AlertDialog chooserDialog;

    @Override protected void onDestroy() {
        if (chooserDialog != null) {
            chooserDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String[] rebootOptions = new String[]{
                getString(R.string.media_scan_trigger),
        };

        final Drawable quickAction = ContextCompat.getDrawable(this, R.drawable.ic_build_black_24dp).mutate();
        final int powerColor = ContextCompat.getColor(this, R.color.accent_light);
        DrawableHelper.applyColorFilter(quickAction, powerColor);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setIcon(quickAction);
        builder.setTitle(R.string.quick_actions);
        builder.setItems(rebootOptions, this);
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                finish();
            }
        });

        chooserDialog = builder.create();
        chooserDialog.show();
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0: {
                AppHelper.startMediaScan(null, getApplicationContext());
                break;
            }
        }
        dialog.dismiss();

        finish();
    }
}
