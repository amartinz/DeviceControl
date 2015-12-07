package org.namelessrom.devicecontrol;

import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import org.namelessrom.devicecontrol.base.BaseActivity;

import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        ViewCompat.setElevation(toolbar, 4.0f);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}
