/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.information;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.DeviceConstants;
import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Created by alex on 18.12.13.
 */
public class InformationGeneralFragment extends Fragment implements DeviceConstants {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information_general,
                container, false);

        StringBuilder sb;

        // Haptic
        TextView tvInfoHaptic = (TextView) view.findViewById(R.id.information_haptic);

        sb = new StringBuilder();
        sb.append("\n\n");
        sb.append("Haptic\n\n");
        sb.append("---");
        sb.append("\n\n");

        tvInfoHaptic.setText(sb);

        // Light
        TextView tvInfoLight = (TextView) view.findViewById(R.id.information_light);

        sb = new StringBuilder();
        sb.append("Light\n\n");
        sb.append("---");
        sb.append("\n\n");

        tvInfoLight.setText(sb);

        // Graphics
        TextView tvInfoGraphics = (TextView) view.findViewById(R.id.information_graphic);

        sb = new StringBuilder();
        sb.append("Display Information\n\n");
        if (Utils.fileExists(FILE_INFO_DISPLAY_LCD_TYPE)) {
            sb.append("LCD Type: ").append(Utils.readOneLine(FILE_INFO_DISPLAY_LCD_TYPE));
        }
        sb.append("\n\n");

        tvInfoGraphics.setText(sb);

        // Sensor
        TextView tvInfoSensor = (TextView) view.findViewById(R.id.information_sensor);

        sb = new StringBuilder("Sensor\n\n");
        sb.append("---");
        sb.append("\n\n");

        tvInfoSensor.setText(sb);

        return view;
    }
}
