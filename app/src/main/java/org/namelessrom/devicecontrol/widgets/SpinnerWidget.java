package org.namelessrom.devicecontrol.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class SpinnerWidget extends LinearLayout {

    private final TextView tvTitle;
    private final TextView tvSummary;
    private final Spinner swSpinner;

    public SpinnerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomWidgets, 0, 0);
        final String titleText = a.getString(R.styleable.CustomWidgets_titleText);
        final String summaryText = a.getString(R.styleable.CustomWidgets_summaryText);
        final CharSequence[] entries = a.getTextArray(R.styleable.CustomWidgets_entries);
        a.recycle();

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.widget_spinner, this, true);

        tvTitle = (TextView) v.findViewById(R.id.widget_spinner_title);
        tvSummary = (TextView) v.findViewById(R.id.widget_spinner_summary);
        swSpinner = (Spinner) v.findViewById(R.id.widget_spinner);

        final ArrayAdapter<CharSequence> dataAdapter = new ArrayAdapter<CharSequence>(context,
                android.R.layout.simple_spinner_item, entries);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        swSpinner.setAdapter(dataAdapter);

        tvTitle.setText(titleText);
        tvTitle.setSelected(true);
        tvSummary.setText(summaryText);
    }

    public void setOnItemSelectedListener(Spinner.OnItemSelectedListener listener) {
        if (swSpinner != null) {
            swSpinner.setOnItemSelectedListener(listener);
        }
    }

    public void setTitle(String title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setTitle(int title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setSummary(String title) {
        if (tvSummary != null) {
            tvSummary.setText(title);
        }
    }

    public void setSummary(int title) {
        if (tvSummary != null) {
            tvSummary.setText(title);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (swSpinner != null) {
            swSpinner.setEnabled(enabled);
        }
    }

    public Spinner getSpinner() {
        return swSpinner;
    }

    public int getSelectedPosition() {
        return swSpinner.getSelectedItemPosition();
    }

}
