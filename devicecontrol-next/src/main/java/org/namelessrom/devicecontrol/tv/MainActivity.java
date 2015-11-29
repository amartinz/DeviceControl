package org.namelessrom.devicecontrol.tv;

import android.app.Activity;
import android.os.Bundle;

import org.namelessrom.devicecontrol.R;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_activity_main);
    }

    /*@Override public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }*/
}
