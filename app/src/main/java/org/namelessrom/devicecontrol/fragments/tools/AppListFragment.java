package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachListFragment;
import org.namelessrom.devicecontrol.widgets.adapters.AppListAdapter;

/**
 * Created by alex on 11.05.14.
 */
public class AppListFragment extends AttachListFragment implements DeviceConstants {

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_TOOLS_APP_MANAGER); }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        setListAdapter(new AppListAdapter(getActivity()));
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            default: {
                break;
            }
        }

        return false;
    }
}
