package org.namelessrom.devicecontrol.fragments.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.RefreshEvent;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;
import org.namelessrom.devicecontrol.widgets.AttachFragment;
import org.namelessrom.devicecontrol.widgets.adapters.FlashListAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static butterknife.ButterKnife.findById;
import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Created by alex on 22.06.14.
 */
public class FlasherFragment extends AttachFragment implements DeviceConstants {

    private static final int REQUEST_CODE_FILE = 100;

    private FlashListAdapter mAdapter;

    private LinearLayout mContainer;
    private ListView     mFlashList;

    private TextView mEmptyView;

    @Override
    public void onAttach(Activity activity) { super.onAttach(activity, ID_TOOLS_FLASHER); }

    @Override public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View v = inflater.inflate(R.layout.fragment_flasher, container, false);

        mContainer = findById(v, R.id.container);
        mFlashList = findById(v, R.id.flash_list);
        mEmptyView = findById(v, android.R.id.empty);

        mAdapter = new FlashListAdapter();

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFlashList.setAdapter(mAdapter);

        checkAdapter();
    }

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_flasher, menu);
    }

    @Override public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            }
            case R.id.action_task_add: {
                // TODO: pick file
                return true;
            }
            default: {
                return false;
            }
        }
    }

    private void checkAdapter() {
        if (mAdapter != null && mContainer != null && mEmptyView != null) {
            if (mAdapter.getCount() != 0) {
                mContainer.setVisibility(View.VISIBLE);
                mEmptyView.setVisibility(View.INVISIBLE);
            } else {
                mContainer.setVisibility(View.INVISIBLE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe public void onRefreshEvent(final RefreshEvent event) {
        if (event == null) return;
        checkAdapter();
    }

}
