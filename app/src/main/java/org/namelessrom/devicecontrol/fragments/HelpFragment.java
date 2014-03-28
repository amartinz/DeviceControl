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
    public static final int TYPE_PREFERENCE = 0;

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
            case TYPE_PREFERENCE:
                ids[0] = R.array.preference_titles;
                ids[1] = R.array.preference_content;
                break;
        }

        return ids;
    }

}
