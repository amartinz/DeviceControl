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

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;

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
        final int color = Application.get().getAccentColor();

        final SaturationBar saturationBar = (SaturationBar) v.findViewById(R.id.saturation);
        final ValueBar valueBar = (ValueBar) v.findViewById(R.id.value);
        final OpacityBar opacityBar = (OpacityBar) v.findViewById(R.id.opacity);

        colorPicker = (ColorPicker) v.findViewById(R.id.color_picker);

        colorPicker.addSaturationBar(saturationBar);
        colorPicker.addValueBar(valueBar);
        colorPicker.addOpacityBar(opacityBar);

        colorPicker.setColor(color);
        colorPicker.setOldCenterColor(color);

        final Button resetColor = (Button) v.findViewById(R.id.color_reset);
        resetColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                colorPicker.setColor(defaultColor);
                PreferenceHelper.setInt("pref_color", defaultColor);
                onColorPickedListener.onColorPicked(defaultColor);
            }
        });

        final Button pickColor = (Button) v.findViewById(R.id.color_pick);
        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Application.get().setAccentColor(colorPicker.getColor());
                PreferenceHelper.setInt("pref_color", Application.get().getAccentColor());
                onColorPickedListener.onColorPicked(Application.get().getAccentColor());
                Utils.restartActivity(getActivity());
            }
        });

        if (getDialog() != null) getDialog().setTitle(R.string.color_pick);

        return v;
    }

}
