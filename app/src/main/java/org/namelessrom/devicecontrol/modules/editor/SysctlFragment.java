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
package org.namelessrom.devicecontrol.modules.editor;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;

import org.namelessrom.devicecontrol.ActivityCallbacks;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.modules.bootup.BootupItem;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.DialogHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

public class SysctlFragment extends AttachPreferenceFragment {

    //==============================================================================================
    private static final String PREF_DIRTY_RATIO = "pref_dirty_ratio";
    private static final String PREF_DIRTY_BACKGROUND = "pref_dirty_background";
    private static final String PREF_DIRTY_EXPIRE = "pref_dirty_expire";
    private static final String PREF_DIRTY_WRITEBACK = "pref_dirty_writeback";
    private static final String PREF_MIN_FREE_KB = "pref_min_free_kb";
    private static final String PREF_OVERCOMMIT = "pref_overcommit";
    private static final String PREF_SWAPPINESS = "pref_swappiness";
    private static final String PREF_VFS = "pref_vfs";
    private static final String DIRTY_RATIO_PATH = "/proc/sys/vm/dirty_ratio";
    private static final String DIRTY_BACKGROUND_PATH = "/proc/sys/vm/dirty_background_ratio";
    private static final String DIRTY_EXPIRE_PATH = "/proc/sys/vm/dirty_expire_centisecs";
    private static final String DIRTY_WRITEBACK_PATH = "/proc/sys/vm/dirty_writeback_centisecs";
    private static final String MIN_FREE_PATH = "/proc/sys/vm/min_free_kbytes";
    private static final String OVERCOMMIT_PATH = "/proc/sys/vm/overcommit_ratio";
    private static final String SWAPPINESS_PATH = "/proc/sys/vm/swappiness";
    private static final String VFS_CACHE_PRESSURE_PATH = "/proc/sys/vm/vfs_cache_pressure";
    //==============================================================================================
    private CustomPreference mFullEditor;
    private CustomPreference mDirtyRatio;
    private CustomPreference mDirtyBackground;
    private CustomPreference mDirtyExpireCentisecs;
    private CustomPreference mDirtyWriteback;
    private CustomPreference mMinFreeK;
    private CustomPreference mOvercommit;
    private CustomPreference mSwappiness;
    private CustomPreference mVfs;

    @Override protected int getFragmentId() { return DeviceConstants.ID_TOOLS_VM; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.vm);

        mFullEditor = (CustomPreference) findPreference("pref_full_editor");
        mDirtyRatio = (CustomPreference) findPreference(PREF_DIRTY_RATIO);
        mDirtyBackground = (CustomPreference) findPreference(PREF_DIRTY_BACKGROUND);
        mDirtyExpireCentisecs = (CustomPreference) findPreference(PREF_DIRTY_EXPIRE);
        mDirtyWriteback = (CustomPreference) findPreference(PREF_DIRTY_WRITEBACK);
        mMinFreeK = (CustomPreference) findPreference(PREF_MIN_FREE_KB);
        mOvercommit = (CustomPreference) findPreference(PREF_OVERCOMMIT);
        mSwappiness = (CustomPreference) findPreference(PREF_SWAPPINESS);
        mVfs = (CustomPreference) findPreference(PREF_VFS);

        mDirtyRatio.setSummary(Utils.readOneLine(DIRTY_RATIO_PATH));
        mDirtyBackground.setSummary(Utils.readOneLine(DIRTY_BACKGROUND_PATH));
        mDirtyExpireCentisecs.setSummary(Utils.readOneLine(DIRTY_EXPIRE_PATH));
        mDirtyWriteback.setSummary(Utils.readOneLine(DIRTY_WRITEBACK_PATH));
        mMinFreeK.setSummary(Utils.readOneLine(MIN_FREE_PATH));
        mOvercommit.setSummary(Utils.readOneLine(OVERCOMMIT_PATH));
        mSwappiness.setSummary(Utils.readOneLine(SWAPPINESS_PATH));
        mVfs.setSummary(Utils.readOneLine(VFS_CACHE_PRESSURE_PATH));
    }

    @Override public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
            @NonNull final Preference preference) {
        if (preference == mFullEditor) {
            final Activity activity = getActivity();
            if (activity instanceof ActivityCallbacks) {
                ((ActivityCallbacks) activity).shouldLoadFragment(DeviceConstants.ID_TOOLS_EDITORS_VM);
            }
            return true;
        } else if (preference == mDirtyRatio) {
            final String title = getString(R.string.dirty_ratio_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(DIRTY_RATIO_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0,
                    100, preference, DIRTY_RATIO_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mDirtyBackground) {
            final String title = getString(R.string.dirty_background_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(DIRTY_BACKGROUND_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 100,
                    preference, DIRTY_BACKGROUND_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mDirtyExpireCentisecs) {
            final String title = getString(R.string.dirty_expire_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(DIRTY_EXPIRE_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 5000,
                    preference, DIRTY_EXPIRE_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mDirtyWriteback) {
            final String title = getString(R.string.dirty_writeback_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(DIRTY_WRITEBACK_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 5000,
                    preference, DIRTY_WRITEBACK_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mMinFreeK) {
            final String title = getString(R.string.min_free_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(MIN_FREE_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 8192,
                    preference, MIN_FREE_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mOvercommit) {
            final String title = getString(R.string.overcommit_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(OVERCOMMIT_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 100,
                    preference, OVERCOMMIT_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mSwappiness) {
            final String title = getString(R.string.swappiness_title);
            final int currentProgress = Utils.parseInt(Utils.readOneLine(SWAPPINESS_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 100,
                    preference, SWAPPINESS_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        } else if (preference == mVfs) {
            final String title = getString(R.string.vfs_title);
            final int currentProgress =
                    Utils.parseInt(Utils.readOneLine(VFS_CACHE_PRESSURE_PATH));
            DialogHelper.openSeekbarDialog(getActivity(), currentProgress, title, 0, 200,
                    preference, VFS_CACHE_PRESSURE_PATH, BootupConfig.CATEGORY_SYSCTL);
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static String restore(BootupConfig config) {
        final ArrayList<BootupItem> items = config
                .getItemsByCategory(BootupConfig.CATEGORY_SYSCTL);
        if (items.size() == 0) {
            return "";
        }

        final StringBuilder sbCmd = new StringBuilder();
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            sbCmd.append(Utils.getWriteCommand(item.name, item.value));
        }

        return sbCmd.toString();
    }

}
