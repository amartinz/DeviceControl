package org.namelessrom.devicecontrol.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.negusoft.holoaccent.dialog.AccentDialogFragment;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;

import butterknife.ButterKnife;

/**
 * Created by alex on 15.07.14.
 */
public class ColorPickerDialogFragment extends AccentDialogFragment {

    private final PreferencesFragment.OnColorPickedListener onColorPickedListener;

    private ColorPicker colorPicker;

    public ColorPickerDialogFragment(final PreferencesFragment.OnColorPickedListener listener) {
        super();
        this.onColorPickedListener = listener;
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.dialog_color_picker, container, false);

        final int defaultColor = getResources().getColor(R.color.accent);
        final int color = PreferenceHelper.getInt("pref_color", defaultColor);

        final SaturationBar saturationBar = ButterKnife.findById(v, R.id.saturation);
        final ValueBar valueBar = ButterKnife.findById(v, R.id.value);
        final OpacityBar opacityBar = ButterKnife.findById(v, R.id.opacity);

        colorPicker = ButterKnife.findById(v, R.id.color_picker);

        colorPicker.addSaturationBar(saturationBar);
        colorPicker.addValueBar(valueBar);
        colorPicker.addOpacityBar(opacityBar);

        colorPicker.setColor(color);
        colorPicker.setOldCenterColor(color);

        final Button resetColor = ButterKnife.findById(v, R.id.color_reset);
        resetColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                colorPicker.setColor(defaultColor);
                PreferenceHelper.setInt("pref_color", colorPicker.getColor());
                onColorPickedListener.onColorPicked(colorPicker.getColor());
            }
        });

        final Button pickColor = ButterKnife.findById(v, R.id.color_pick);
        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                PreferenceHelper.setInt("pref_color", colorPicker.getColor());
                onColorPickedListener.onColorPicked(colorPicker.getColor());
                if (getDialog() != null) getDialog().dismiss();
            }
        });

        if (getDialog() != null) getDialog().setTitle(R.string.color_pick);

        return v;
    }

}
