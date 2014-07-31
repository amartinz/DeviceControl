package org.namelessrom.devicecontrol.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import static butterknife.ButterKnife.findById;

/**
 * Created by alex on 7/31/14.
 */
public class CpuCoreWidget extends LinearLayout {

    public TextView    core;
    public TextView    freq;
    public ProgressBar bar;

    public CpuCoreWidget(final Context context) {
        super(context);
        init(context);
    }

    private void init(final Context context) {
        final View v = LayoutInflater.from(context).inflate(R.layout.row_device, this, false);
        addView(v);

        core = findById(v, R.id.ui_device_title);
        freq = findById(v, R.id.ui_device_value);
        bar = findById(v, R.id.ui_device_bar);
    }


}
