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
package org.namelessrom.devicecontrol.views;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.modules.cpu.monitors.CpuStateMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CpuStateView extends LinearLayout implements CpuUtils.StateListener {

    private LinearLayout mStatesView;

    private TextView mAdditionalStates;
    private TextView mTotalStateTime;
    private TextView mHeaderAdditionalStates;
    private TextView mHeaderTotalStateTime;
    private TextView mStatesWarning;

    private boolean mUpdatingData = false;

    public CpuStateView(final Context context) { this(context, null); }

    public CpuStateView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuStateView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        createViews(context);
    }

    public void onResume() {
        Timber.d("onResume");
    }

    public void onPause() {
        Timber.d("onPause");
    }

    public void onDestroy() {
        // nothing to be done yet
    }

    private void createViews(final Context context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.widget_cpu_states, this);

        mStatesView = (LinearLayout) view.findViewById(R.id.ui_states_view);
        mAdditionalStates = (TextView) view.findViewById(R.id.ui_additional_states);
        mHeaderAdditionalStates = (TextView) view.findViewById(R.id.ui_header_additional_states);
        mHeaderTotalStateTime = (TextView) view.findViewById(R.id.ui_header_total_state_time);
        mStatesWarning = (TextView) view.findViewById(R.id.ui_states_warning);
        mTotalStateTime = (TextView) view.findViewById(R.id.ui_total_state_time);

        refreshData();
    }

    public void refreshData() { new CpuStateUpdater().execute(); }

    private class CpuStateUpdater extends AsyncTask<Void, Void, Void> {

        @Override protected Void doInBackground(Void... params) {
            if (!mUpdatingData) {
                mUpdatingData = true;
                try {
                    CpuStateMonitor.getInstance().updateStates(CpuStateView.this);
                } catch (IOException e) {
                    Timber.e(e, "updateStates()");
                }
            }
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) { mUpdatingData = false; }
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
        final LinearLayout view = (LinearLayout) inflate(getContext(), R.layout.row_state, null);

        int per = ((int) ((float) state.duration * 100 / totalStateTime));
        if (per > 100) { per = 100; } else if (per < 0) { per = 0; }

        final String sFreq;
        if (state.freq == 0) {
            sFreq = getContext().getString(R.string.deep_sleep);
        } else {
            sFreq = state.freq / 1000 + " MHz";
        }

        long tSec = state.duration / 100;
        final String sDur = toString(tSec);

        final TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
        final TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
        final NumberProgressBar bar = (NumberProgressBar) view.findViewById(R.id.ui_bar);

        freqText.setText(sFreq);
        durText.setText(sDur);
        bar.setProgress(per);

        parent.addView(view);
        return view;
    }

    @Override public void onStates(@NonNull final CpuUtils.State states) {
        mStatesView.removeAllViews();

        final List<String> extraStates = new ArrayList<>();
        for (CpuStateMonitor.CpuState state : states.states) {
            if (state.duration > 0) {
                generateStateRow(state, mStatesView, states.totalTime);
            } else {
                if (state.freq == 0) {
                    extraStates.add(getContext().getString(R.string.deep_sleep));
                } else {
                    extraStates.add(state.freq / 1000 + " MHz");
                }
            }
        }

        if (states.states.size() == 0) {
            mStatesWarning.setVisibility(View.VISIBLE);
            mHeaderTotalStateTime.setVisibility(View.GONE);
            mTotalStateTime.setVisibility(View.GONE);
            mStatesView.setVisibility(View.GONE);
        }

        final long totTime = states.totalTime / 100;
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

}
