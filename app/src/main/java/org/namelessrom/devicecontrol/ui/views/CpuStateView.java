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
package org.namelessrom.devicecontrol.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.squareup.leakcanary.RefWatcher;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.modules.cpu.monitors.CpuStateMonitor;
import org.namelessrom.devicecontrol.theme.AppResources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class CpuStateView extends LinearLayout implements CpuUtils.StateListener {
    private boolean mUpdatingData = false;

    private HorizontalBarChart mCpuStateChart;

    public CpuStateView(final Context context) {
        this(context, null);
    }

    public CpuStateView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CpuStateView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        createViews(context);
    }

    public void onResume() {
        Logger.d(this, "onResume");
    }

    public void onPause() {
        Logger.d(this, "onPause");
    }

    public void onDestroy() {
        RefWatcher refWatcher = Application.getRefWatcher(getContext());
        refWatcher.watch(this);
    }

    private void createViews(final Context context) {
        final View view = LayoutInflater.from(context).inflate(R.layout.widget_cpu_states, this);

        mCpuStateChart = (HorizontalBarChart) view.findViewById(R.id.cpu_state_chart);
        mCpuStateChart.setNoDataText(getContext().getString(R.string.no_data_available));

        mCpuStateChart.setDragDecelerationFrictionCoef(0.95f);
        mCpuStateChart.setDrawBarShadow(false);
        mCpuStateChart.setDrawValueAboveBar(true);
        mCpuStateChart.setDrawGridBackground(false);
        mCpuStateChart.setDescription("");
        mCpuStateChart.setHighlightEnabled(false);
        mCpuStateChart.setPinchZoom(false);

        final AppResources appResources = AppResources.get();

        final XAxis xAxis = mCpuStateChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(appResources.getRobotoCondensed());
        xAxis.setTextSize(10f);
        xAxis.setTextColor(appResources.getTextColorPlain());
        xAxis.setAvoidFirstLastClipping(true);

        final YAxis yAxisLeft = mCpuStateChart.getAxisLeft();
        yAxisLeft.setValueFormatter(mValueFormatter);
        yAxisLeft.setTypeface(appResources.getRobotoCondensed());
        yAxisLeft.setTextColor(appResources.getTextColorPlain());
        yAxisLeft.setTextSize(10f);

        final YAxis yAxisRight = mCpuStateChart.getAxisRight();
        yAxisRight.setValueFormatter(mValueFormatter);
        yAxisRight.setTypeface(appResources.getRobotoCondensed());
        yAxisRight.setTextColor(appResources.getTextColorPlain());
        yAxisRight.setTextSize(10f);

        final Legend legend = mCpuStateChart.getLegend();
        legend.setEnabled(false);

        refreshData();
    }

    public void refreshData() {
        new CpuStateUpdater().execute();
    }

    private class CpuStateUpdater extends AsyncTask<Void, Void, Void> {
        @Override protected Void doInBackground(Void... params) {
            if (!mUpdatingData) {
                mUpdatingData = true;
                try {
                    CpuStateMonitor.getInstance().updateStates(CpuStateView.this);
                } catch (IOException e) {
                    Logger.e(this, "updateStates()", e);
                }
            }
            return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
            mUpdatingData = false;
        }
    }

    @Override public void onStates(@NonNull final CpuUtils.State states) {
        final ArrayList<CpuStateMonitor.CpuState> cpuStates = new ArrayList<>();
        cpuStates.addAll(states.states);

        // TODO: configurable
        Collections.reverse(cpuStates);

        final int length = cpuStates.size();
        final ArrayList<String> freqList = new ArrayList<>(length);
        final ArrayList<BarEntry> sliceList = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            final CpuStateMonitor.CpuState state = cpuStates.get(i);
            final String sFreq;
            if (state.freq == 0) {
                sFreq = getContext().getString(R.string.cpu_state_standby);
            } else {
                sFreq = state.freq / 1000 + " MHz";
            }

            freqList.add(sFreq);
            sliceList.add(new BarEntry(state.duration, i));
        }

        final AppResources appResources = AppResources.get();
        final Resources res = getContext().getResources();

        final String totTimeString = mValueFormatter.getFormattedValue(states.totalTime);

        final BarDataSet dataSet =
                new BarDataSet(sliceList, res.getString(R.string.frequency_distribution));
        dataSet.setLabel(res.getString(R.string.time_since_boot, totTimeString));
        dataSet.setValueTypeface(appResources.getRobotoCondensed());
        dataSet.setBarSpacePercent(50f);
        dataSet.setColors(ColorTemplate.createColors(ColorTemplate.VORDIPLOM_COLORS));

        final BarData data = new BarData(freqList, dataSet);
        data.setDrawValues(true);
        data.setValueTextSize(10f);
        data.setValueFormatter(mValueFormatter);
        data.setValueTextColor(appResources.getTextColorPlain());
        mCpuStateChart.setData(data);

        // we are ready
        mCpuStateChart.animateY(1500, Easing.EasingOption.EaseInOutQuad);
    }

    private final ValueFormatter mValueFormatter = new ValueFormatter() {
        @Override public String getFormattedValue(float value) {
            value /= 100;

            final long h = (long) Math.floor(value / (60 * 60));
            final long m = (long) Math.floor((value - h * 60 * 60) / 60);
            final long s = (long) value % 60;

            final StringBuilder sDur = new StringBuilder();
            if (h != 0) {
                sDur.append(h).append('h').append(' ');
            }
            if (m != 0) {
                sDur.append(m).append('m').append(' ');
            } else if (h != 0) {
                sDur.append("0m").append(' ');
            }
            sDur.append(s).append('s');
            return sDur.toString();
        }
    };

}
