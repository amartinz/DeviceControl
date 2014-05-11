package org.namelessrom.devicecontrol.widgets;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.CpuStateEvent;
import org.namelessrom.devicecontrol.utils.monitors.CpuStateMonitor;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 23.04.14.
 */
public class CpuStateWidget extends LinearLayout {

    private LinearLayout mStatesView;
    private TextView     mAdditionalStates;
    private TextView     mTotalStateTime;
    private TextView     mHeaderAdditionalStates;
    private TextView     mHeaderTotalStateTime;
    private TextView     mStatesWarning;

    private boolean mUpdatingData = false;
    private boolean mIsAttached   = false;

    public CpuStateWidget(Context context) {
        super(context);
        createViews(context);
    }

    public CpuStateWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        createViews(context);
    }

    public CpuStateWidget(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        createViews(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttached = true;
        BusProvider.getBus().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mIsAttached = false;
        BusProvider.getBus().unregister(this);
    }

    private void createViews(final Context context) {
        final View view = inflate(context, R.layout.widget_cpu_states, this);

        mStatesView = (LinearLayout) view.findViewById(R.id.ui_states_view);
        mAdditionalStates = (TextView) view.findViewById(R.id.ui_additional_states);
        mHeaderAdditionalStates = (TextView) view.findViewById(R.id.ui_header_additional_states);
        mHeaderTotalStateTime = (TextView) view.findViewById(R.id.ui_header_total_state_time);
        mStatesWarning = (TextView) view.findViewById(R.id.ui_states_warning);
        mTotalStateTime = (TextView) view.findViewById(R.id.ui_total_state_time);
        final ImageView mRefresh = (ImageView) view.findViewById(R.id.ui_refresh);
        mRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData();
            }
        });

        refreshData();
    }

    private void refreshData() {
        new CpuStateUpdater().execute();
    }

    private class CpuStateUpdater extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!mUpdatingData) {
                mUpdatingData = true;
                try {
                    CpuStateMonitor.getInstance().updateStates();
                } catch (IOException ignored) { }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mUpdatingData = false;
        }
    }

    private static String toString(final long tSec) {
        final long h = (long) Math.floor(tSec / (60 * 60));
        final long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
        final long s = tSec % 60;

        final StringBuilder sDur = new StringBuilder();
        sDur.append(h).append(':');
        if (m < 10) {
            sDur.append('0');
        }
        sDur.append(m).append(':');
        if (s < 10) {
            sDur.append('0');
        }
        sDur.append(s);

        return sDur.toString();
    }

    private View generateStateRow(final CpuStateMonitor.CpuState state, final ViewGroup parent,
            final long totalStateTime) {

        if (!isAttached()) { return null; }

        final Context context = getContext();

        if (context == null) { return null; }

        final LinearLayout view = (LinearLayout) inflate(context, R.layout.row_state, null);

        float per = (float) state.duration * 100 / totalStateTime;
        final String sPer = (int) per + "%";

        String sFreq;
        if (state.freq == 0) {
            sFreq = context.getString(R.string.deep_sleep);
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        long tSec = state.duration / 100;
        final String sDur = toString(tSec);

        final TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
        final TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
        final TextView perText = (TextView) view.findViewById(R.id.ui_percentage_text);
        final ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_bar);

        freqText.setText(sFreq);
        perText.setText(sPer);
        durText.setText(sDur);
        bar.setProgress((int) per);

        parent.addView(view);
        return view;
    }

    @Subscribe
    public void onCpuStateEvent(final CpuStateEvent event) {
        if (event == null) { return; }
        if (!isAttached() || getContext() == null) { return; }

        final List<CpuStateMonitor.CpuState> states = event.getStates();
        final long totalStateTime = event.getTotalStateTime();

        mStatesView.removeAllViews();
        final List<String> extraStates = new ArrayList<String>();
        for (CpuStateMonitor.CpuState state : states) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView, totalStateTime);
            } else {
                if (state.freq == 0) {
                    extraStates.add(getContext().getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.freq / 1000 + " MHz");
                }
            }
        }

        if (states.size() == 0) {
            mStatesWarning.setVisibility(View.VISIBLE);
            mHeaderTotalStateTime.setVisibility(View.GONE);
            mTotalStateTime.setVisibility(View.GONE);
            mStatesView.setVisibility(View.GONE);
        }

        final long totTime = totalStateTime / 100;
        mTotalStateTime.setText(toString(totTime));

        if (extraStates.size() > 0) {
            int n = 0;
            final StringBuilder sb = new StringBuilder();

            for (final String s : extraStates) {
                if (n++ > 0) {
                    sb.append(", ");
                }
                sb.append(s);
            }

            mAdditionalStates.setVisibility(View.VISIBLE);
            mHeaderAdditionalStates.setVisibility(View.VISIBLE);
            mAdditionalStates.setText(sb.toString());
        } else {
            mAdditionalStates.setVisibility(View.GONE);
            mHeaderAdditionalStates.setVisibility(View.GONE);
        }
    }

    private boolean isAttached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return isAttachedToWindow();
        } else {
            return mIsAttached;
        }
    }

}
