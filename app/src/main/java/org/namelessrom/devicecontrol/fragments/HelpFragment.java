package org.namelessrom.devicecontrol.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.widgets.adapters.HelpArrayAdapter;

public class HelpFragment extends Fragment {

    public static final String ARG_TYPE = "arg_type";

    public static final int TYPE_DUMMY      = -1;
    public static final int TYPE_HOME       = 110;
    public static final int TYPE_DEVICE     = 710;
    public static final int TYPE_PERF_INFO  = 810;
    public static final int TYPE_CPU        = 820;
    public static final int TYPE_GPU        = 830;
    public static final int TYPE_EXTRAS     = 840;
    public static final int TYPE_TASKER     = 910;
    public static final int TYPE_EDITORS    = 920;
    public static final int TYPE_FREEZER    = 930;
    public static final int TYPE_PREFERENCE = 1100;
    public static final int TYPE_LICENSES   = 1200;

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

        final int type = getArguments().getInt(ARG_TYPE, 0);
        final int[] ids = getIds(type);
        final int titleId = ids[0];
        final int contentId = ids[1];

        final ListView list = (ListView) v.findViewById(R.id.navbarlist);
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
            case TYPE_DUMMY:
                ids[0] = R.array.dummy_titles;
                ids[1] = R.array.dummy_content;
                break;
            case TYPE_HOME:
                ids[0] = R.array.home_titles;
                ids[1] = R.array.home_content;
                break;
            case TYPE_DEVICE:
                ids[0] = R.array.device_titles;
                ids[1] = R.array.device_content;
                break;
            case TYPE_PERF_INFO:
                ids[0] = R.array.perf_info_titles;
                ids[1] = R.array.perf_info_content;
                break;
            case TYPE_CPU:
                ids[0] = R.array.cpu_titles;
                ids[1] = R.array.cpu_content;
                break;
            case TYPE_GPU:
                ids[0] = R.array.gpu_titles;
                ids[1] = R.array.gpu_content;
                break;
            case TYPE_EXTRAS:
                ids[0] = R.array.extras_titles;
                ids[1] = R.array.extras_content;
                break;
            case TYPE_TASKER:
                ids[0] = R.array.tasker_titles;
                ids[1] = R.array.tasker_content;
                break;
            case TYPE_EDITORS:
                ids[0] = R.array.editors_titles;
                ids[1] = R.array.editors_content;
                break;
            case TYPE_FREEZER:
                ids[0] = R.array.freezer_titles;
                ids[1] = R.array.freezer_content;
                break;
            case TYPE_PREFERENCE:
                ids[0] = R.array.preference_titles;
                ids[1] = R.array.preference_content;
                break;
            case TYPE_LICENSES:
                ids[0] = R.array.licenses_titles;
                ids[1] = R.array.licenses_content;
                break;
        }

        return ids;
    }

}
