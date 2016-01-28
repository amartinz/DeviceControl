package org.namelessrom.devicecontrol.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class CpuCoreView extends LinearLayout {

    public TextView core;
    public TextView freq;
    public NumberProgressBar bar;

    public CpuCoreView(final Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.row_device, this, false);
        addView(v);

        core = (TextView) v.findViewById(R.id.ui_device_title);
        freq = (TextView) v.findViewById(R.id.ui_device_value);
        bar = (NumberProgressBar) v.findViewById(R.id.ui_device_bar);
    }


}
