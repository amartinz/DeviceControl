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
package org.namelessrom.devicecontrol.ui.views;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.support.v4.preference.PreferenceFragment;

import org.namelessrom.devicecontrol.MainActivity;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.listeners.OnSectionAttachedListener;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeEditTextPreference;
import org.namelessrom.devicecontrol.ui.preferences.AwesomeTogglePreference;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.AppHelper;

public abstract class AttachPreferenceFragment extends PreferenceFragment
        implements OnBackPressedListener, Preference.OnPreferenceChangeListener {

    /**
     * @return The fragment id
     */
    protected abstract int getFragmentId();

    @Override public void onAttach(final Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnSectionAttachedListener) {
            ((OnSectionAttachedListener) activity).onSectionAttached(getFragmentId());
        }
    }

    @Override public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (MainActivity.sSlidingMenu != null && MainActivity.sSlidingMenu.isMenuShowing()) {
            MainActivity.sSlidingMenu.toggle(true);
        }
    }

    @Override public void onResume() {
        super.onResume();
        final Activity activity = getActivity();
        if (!AppHelper.preventOnResume && activity instanceof MainActivity) {
            ((MainActivity) activity).setFragment(this);
        }
        MainActivity.loadFragment(activity, getFragmentId(), true);
    }

    @Override public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference instanceof AwesomeTogglePreference) {
            ((AwesomeTogglePreference) preference).writeValue((Boolean) o);
            return true;
        } else if (preference instanceof AwesomeEditTextPreference) {
            ((AwesomeEditTextPreference) preference).writeValue(String.valueOf(o));
            return true;
        }

        return false;
    }

    public void removeIfEmpty(final PreferenceScreen root, final PreferenceGroup preferenceGroup) {
        if (root != null && preferenceGroup.getPreferenceCount() == 0) {
            root.removePreference(preferenceGroup);
        }
    }

    protected void isSupported(final PreferenceScreen preferenceScreen, final Context context) {
        isSupported(preferenceScreen, context, R.string.no_tweaks_message);
    }

    protected void isSupported(final PreferenceScreen preferenceScreen, final Context context,
            final int sId) {
        if (preferenceScreen.getPreferenceCount() == 0) {
            preferenceScreen.addPreference(createPreference(context, sId));
        }
    }

    protected void isSupported(final PreferenceCategory preferenceCategory, final Context context) {
        isSupported(preferenceCategory, context, R.string.no_tweaks_message);
    }

    protected void isSupported(final PreferenceCategory preferenceCategory, final Context context,
            final int sId) {
        if (preferenceCategory.getPreferenceCount() == 0) {
            preferenceCategory.addPreference(createPreference(context, sId));
        }
    }

    private CustomPreference createPreference(final Context context, final int sId) {
        final CustomPreference pref = new CustomPreference(context);
        pref.setTitle(R.string.no_tweaks_available);
        pref.setSummary(sId);
        return pref;
    }

    /*
     * whether we should show the burger on a back action.
     * defaults to true, set to false to show an arrow
     * possible use case when to set it to false: sub fragment navigation
     */
    @Override public boolean showBurger() { return true; }

    @Override public boolean onBackPressed() { return false; }

}
