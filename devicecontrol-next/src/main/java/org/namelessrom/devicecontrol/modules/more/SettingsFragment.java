package org.namelessrom.devicecontrol.modules.more;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.base.BaseViewPagerFragment;
import org.namelessrom.devicecontrol.preferences.AutoSwitchPreference;

import java.util.ArrayList;

import alexander.martinz.libs.logger.Logger;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsFragment extends BaseViewPagerFragment {
    private static final int EXPECTED_FRAGMENT_COUNT = 1;

    @Override public ViewPagerAdapter getPagerAdapter() {
        final ArrayList<Fragment> fragments = new ArrayList<>(EXPECTED_FRAGMENT_COUNT);
        final ArrayList<CharSequence> titles = new ArrayList<>(EXPECTED_FRAGMENT_COUNT);

        fragments.add(new GeneralSettingsFragment());
        titles.add(getString(R.string.general));

        return new ViewPagerAdapter(getChildFragmentManager(), fragments, titles);
    }

    public static class GeneralSettingsFragment extends BaseFragment implements MaterialPreference.MaterialPreferenceChangeListener {
        @Bind(R.id.cat_debug) MaterialPreferenceCategory debugCategory;
        @Bind(R.id.pref_debug_logging) AutoSwitchPreference debugLogging;
        @Bind(R.id.pref_debug_always_launch_setup) AutoSwitchPreference debugAlwaysLaunchSetup;

        @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.fragment_settings_general, container, false);
            ButterKnife.bind(this, view);

            debugLogging.initFromPreferences(BuildConfig.DEBUG);
            debugLogging.setOnPreferenceChangeListener(this);

            if (BuildConfig.DEBUG) {
                debugAlwaysLaunchSetup.initFromPreferences(false);
                debugAlwaysLaunchSetup.setOnPreferenceChangeListener(this);
            } else {
                debugAlwaysLaunchSetup.setVisibility(View.GONE);
            }

            return view;
        }

        @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
            if (preference == debugLogging) {
                debugLogging.onPreferenceChanged(preference, newValue);
                Logger.setEnabled((boolean) newValue);
                return true;
            } else if (preference == debugAlwaysLaunchSetup) {
                debugAlwaysLaunchSetup.onPreferenceChanged(preference, newValue);
                Snackbar.make(debugLogging, R.string.changes_on_restart, Snackbar.LENGTH_SHORT).show();
                return true;
            }
            return false;
        }
    }
}
