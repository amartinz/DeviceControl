package org.namelessrom.devicecontrol.ui.preferences.hardware;

import android.content.Context;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.hardware.monitors.GpuMonitor;
import org.namelessrom.devicecontrol.utils.Utils;

import static android.opengl.GLES20.GL_RENDERER;
import static android.opengl.GLES20.glGetString;

public class GpuView extends Preference implements GpuMonitor.GpuMonitorListener {

    public TextView core;
    public TextView freq;
    public NumberProgressBar bar;

    public GpuView(final Context context) {
        super(context);
        setLayoutResource(R.layout.row_device);
    }

    public void onResume() {
        GpuMonitor.getInstance().start(this);
    }

    public void onPause() {
        GpuMonitor.getInstance().stop();
    }

    @Override protected void onBindView(@NonNull final View view) {
        super.onBindView(view);
        core = (TextView) view.findViewById(R.id.ui_device_title);
        freq = (TextView) view.findViewById(R.id.ui_device_value);
        bar = (NumberProgressBar) view.findViewById(R.id.ui_device_bar);
    }

    @Override public void onGpu(final GpuUtils.Gpu gpu) {
        if (core != null) {
            final String title;
            if (GpuUtils.isOpenGLES20Supported()) {
                title = glGetString(GL_RENDERER);
            } else {
                title = Application.get().getString(R.string.gpu);
            }
            core.setText(title);
        }
        if (freq != null) {
            freq.setText(String.format("%s / %s [%s]", GpuUtils.toMhz(gpu.current),
                    GpuUtils.toMhz(gpu.max), gpu.governor));
        }
        if (bar != null) {
            bar.setMax(Utils.parseInt(gpu.max, 0) / 1000);
            bar.setProgress(Utils.parseInt(gpu.current, 0) / 1000);
        }
    }
}
