package org.namelessrom.devicecontrol.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.tools.AppListFragment;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class AppDetailsActivity extends Activity {

    public static final String ARG_PACKAGE_NAME = "package";

    @Override protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve package name
        final Bundle args = getIntent().getExtras();
        String packageName = (args != null) ? args.getString(ARG_PACKAGE_NAME) : null;
        if (packageName == null) {
            final Intent intent =
                    (args == null) ? getIntent() : (Intent) args.getParcelable("intent");
            if (intent != null && intent.getData() != null) {
                packageName = intent.getData().getSchemeSpecificPart();
            }
        }
        logDebug("AppDetailsActivity", "packageName:" + String.valueOf(packageName));

        // Prepare bundle, containing the package name
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_PACKAGE_NAME, packageName);

        // Bind bundle to fragment
        final Fragment f = new AppListFragment();
        f.setArguments(bundle);

        // Show fragment
        getFragmentManager().beginTransaction()
                .add(R.id.container, f)
                .commit();
    }

    @Override protected void onPause() {
        super.onPause();
        finish();
    }
}
