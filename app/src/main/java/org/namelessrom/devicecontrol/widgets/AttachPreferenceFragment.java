/*
 *  Copyright (C) 2013 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.widgets;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.BusProvider;

public class AttachPreferenceFragment extends PreferenceFragment {

    protected Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    protected void onAttach(Activity activity, int number) {
        super.onAttach(activity);
        BusProvider.getBus().post(new SectionAttachedEvent(number));
    }

    protected void isSupported(final PreferenceScreen preferenceScreen, final Context context) {
        if (preferenceScreen.getPreferenceCount() == 0) {
            preferenceScreen.addPreference(createPreference(context));
        }
    }

    protected void isSupported(final PreferenceCategory preferenceCategory, final Context context) {
        if (preferenceCategory.getPreferenceCount() == 0) {
            preferenceCategory.addPreference(createPreference(context));
        }
    }

    private CustomPreference createPreference(final Context context) {
        final CustomPreference pref = new CustomPreference(context);
        pref.setTitle(R.string.no_tweaks_available);
        pref.setSummary(R.string.no_tweaks_message);
        pref.setTitleColor("#ffffff");
        pref.setSummaryColor("#ffffff");
        return pref;
    }
}
