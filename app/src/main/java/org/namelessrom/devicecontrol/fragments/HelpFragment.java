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
package org.namelessrom.devicecontrol.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.widgets.adapters.HelpArrayAdapter;

import butterknife.ButterKnife;

public class HelpFragment extends Fragment implements DeviceConstants {

    public static final String ARG_TYPE = "arg_type";

    public static HelpFragment newInstance(final int type) {
        final HelpFragment f = new HelpFragment();
        final Bundle b = new Bundle();
        b.putInt(HelpFragment.ARG_TYPE, type);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View v = inflater.inflate(R.layout.menu_list, container, false);

        final Bundle b = getArguments();

        int type = ID_DUMMY;
        if (b != null) {
            type = b.getInt(ARG_TYPE, 0);
        }
        final int[] ids = getIds(type);
        final int titleId = ids[0];
        final int contentId = ids[1];

        final ListView list = ButterKnife.findById(v, R.id.navbarlist);
        HelpArrayAdapter mAdapter = new HelpArrayAdapter(
                getActivity(),
                getResources().getStringArray(titleId),
                getResources().getStringArray(contentId)
        );
        list.setAdapter(mAdapter);

        return v;
    }

    private int[] getIds(final int type) {
        final int[] ids = new int[2];

        switch (type) {
            default:
            case ID_DUMMY:
                ids[0] = R.array.dummy_titles;
                ids[1] = R.array.dummy_content;
                break;
            case ID_HOME:
                ids[0] = R.array.home_titles;
                ids[1] = R.array.home_content;
                break;
            case ID_DEVICE:
                ids[0] = R.array.device_titles;
                ids[1] = R.array.device_content;
                break;
            case ID_PERFORMANCE_INFO:
                ids[0] = R.array.perf_info_titles;
                ids[1] = R.array.perf_info_content;
                break;
            case ID_PERFORMANCE_CPU_SETTINGS:
                ids[0] = R.array.cpu_titles;
                ids[1] = R.array.cpu_content;
                break;
            case ID_PERFORMANCE_GPU_SETTINGS:
                ids[0] = R.array.gpu_titles;
                ids[1] = R.array.gpu_content;
                break;
            case ID_PERFORMANCE_EXTRA:
                ids[0] = R.array.extras_titles;
                ids[1] = R.array.extras_content;
                break;
            case ID_HOTPLUGGING:
                ids[0] = R.array.extras_hotplugging_titles;
                ids[1] = R.array.extras_hotplugging_content;
                break;
            case ID_TOOLS_TASKER:
                ids[0] = R.array.tasker_titles;
                ids[1] = R.array.tasker_content;
                break;
            case ID_PREFERENCES:
                ids[0] = R.array.preference_titles;
                ids[1] = R.array.preference_content;
                break;
            case ID_LICENSES:
                ids[0] = R.array.licenses_titles;
                ids[1] = R.array.licenses_content;
                break;
        }

        return ids;
    }

}
