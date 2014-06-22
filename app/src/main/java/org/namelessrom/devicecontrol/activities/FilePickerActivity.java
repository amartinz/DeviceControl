package org.namelessrom.devicecontrol.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.FlashItemEvent;
import org.namelessrom.devicecontrol.fragments.filepicker.FilePickerFragment;
import org.namelessrom.devicecontrol.fragments.tools.FlasherFragment;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

/**
 * Created by alex on 25.06.14.
 */
public class FilePickerActivity extends Activity {

    @Override protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override protected void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
        getFragmentManager().beginTransaction()
                .add(R.id.container, buildFragment(getIntent()))
                .commit();
    }

    @Override protected void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    private Fragment buildFragment(final Intent intent) {
        final String fileType = intent.getStringExtra(FilePickerFragment.ARG_FILE_TYPE);
        // Prepare bundle, containing the package name
        final Bundle bundle = new Bundle(1);
        bundle.putString(FilePickerFragment.ARG_FILE_TYPE, (fileType != null ? fileType : ""));

        // Bind bundle to fragment
        final Fragment f = new FilePickerFragment();
        f.setArguments(bundle);

        return f;
    }

    @Subscribe public void onFlashItemEvent(final FlashItemEvent event) {
        if (event == null) return;
        final Bundle b = new Bundle(1);
        b.putSerializable(FlasherFragment.EXTRA_FLASHITEM, event.getItem());
        final Intent i = new Intent();
        i.putExtras(b);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

}
