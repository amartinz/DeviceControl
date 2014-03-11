package org.namelessrom.devicecontrol.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

public class SwitchWidget extends LinearLayout {

    private final TextView tvTitle;
    private final TextView tvSummary;
    private final Switch swToggle;

    public SwitchWidget(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomWidgets, 0, 0);
        final String titleText = a.getString(R.styleable.CustomWidgets_titleText);
        final String summaryText = a.getString(R.styleable.CustomWidgets_summaryText);
        a.recycle();

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.widget_switch, this, true);

        tvTitle = (TextView) v.findViewById(R.id.widget_switch_title);
        tvSummary = (TextView) v.findViewById(R.id.widget_switch_summary);
        swToggle = (Switch) v.findViewById(R.id.widget_switch_toggle);

        tvTitle.setText(titleText);
        tvTitle.setSelected(true);
        tvSummary.setText(summaryText);
    }

    public boolean isChecked() {
        return swToggle != null && swToggle.isChecked();
    }

    public void setOnToggleListener(Switch.OnCheckedChangeListener listener) {
        if (swToggle != null) {
            swToggle.setOnCheckedChangeListener(listener);
        }
    }

    public void setChecked(boolean checked) {
        if (swToggle != null) {
            swToggle.setChecked(checked);
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
        if (swToggle != null) {
            swToggle.setEnabled(enabled);
        }
    }

}
